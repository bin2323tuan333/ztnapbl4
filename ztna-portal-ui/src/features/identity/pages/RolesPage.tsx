import { useState } from 'react';
import { Plus, Pencil, Trash2, Users as UsersIcon, X } from 'lucide-react';
import Modal from '@/components/common/Modal';
import { SearchBar } from '@/components/common/SearchBar';
import type { User } from '@/shared/types/model/User';
import { userApi, type RoleItem } from '../api/identityApi';
import { useRoles } from '../hooks/useRoles';
import '@/shared/styles/AdminPage.css';

const EMPTY_ROLE = { name: '', displayName: '', description: '' };
const EMPTY_ASSIGN = { userId: '', reason: '', expiresAt: '' };

const formatDate = (v: string | null) => (v ? new Date(v).toLocaleString('vi-VN') : 'Không hết hạn');

export const RolesPage = () => {
    const { roles, isLoading, error, searchTerm, setSearchTerm, saveRole, deleteRole,
        members, setMembers, loadMembers, assignUser, unassignUser } = useRoles();

    // Form thêm / sửa (editing = null nghĩa là đang thêm mới)
    const [showForm, setShowForm] = useState(false);
    const [editing, setEditing] = useState<RoleItem | null>(null);
    const [form, setForm] = useState(EMPTY_ROLE);

    // Hộp thoại thành viên
    const [memberRole, setMemberRole] = useState<RoleItem | null>(null);
    const [allUsers, setAllUsers] = useState<User[]>([]);
    const [assign, setAssign] = useState(EMPTY_ASSIGN);

    const [formError, setFormError] = useState('');

    const openCreate = () => {
        setEditing(null);
        setForm(EMPTY_ROLE);
        setFormError('');
        setShowForm(true);
    };
    const openEdit = (r: RoleItem) => {
        setEditing(r);
        setForm({ name: r.name, displayName: r.displayName, description: r.description ?? '' });
        setFormError('');
        setShowForm(true);
    };

    const handleSave = async (e: React.FormEvent) => {
        e.preventDefault();
        setFormError('');
        try {
            await saveRole(editing?.id ?? null, form);
            setShowForm(false);
        } catch (err: any) {
            setFormError(err.message || 'Lưu vai trò thất bại');
        }
    };

    const handleDelete = async (r: RoleItem) => {
        if (!window.confirm(`Xóa vai trò "${r.name}"?`)) return;
        try { await deleteRole(r.id); } catch (err: any) { alert(err.message); }
    };

    // ───────────── Thành viên ─────────────

    const openMembers = async (r: RoleItem) => {
        setMemberRole(r);
        setMembers([]);
        setAssign(EMPTY_ASSIGN);
        setFormError('');
        try {
            const [users] = await Promise.all([userApi.getAll(), loadMembers(r.id)]);
            setAllUsers(users);
        } catch (err: any) {
            setFormError(err.message || 'Không tải được thành viên');
        }
    };

    const handleAssign = async (e: React.FormEvent) => {
        e.preventDefault();
        if (!memberRole) return;
        setFormError('');
        try {
            await assignUser(memberRole.id, {
                userId: assign.userId,
                reason: assign.reason,
                // Ngày hết hạn tính đến cuối ngày đã chọn
                expiresAt: assign.expiresAt ? new Date(`${assign.expiresAt}T23:59:59`).toISOString() : null,
            });
            setAssign(EMPTY_ASSIGN);
        } catch (err: any) {
            setFormError(err.message || 'Gán vai trò thất bại');
        }
    };

    const handleRemove = async (userId: string, username: string) => {
        if (!memberRole) return;
        if (!window.confirm(`Gỡ "${username}" khỏi vai trò ${memberRole.name}?`)) return;
        try { await unassignUser(memberRole.id, userId); } catch (err: any) { setFormError(err.message); }
    };

    // Chỉ cho chọn user chưa thuộc vai trò
    const memberIds = new Set(members.map(m => m.userId));
    const assignableUsers = allUsers.filter(u => !memberIds.has(u.id));

    return (
        <div className="ap-container">
            <div className="ap-header">
                <h1 className="ap-title">Vai trò</h1>
                <p className="ap-subtitle">Nhóm nghiệp vụ (DEV, DBA, KE_TOAN...) dùng để phân quyền vào ứng dụng</p>
            </div>

            <div className="ap-toolbar">
                <SearchBar value={searchTerm} onChange={setSearchTerm} placeholder="Tìm theo mã hoặc tên vai trò..." />
                <button className="ap-btn" onClick={openCreate}><Plus size={16} /> Thêm vai trò</button>
            </div>

            {error && <div className="ap-error">{error}</div>}

            <div className="ap-table-wrapper">
                <table className="ap-table">
                    <thead>
                        <tr>
                            <th>Mã vai trò</th>
                            <th>Tên hiển thị</th>
                            <th>Mô tả</th>
                            <th>Thành viên</th>
                            <th style={{ textAlign: 'right' }}>Thao tác</th>
                        </tr>
                    </thead>
                    <tbody>
                        {isLoading ? (
                            <tr><td colSpan={5} className="ap-empty">Đang tải...</td></tr>
                        ) : roles.length === 0 ? (
                            <tr><td colSpan={5} className="ap-empty">Không tìm thấy vai trò</td></tr>
                        ) : roles.map(r => (
                            <tr key={r.id}>
                                <td><span className="ap-badge blue">{r.name}</span></td>
                                <td><strong>{r.displayName}</strong></td>
                                <td className="ap-muted">{r.description}</td>
                                <td>{r.memberCount}</td>
                                <td>
                                    <div className="ap-actions">
                                        <button className="ap-icon-btn" title="Thành viên" onClick={() => openMembers(r)}><UsersIcon size={16} /></button>
                                        <button className="ap-icon-btn" title="Sửa" onClick={() => openEdit(r)}><Pencil size={16} /></button>
                                        <button className="ap-icon-btn danger" title="Xóa" onClick={() => handleDelete(r)}><Trash2 size={16} /></button>
                                    </div>
                                </td>
                            </tr>
                        ))}
                    </tbody>
                </table>
            </div>

            {/* Thêm / sửa */}
            <Modal isOpen={showForm} onClose={() => setShowForm(false)} title={editing ? `Sửa vai trò ${editing.name}` : 'Thêm vai trò'}>
                <form className="ap-form" onSubmit={handleSave}>
                    {formError && <div className="ap-error">{formError}</div>}
                    <input className="ap-input" placeholder="Mã vai trò (chữ in hoa, số, _) vd: NHAN_SU" value={form.name}
                           onChange={(e) => setForm({ ...form, name: e.target.value })} />
                    <input className="ap-input" placeholder="Tên hiển thị, vd: Nhân sự" value={form.displayName}
                           onChange={(e) => setForm({ ...form, displayName: e.target.value })} />
                    <textarea className="ap-input" style={{ minHeight: 80 }} placeholder="Mô tả" value={form.description}
                              onChange={(e) => setForm({ ...form, description: e.target.value })} />
                    <button type="submit" className="ap-btn">{editing ? 'Lưu thay đổi' : 'Tạo vai trò'}</button>
                </form>
            </Modal>

            {/* Thành viên */}
            <Modal isOpen={memberRole !== null} onClose={() => setMemberRole(null)} title={`Thành viên vai trò ${memberRole?.name ?? ''}`}>
                {formError && <div className="ap-error">{formError}</div>}

                <div className="ap-list">
                    {members.length === 0
                        ? <div className="ap-muted">Chưa có thành viên</div>
                        : members.map(m => (
                            <div key={m.userId} className="ap-list-item">
                                <div>
                                    <strong>{m.username}</strong> — {m.fullName}
                                    <div className="ap-list-sub">Gán bởi {m.grantedBy} · {m.reason} · Hết hạn: {formatDate(m.expiresAt)}</div>
                                </div>
                                <button className="ap-icon-btn danger" title="Gỡ khỏi vai trò" onClick={() => handleRemove(m.userId, m.username)}>
                                    <X size={16} />
                                </button>
                            </div>
                        ))}
                </div>

                <form className="ap-form" onSubmit={handleAssign}>
                    <strong style={{ fontSize: 14 }}>Gán thêm người dùng</strong>
                    <select className="ap-input" value={assign.userId} onChange={(e) => setAssign({ ...assign, userId: e.target.value })}>
                        <option value="">— Chọn người dùng —</option>
                        {assignableUsers.map(u => <option key={u.id} value={u.id}>{u.username} — {u.fullName}</option>)}
                    </select>
                    <input className="ap-input" placeholder="Lý do (bắt buộc)" value={assign.reason}
                           onChange={(e) => setAssign({ ...assign, reason: e.target.value })} />
                    <label className="ap-label">
                        Hết hạn (bỏ trống = không hết hạn)
                        <input className="ap-input" type="date" value={assign.expiresAt}
                               onChange={(e) => setAssign({ ...assign, expiresAt: e.target.value })} />
                    </label>
                    <button type="submit" className="ap-btn" disabled={!assign.userId}>Gán vào vai trò</button>
                </form>
            </Modal>
        </div>
    );
};
