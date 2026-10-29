import { useState } from 'react';
import { useSearchParams } from 'react-router-dom';
import { Plus, Lock, KeyRound, Trash2, Pencil, RotateCcw } from 'lucide-react';
import Modal from '@/components/common/Modal';
import { SearchBar } from '@/components/common/SearchBar';
import type { User, UserRole } from '@/shared/types/model/User';
import { useUsers } from '../hooks/useUsers';
import '@/shared/styles/AdminPage.css';

// Tiêu đề trang theo bộ lọc ?role= trên menu
const ROLE_TITLES: Record<UserRole, string> = {
    ADMIN: 'Quản trị viên',
    SUPERVISOR: 'Người giám sát',
    USER: 'Người dùng thường',
};
const ROLE_BADGE: Record<UserRole, string> = { ADMIN: 'red', SUPERVISOR: 'yellow', USER: 'blue' };

const EMPTY_CREATE = { username: '', fullName: '', email: '', password: '', role: 'USER' as UserRole };

const isRole = (v: string | null): v is UserRole => v === 'ADMIN' || v === 'SUPERVISOR' || v === 'USER';
const formatDate = (v: string | null) => (v ? new Date(v).toLocaleString('vi-VN') : '—');

const RoleSelect = ({ value, onChange }: { value: UserRole; onChange: (r: UserRole) => void }) => (
    <select className="ap-input" value={value} onChange={(e) => onChange(e.target.value as UserRole)}>
        <option value="USER">USER — Người dùng thường</option>
        <option value="SUPERVISOR">SUPERVISOR — Giám sát</option>
        <option value="ADMIN">ADMIN — Quản trị</option>
    </select>
);

export const UsersPage = () => {
    const [searchParams] = useSearchParams();
    const roleParam = searchParams.get('role');
    const roleFilter: UserRole | null = isRole(roleParam) ? roleParam : null;

    const { users, isLoading, error, searchTerm, setSearchTerm,
        createUser, updateUser, resetPassword, toggleBlock, deleteUser } = useUsers(roleFilter);

    // Thêm
    const [showCreate, setShowCreate] = useState(false);
    const [createForm, setCreateForm] = useState(EMPTY_CREATE);
    // Sửa
    const [editing, setEditing] = useState<User | null>(null);
    const [editForm, setEditForm] = useState({ fullName: '', email: '', role: 'USER' as UserRole });
    // Đặt lại mật khẩu
    const [resetting, setResetting] = useState<User | null>(null);
    const [newPassword, setNewPassword] = useState('');
    // Lỗi trong modal đang mở
    const [formError, setFormError] = useState('');
    const [saving, setSaving] = useState(false);

    // Chạy 1 thao tác trong modal: bật "đang lưu", bắt lỗi hiện lên modal
    const run = async (action: () => Promise<unknown>, onDone: () => void) => {
        setFormError('');
        setSaving(true);
        try {
            await action();
            onDone();
        } catch (err: any) {
            setFormError(err.message || 'Thao tác thất bại');
        } finally {
            setSaving(false);
        }
    };

    const openCreate = () => {
        setCreateForm({ ...EMPTY_CREATE, role: roleFilter ?? 'USER' });
        setFormError('');
        setShowCreate(true);
    };
    const openEdit = (u: User) => {
        setEditing(u);
        setEditForm({ fullName: u.fullName ?? '', email: u.email ?? '', role: u.role });
        setFormError('');
    };
    const openReset = (u: User) => {
        setResetting(u);
        setNewPassword('');
        setFormError('');
    };

    const handleToggleBlock = async (u: User) => {
        if (!window.confirm(`${u.enabled ? 'Khóa' : 'Mở khóa'} tài khoản "${u.username}"?`)) return;
        try { await toggleBlock(u); } catch (err: any) { alert(err.message); }
    };
    const handleDelete = async (u: User) => {
        if (!window.confirm(`Xóa tài khoản "${u.username}"? Không thể hoàn tác.`)) return;
        try { await deleteUser(u.id); } catch (err: any) { alert(err.message); }
    };

    return (
        <div className="ap-container">
            <div className="ap-header">
                <h1 className="ap-title">{roleFilter ? ROLE_TITLES[roleFilter] : 'Người dùng'}</h1>
                <p className="ap-subtitle">
                    {roleFilter ? `Tài khoản có quyền hệ thống ${roleFilter}` : 'Quản lý tài khoản người dùng trong hệ thống'}
                </p>
            </div>

            <div className="ap-toolbar">
                <SearchBar value={searchTerm} onChange={setSearchTerm} placeholder="Tìm theo username, họ tên, email..." />
                <button className="ap-btn" onClick={openCreate}><Plus size={16} /> Thêm người dùng</button>
            </div>

            {error && <div className="ap-error">{error}</div>}

            <div className="ap-table-wrapper">
                <table className="ap-table">
                    <thead>
                        <tr>
                            <th>Username</th>
                            <th>Họ tên</th>
                            <th>Email</th>
                            <th>Role</th>
                            <th>Trạng thái</th>
                            <th>Đăng nhập gần nhất</th>
                            <th style={{ textAlign: 'right' }}>Thao tác</th>
                        </tr>
                    </thead>
                    <tbody>
                        {isLoading ? (
                            <tr><td colSpan={7} className="ap-empty">Đang tải...</td></tr>
                        ) : users.length === 0 ? (
                            <tr><td colSpan={7} className="ap-empty">Không tìm thấy người dùng</td></tr>
                        ) : users.map(u => (
                            <tr key={u.id}>
                                <td><strong>{u.username}</strong></td>
                                <td>{u.fullName}</td>
                                <td className="ap-muted">{u.email || '—'}</td>
                                <td><span className={`ap-badge ${ROLE_BADGE[u.role]}`}>{u.role}</span></td>
                                <td>
                                    <span className={`ap-badge ${u.enabled ? 'green' : 'gray'}`}>
                                        {u.enabled ? 'Hoạt động' : 'Đã khóa'}
                                    </span>
                                </td>
                                <td className="ap-muted">{formatDate(u.lastLoginAt)}</td>
                                <td>
                                    <div className="ap-actions">
                                        <button className="ap-icon-btn" title="Sửa" onClick={() => openEdit(u)}><Pencil size={16} /></button>
                                        <button className="ap-icon-btn" title="Đặt lại mật khẩu" onClick={() => openReset(u)}><RotateCcw size={16} /></button>
                                        <button className="ap-icon-btn" title={u.enabled ? 'Khóa' : 'Mở khóa'} onClick={() => handleToggleBlock(u)}>
                                            {u.enabled ? <Lock size={16} /> : <KeyRound size={16} />}
                                        </button>
                                        <button className="ap-icon-btn danger" title="Xóa" onClick={() => handleDelete(u)}><Trash2 size={16} /></button>
                                    </div>
                                </td>
                            </tr>
                        ))}
                    </tbody>
                </table>
            </div>

            {/* Thêm người dùng */}
            <Modal isOpen={showCreate} onClose={() => setShowCreate(false)} title="Thêm người dùng">
                <form className="ap-form" onSubmit={(e) => { e.preventDefault(); run(() => createUser(createForm), () => setShowCreate(false)); }}>
                    {formError && <div className="ap-error">{formError}</div>}
                    <input className="ap-input" placeholder="Username (3-50 ký tự)" value={createForm.username}
                           onChange={(e) => setCreateForm({ ...createForm, username: e.target.value })} />
                    <input className="ap-input" placeholder="Họ tên" value={createForm.fullName}
                           onChange={(e) => setCreateForm({ ...createForm, fullName: e.target.value })} />
                    <input className="ap-input" placeholder="Email (không bắt buộc)" value={createForm.email}
                           onChange={(e) => setCreateForm({ ...createForm, email: e.target.value })} />
                    <input className="ap-input" type="password" placeholder="Mật khẩu (tối thiểu 6 ký tự)" value={createForm.password}
                           onChange={(e) => setCreateForm({ ...createForm, password: e.target.value })} />
                    <RoleSelect value={createForm.role} onChange={(role) => setCreateForm({ ...createForm, role })} />
                    <button type="submit" className="ap-btn" disabled={saving}>{saving ? 'Đang lưu...' : 'Tạo người dùng'}</button>
                </form>
            </Modal>

            {/* Sửa người dùng */}
            <Modal isOpen={editing !== null} onClose={() => setEditing(null)} title={`Sửa người dùng ${editing?.username ?? ''}`}>
                <form className="ap-form" onSubmit={(e) => { e.preventDefault(); if (editing) run(() => updateUser(editing.id, editForm), () => setEditing(null)); }}>
                    {formError && <div className="ap-error">{formError}</div>}
                    <input className="ap-input" placeholder="Họ tên" value={editForm.fullName}
                           onChange={(e) => setEditForm({ ...editForm, fullName: e.target.value })} />
                    <input className="ap-input" placeholder="Email (không bắt buộc)" value={editForm.email}
                           onChange={(e) => setEditForm({ ...editForm, email: e.target.value })} />
                    <RoleSelect value={editForm.role} onChange={(role) => setEditForm({ ...editForm, role })} />
                    <button type="submit" className="ap-btn" disabled={saving}>Lưu thay đổi</button>
                </form>
            </Modal>

            {/* Đặt lại mật khẩu */}
            <Modal isOpen={resetting !== null} onClose={() => setResetting(null)} title={`Đặt lại mật khẩu cho ${resetting?.username ?? ''}`}>
                <form className="ap-form" onSubmit={(e) => {
                    e.preventDefault();
                    if (!resetting) return;
                    run(() => resetPassword(resetting.id, newPassword), () => {
                        alert(`Đã đặt lại mật khẩu cho "${resetting.username}". Các phiên đăng nhập cũ đã bị thu hồi.`);
                        setResetting(null);
                    });
                }}>
                    {formError && <div className="ap-error">{formError}</div>}
                    <input className="ap-input" type="password" placeholder="Mật khẩu mới (tối thiểu 6 ký tự)" value={newPassword}
                           onChange={(e) => setNewPassword(e.target.value)} />
                    <button type="submit" className="ap-btn" disabled={saving}>Đặt lại mật khẩu</button>
                </form>
            </Modal>
        </div>
    );
};
