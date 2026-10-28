import { useEffect, useState } from 'react';
import { Plus, Search, Pencil, Trash2, Users as UsersIcon, X } from 'lucide-react';
import PageHeader from '../../components/common/PageHeader';
import DataTable from '../../components/common/DataTable';
import Modal from '../../components/common/Modal';
import { getRequest, postRequest, putRequest, deleteRequest } from '../../utils/apiClient';
import type { Column } from '../../types';
import type { RoleItem, RoleMember } from '../../types/model/Role';
import type { AdminUser } from '../../types/model/User';
import './CommonPages.css';

const EMPTY_ROLE = { name: '', displayName: '', description: '' };
const EMPTY_ASSIGN = { userId: '', reason: '', expiresAt: '' };

const inputStyle: React.CSSProperties = {
    width: '100%', padding: '10px 12px', border: '1px solid #d1d5db',
    borderRadius: '8px', fontSize: '14px', boxSizing: 'border-box',
};

const formatDate = (value: string | null) =>
    value ? new Date(value).toLocaleString('vi-VN') : 'Không hết hạn';

const Roles: React.FC = () => {
    const [roles, setRoles] = useState<RoleItem[]>([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');
    const [search, setSearch] = useState('');

    // Form thêm / sửa role (editing = null nghĩa là đang thêm mới)
    const [showForm, setShowForm] = useState(false);
    const [editing, setEditing] = useState<RoleItem | null>(null);
    const [form, setForm] = useState(EMPTY_ROLE);
    const [formError, setFormError] = useState('');

    // Hộp thoại thành viên
    const [memberRole, setMemberRole] = useState<RoleItem | null>(null);
    const [members, setMembers] = useState<RoleMember[]>([]);
    const [allUsers, setAllUsers] = useState<AdminUser[]>([]);
    const [assign, setAssign] = useState(EMPTY_ASSIGN);
    const [memberError, setMemberError] = useState('');

    // GET /admin/roles
    const loadRoles = async () => {
        try {
            setLoading(true);
            setError('');
            const data: RoleItem[] = await getRequest('/admin/roles');
            setRoles(data);
        } catch (err: any) {
            setError(err.message || 'Không tải được danh sách vai trò');
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        loadRoles();
    }, []);

    // ───────────── Thêm / sửa / xóa role ─────────────

    const openCreate = () => {
        setEditing(null);
        setForm(EMPTY_ROLE);
        setFormError('');
        setShowForm(true);
    };

    const openEdit = (r: RoleItem) => {
        setEditing(r);
        setForm({ name: r.name, displayName: r.displayName, description: r.description });
        setFormError('');
        setShowForm(true);
    };

    // POST /admin/roles  hoặc  PUT /admin/roles/{id}
    const handleSave = async (e: React.FormEvent) => {
        e.preventDefault();
        setFormError('');
        try {
            const body = { ...form, name: form.name.trim().toUpperCase() };
            if (editing) {
                await putRequest(`/admin/roles/${editing.id}`, body);
            } else {
                await postRequest('/admin/roles', body);
            }
            setShowForm(false);
            await loadRoles();
        } catch (err: any) {
            setFormError(err.message || 'Lưu vai trò thất bại');
        }
    };

    // DELETE /admin/roles/{id}
    const handleDelete = async (r: RoleItem) => {
        if (!window.confirm(`Xóa vai trò "${r.name}"?`)) return;
        try {
            await deleteRequest(`/admin/roles/${r.id}`);
            await loadRoles();
        } catch (err: any) {
            alert(err.message);
        }
    };

    // ───────────── Thành viên của role ─────────────

    const loadMembers = async (roleId: string) => {
        const data: RoleMember[] = await getRequest(`/admin/roles/${roleId}/users`);
        setMembers(data);
    };

    const openMembers = async (r: RoleItem) => {
        setMemberRole(r);
        setMembers([]);
        setAssign(EMPTY_ASSIGN);
        setMemberError('');
        try {
            const [users] = await Promise.all([getRequest('/admin/users'), loadMembers(r.id)]);
            setAllUsers(users);
        } catch (err: any) {
            setMemberError(err.message || 'Không tải được thành viên');
        }
    };

    // POST /admin/roles/{id}/users
    const handleAssign = async (e: React.FormEvent) => {
        e.preventDefault();
        if (!memberRole) return;
        setMemberError('');
        try {
            await postRequest(`/admin/roles/${memberRole.id}/users`, {
                userId: assign.userId,
                reason: assign.reason,
                // Ngày hết hạn tính đến cuối ngày đã chọn
                expiresAt: assign.expiresAt ? new Date(`${assign.expiresAt}T23:59:59`).toISOString() : null,
            });
            setAssign(EMPTY_ASSIGN);
            await loadMembers(memberRole.id);
            await loadRoles();
        } catch (err: any) {
            setMemberError(err.message || 'Gán vai trò thất bại');
        }
    };

    // DELETE /admin/roles/{id}/users/{userId}
    const handleRemove = async (m: RoleMember) => {
        if (!memberRole) return;
        if (!window.confirm(`Gỡ "${m.username}" khỏi vai trò ${memberRole.name}?`)) return;
        try {
            await deleteRequest(`/admin/roles/${memberRole.id}/users/${m.userId}`);
            await loadMembers(memberRole.id);
            await loadRoles();
        } catch (err: any) {
            setMemberError(err.message);
        }
    };

    // ───────────── Bảng ─────────────

    const keyword = search.toLowerCase();
    const filtered = roles.filter(r =>
        r.name.toLowerCase().includes(keyword) ||
        r.displayName.toLowerCase().includes(keyword)
    );

    const columns: Column<RoleItem>[] = [
        { key: 'name', header: 'Mã vai trò', render: (r) => <span className="badge badge-info">{r.name}</span> },
        { key: 'displayName', header: 'Tên hiển thị', render: (r) => <strong>{r.displayName}</strong> },
        { key: 'description', header: 'Mô tả' },
        { key: 'memberCount', header: 'Thành viên', width: '110px' },
        {
            key: 'actions', header: 'Thao tác', width: '140px',
            render: (r) => (
                <div className="actions-cell">
                    <button className="action-btn" title="Thành viên" onClick={() => openMembers(r)}><UsersIcon size={16} /></button>
                    <button className="action-btn" title="Sửa" onClick={() => openEdit(r)}><Pencil size={16} /></button>
                    <button className="action-btn action-btn-danger" title="Xóa" onClick={() => handleDelete(r)}><Trash2 size={16} /></button>
                </div>
            ),
        },
    ];

    // User chưa thuộc role — để chọn khi gán
    const memberIds = new Set(members.map(m => m.userId));
    const assignableUsers = allUsers.filter(u => !memberIds.has(u.id));

    return (
        <div>
            <PageHeader
                title="Vai trò"
                subtitle="Nhóm nghiệp vụ (DEV, DBA, KE_TOAN...) dùng để phân quyền vào ứng dụng"
                actions={
                    <button className="btn-primary" onClick={openCreate}>
                        <Plus size={16} />
                        <span>Thêm vai trò</span>
                    </button>
                }
            />
            <div className="page-search">
                <div className="search-wrapper">
                    <Search size={18} className="search-icon" />
                    <input
                        type="text"
                        className="search-input"
                        placeholder="Tìm theo mã hoặc tên vai trò..."
                        value={search}
                        onChange={(e) => setSearch(e.target.value)}
                    />
                </div>
            </div>

            {error && <div style={{ color: '#dc2626', marginBottom: '12px' }}>{error}</div>}

            {loading
                ? <div style={{ padding: '24px', color: '#6b7280' }}>Đang tải...</div>
                : <DataTable columns={columns} data={filtered} emptyMessage="Không tìm thấy vai trò" />}

            {/* Form thêm / sửa */}
            <Modal isOpen={showForm} onClose={() => setShowForm(false)} title={editing ? `Sửa vai trò ${editing.name}` : 'Thêm vai trò'}>
                <form onSubmit={handleSave} style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
                    {formError && <div style={{ color: '#dc2626', fontSize: '14px' }}>{formError}</div>}
                    <input style={inputStyle} placeholder="Mã vai trò (chữ in hoa, số, _ ) vd: NHAN_SU" value={form.name}
                           onChange={(e) => setForm({ ...form, name: e.target.value })} />
                    <input style={inputStyle} placeholder="Tên hiển thị, vd: Nhân sự" value={form.displayName}
                           onChange={(e) => setForm({ ...form, displayName: e.target.value })} />
                    <textarea style={{ ...inputStyle, minHeight: '80px', fontFamily: 'inherit' }} placeholder="Mô tả" value={form.description}
                              onChange={(e) => setForm({ ...form, description: e.target.value })} />
                    <button type="submit" className="btn-primary" style={{ justifyContent: 'center' }}>
                        {editing ? 'Lưu thay đổi' : 'Tạo vai trò'}
                    </button>
                </form>
            </Modal>

            {/* Thành viên */}
            <Modal isOpen={memberRole !== null} onClose={() => setMemberRole(null)} title={`Thành viên vai trò ${memberRole?.name ?? ''}`}>
                {memberError && <div style={{ color: '#dc2626', fontSize: '14px', marginBottom: '12px' }}>{memberError}</div>}

                <div style={{ maxHeight: '220px', overflowY: 'auto', marginBottom: '16px' }}>
                    {members.length === 0
                        ? <div style={{ color: '#9ca3af', fontSize: '14px' }}>Chưa có thành viên</div>
                        : members.map(m => (
                            <div key={m.userId} style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', padding: '8px 0', borderBottom: '1px solid #f3f4f6' }}>
                                <div style={{ fontSize: '14px' }}>
                                    <strong>{m.username}</strong> — {m.fullName}
                                    <div style={{ color: '#6b7280', fontSize: '12px' }}>
                                        Gán bởi {m.grantedBy} · {m.reason} · Hết hạn: {formatDate(m.expiresAt)}
                                    </div>
                                </div>
                                <button className="action-btn action-btn-danger" title="Gỡ khỏi vai trò" onClick={() => handleRemove(m)}>
                                    <X size={16} />
                                </button>
                            </div>
                        ))}
                </div>

                <form onSubmit={handleAssign} style={{ display: 'flex', flexDirection: 'column', gap: '10px' }}>
                    <strong style={{ fontSize: '14px' }}>Gán thêm người dùng</strong>
                    <select style={inputStyle} value={assign.userId} onChange={(e) => setAssign({ ...assign, userId: e.target.value })}>
                        <option value="">— Chọn người dùng —</option>
                        {assignableUsers.map(u => (
                            <option key={u.id} value={u.id}>{u.username} — {u.fullName}</option>
                        ))}
                    </select>
                    <input style={inputStyle} placeholder="Lý do (bắt buộc)" value={assign.reason}
                           onChange={(e) => setAssign({ ...assign, reason: e.target.value })} />
                    <label style={{ fontSize: '13px', color: '#6b7280' }}>
                        Hết hạn (bỏ trống = không hết hạn)
                        <input style={{ ...inputStyle, marginTop: '4px' }} type="date" value={assign.expiresAt}
                               onChange={(e) => setAssign({ ...assign, expiresAt: e.target.value })} />
                    </label>
                    <button type="submit" className="btn-primary" disabled={!assign.userId} style={{ justifyContent: 'center' }}>
                        Gán vào vai trò
                    </button>
                </form>
            </Modal>
        </div>
    );
};

export default Roles;
