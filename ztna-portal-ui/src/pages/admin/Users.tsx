import { useEffect, useState } from 'react';
import { useSearchParams } from 'react-router-dom';
import { Plus, Search, Lock, KeyRound, Trash2, Pencil, RotateCcw } from 'lucide-react';
import PageHeader from '../../components/common/PageHeader';
import DataTable from '../../components/common/DataTable';
import Modal from '../../components/common/Modal';
import { getRequest, postRequest, putRequest, deleteRequest, patchRequest } from '../../utils/apiClient';
import type { Column } from '../../types';
import type { AdminUser, UserRole } from '../../types/model/User';
import './CommonPages.css';

const EMPTY_FORM = { username: '', fullName: '', email: '', password: '', role: 'USER' as UserRole };

// Tiêu đề trang theo bộ lọc ?role= trên menu
const ROLE_TITLES: Record<UserRole, string> = {
    ADMIN: 'Quản trị viên',
    SUPERVISOR: 'Người giám sát',
    USER: 'Người dùng thường',
};

const inputStyle: React.CSSProperties = {
    width: '100%', padding: '10px 12px', border: '1px solid #d1d5db',
    borderRadius: '8px', fontSize: '14px', boxSizing: 'border-box',
};

const formatDate = (value: string | null) =>
    value ? new Date(value).toLocaleString('vi-VN') : '—';

const isRole = (v: string | null): v is UserRole => v === 'ADMIN' || v === 'SUPERVISOR' || v === 'USER';

const Users: React.FC = () => {
    const [searchParams] = useSearchParams();
    const roleParam = searchParams.get('role');
    const roleFilter: UserRole | null = isRole(roleParam) ? roleParam : null;

    const [users, setUsers] = useState<AdminUser[]>([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');
    const [search, setSearch] = useState('');

    // Thêm user
    const [showCreate, setShowCreate] = useState(false);
    const [form, setForm] = useState(EMPTY_FORM);
    const [formError, setFormError] = useState('');
    const [saving, setSaving] = useState(false);

    // Sửa user
    const [editing, setEditing] = useState<AdminUser | null>(null);
    const [editForm, setEditForm] = useState({ fullName: '', email: '', role: 'USER' as UserRole });
    const [editError, setEditError] = useState('');

    // Đặt lại mật khẩu
    const [resetting, setResetting] = useState<AdminUser | null>(null);
    const [newPassword, setNewPassword] = useState('');
    const [resetError, setResetError] = useState('');

    // GET /admin/users
    const loadUsers = async () => {
        try {
            setLoading(true);
            setError('');
            const data: AdminUser[] = await getRequest('/admin/users');
            setUsers(data);
        } catch (err: any) {
            setError(err.status === 403 ? 'Bạn không có quyền xem trang này' : (err.message || 'Không tải được danh sách'));
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        loadUsers();
    }, []);

    // POST /admin/users
    const openCreate = () => {
        setForm({ ...EMPTY_FORM, role: roleFilter ?? 'USER' });
        setFormError('');
        setShowCreate(true);
    };

    const handleCreate = async (e: React.FormEvent) => {
        e.preventDefault();
        setFormError('');
        setSaving(true);
        try {
            await postRequest('/admin/users', form);
            setShowCreate(false);
            await loadUsers();
        } catch (err: any) {
            setFormError(err.message || 'Tạo user thất bại');
        } finally {
            setSaving(false);
        }
    };

    // PUT /admin/users/{id}
    const openEdit = (u: AdminUser) => {
        setEditing(u);
        setEditForm({ fullName: u.fullName ?? '', email: u.email ?? '', role: u.role });
        setEditError('');
    };

    const handleEdit = async (e: React.FormEvent) => {
        e.preventDefault();
        if (!editing) return;
        setEditError('');
        try {
            await putRequest(`/admin/users/${editing.id}`, editForm);
            setEditing(null);
            await loadUsers();
        } catch (err: any) {
            setEditError(err.message || 'Cập nhật thất bại');
        }
    };

    // PUT /admin/users/{id}/password
    const openReset = (u: AdminUser) => {
        setResetting(u);
        setNewPassword('');
        setResetError('');
    };

    const handleReset = async (e: React.FormEvent) => {
        e.preventDefault();
        if (!resetting) return;
        setResetError('');
        try {
            await putRequest(`/admin/users/${resetting.id}/password`, { password: newPassword });
            setResetting(null);
            alert(`Đã đặt lại mật khẩu cho "${resetting.username}". Các phiên đăng nhập cũ của user này đã bị thu hồi.`);
        } catch (err: any) {
            setResetError(err.message || 'Đặt lại mật khẩu thất bại');
        }
    };

    // PATCH /admin/users/{id}/block | unblock
    const handleToggleBlock = async (u: AdminUser) => {
        const action = u.enabled ? 'block' : 'unblock';
        if (!window.confirm(`${u.enabled ? 'Khóa' : 'Mở khóa'} tài khoản "${u.username}"?`)) return;
        try {
            await patchRequest(`/admin/users/${u.id}/${action}`);
            await loadUsers();
        } catch (err: any) {
            alert(err.message);
        }
    };

    // DELETE /admin/users/{id}
    const handleDelete = async (u: AdminUser) => {
        if (!window.confirm(`Xóa tài khoản "${u.username}"? Không thể hoàn tác.`)) return;
        try {
            await deleteRequest(`/admin/users/${u.id}`);
            await loadUsers();
        } catch (err: any) {
            alert(err.message);
        }
    };

    const keyword = search.toLowerCase();
    const filtered = users
        .filter(u => roleFilter === null || u.role === roleFilter)
        .filter(u =>
            u.username.toLowerCase().includes(keyword) ||
            (u.fullName ?? '').toLowerCase().includes(keyword) ||
            (u.email ?? '').toLowerCase().includes(keyword)
        );

    const columns: Column<AdminUser>[] = [
        { key: 'username', header: 'Username', render: (u) => <strong>{u.username}</strong> },
        { key: 'fullName', header: 'Họ tên' },
        { key: 'email', header: 'Email', render: (u) => <span>{u.email || '—'}</span> },
        {
            key: 'role', header: 'Role',
            render: (u) => (
                <span className={`badge ${u.role === 'ADMIN' ? 'badge-danger' : u.role === 'SUPERVISOR' ? 'badge-warning' : 'badge-info'}`}>
                    {u.role}
                </span>
            ),
        },
        {
            key: 'enabled', header: 'Trạng thái',
            render: (u) => (
                <span className={`badge ${u.enabled ? 'badge-success' : 'badge-muted'}`}>
                    {u.enabled ? 'Hoạt động' : 'Đã khóa'}
                </span>
            ),
        },
        { key: 'lastLoginAt', header: 'Đăng nhập gần nhất', render: (u) => <span>{formatDate(u.lastLoginAt)}</span> },
        {
            key: 'actions', header: 'Thao tác', width: '170px',
            render: (u) => (
                <div className="actions-cell">
                    <button className="action-btn" title="Sửa" onClick={() => openEdit(u)}><Pencil size={16} /></button>
                    <button className="action-btn" title="Đặt lại mật khẩu" onClick={() => openReset(u)}><RotateCcw size={16} /></button>
                    <button className="action-btn" title={u.enabled ? 'Khóa' : 'Mở khóa'} onClick={() => handleToggleBlock(u)}>
                        {u.enabled ? <Lock size={16} /> : <KeyRound size={16} />}
                    </button>
                    <button className="action-btn action-btn-danger" title="Xóa" onClick={() => handleDelete(u)}>
                        <Trash2 size={16} />
                    </button>
                </div>
            ),
        },
    ];

    return (
        <div>
            <PageHeader
                title={roleFilter ? ROLE_TITLES[roleFilter] : 'Người dùng'}
                subtitle={roleFilter ? `Tài khoản có quyền hệ thống ${roleFilter}` : 'Quản lý tài khoản người dùng trong hệ thống'}
                actions={
                    <button className="btn-primary" onClick={openCreate}>
                        <Plus size={16} />
                        <span>Thêm người dùng</span>
                    </button>
                }
            />
            <div className="page-search">
                <div className="search-wrapper">
                    <Search size={18} className="search-icon" />
                    <input
                        type="text"
                        className="search-input"
                        placeholder="Tìm theo username, họ tên, email..."
                        value={search}
                        onChange={(e) => setSearch(e.target.value)}
                    />
                </div>
            </div>

            {error && <div style={{ color: '#dc2626', marginBottom: '12px' }}>{error}</div>}

            {loading
                ? <div style={{ padding: '24px', color: '#6b7280' }}>Đang tải...</div>
                : <DataTable columns={columns} data={filtered} emptyMessage="Không tìm thấy người dùng" />}

            {/* Thêm user */}
            <Modal isOpen={showCreate} onClose={() => setShowCreate(false)} title="Thêm người dùng">
                <form onSubmit={handleCreate} style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
                    {formError && <div style={{ color: '#dc2626', fontSize: '14px' }}>{formError}</div>}
                    <input style={inputStyle} placeholder="Username (3-50 ký tự)" value={form.username}
                           onChange={(e) => setForm({ ...form, username: e.target.value })} />
                    <input style={inputStyle} placeholder="Họ tên" value={form.fullName}
                           onChange={(e) => setForm({ ...form, fullName: e.target.value })} />
                    <input style={inputStyle} placeholder="Email (không bắt buộc)" value={form.email}
                           onChange={(e) => setForm({ ...form, email: e.target.value })} />
                    <input style={inputStyle} type="password" placeholder="Mật khẩu (tối thiểu 6 ký tự)" value={form.password}
                           onChange={(e) => setForm({ ...form, password: e.target.value })} />
                    <select style={inputStyle} value={form.role}
                            onChange={(e) => setForm({ ...form, role: e.target.value as UserRole })}>
                        <option value="USER">USER — Người dùng thường</option>
                        <option value="SUPERVISOR">SUPERVISOR — Giám sát</option>
                        <option value="ADMIN">ADMIN — Quản trị</option>
                    </select>
                    <button type="submit" className="btn-primary" disabled={saving} style={{ justifyContent: 'center' }}>
                        {saving ? 'Đang lưu...' : 'Tạo người dùng'}
                    </button>
                </form>
            </Modal>

            {/* Sửa user */}
            <Modal isOpen={editing !== null} onClose={() => setEditing(null)} title={`Sửa người dùng ${editing?.username ?? ''}`}>
                <form onSubmit={handleEdit} style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
                    {editError && <div style={{ color: '#dc2626', fontSize: '14px' }}>{editError}</div>}
                    <input style={inputStyle} placeholder="Họ tên" value={editForm.fullName}
                           onChange={(e) => setEditForm({ ...editForm, fullName: e.target.value })} />
                    <input style={inputStyle} placeholder="Email (không bắt buộc)" value={editForm.email}
                           onChange={(e) => setEditForm({ ...editForm, email: e.target.value })} />
                    <select style={inputStyle} value={editForm.role}
                            onChange={(e) => setEditForm({ ...editForm, role: e.target.value as UserRole })}>
                        <option value="USER">USER — Người dùng thường</option>
                        <option value="SUPERVISOR">SUPERVISOR — Giám sát</option>
                        <option value="ADMIN">ADMIN — Quản trị</option>
                    </select>
                    <button type="submit" className="btn-primary" style={{ justifyContent: 'center' }}>Lưu thay đổi</button>
                </form>
            </Modal>

            {/* Đặt lại mật khẩu */}
            <Modal isOpen={resetting !== null} onClose={() => setResetting(null)} title={`Đặt lại mật khẩu cho ${resetting?.username ?? ''}`}>
                <form onSubmit={handleReset} style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
                    {resetError && <div style={{ color: '#dc2626', fontSize: '14px' }}>{resetError}</div>}
                    <input style={inputStyle} type="password" placeholder="Mật khẩu mới (tối thiểu 6 ký tự)" value={newPassword}
                           onChange={(e) => setNewPassword(e.target.value)} />
                    <button type="submit" className="btn-primary" style={{ justifyContent: 'center' }}>Đặt lại mật khẩu</button>
                </form>
            </Modal>
        </div>
    );
};

export default Users;
