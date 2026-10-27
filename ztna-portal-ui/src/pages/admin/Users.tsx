import { useEffect, useState } from 'react';
import { Plus, Search, Lock, KeyRound, Trash2 } from 'lucide-react';
import PageHeader from '../../components/common/PageHeader';
import DataTable from '../../components/common/DataTable';
import Modal from '../../components/common/Modal';
import { getRequest, postRequest, deleteRequest, patchRequest } from '../../utils/apiClient';
import type { Column } from '../../types';
import type { AdminUser, UserRole } from '../../types/model/User';
import './CommonPages.css';

const EMPTY_FORM = { username: '', fullName: '', email: '', password: '', role: 'USER' as UserRole };

const formatDate = (value: string | null) =>
    value ? new Date(value).toLocaleString('vi-VN') : '—';

const Users: React.FC = () => {
    const [users, setUsers] = useState<AdminUser[]>([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');
    const [search, setSearch] = useState('');

    const [showCreate, setShowCreate] = useState(false);
    const [form, setForm] = useState(EMPTY_FORM);
    const [formError, setFormError] = useState('');
    const [saving, setSaving] = useState(false);

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
    const handleCreate = async (e: React.FormEvent) => {
        e.preventDefault();
        setFormError('');
        setSaving(true);
        try {
            await postRequest('/admin/users', form);
            setShowCreate(false);
            setForm(EMPTY_FORM);
            await loadUsers();
        } catch (err: any) {
            setFormError(err.message || 'Tạo user thất bại');
        } finally {
            setSaving(false);
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
    const filtered = users.filter(u =>
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
            key: 'actions', header: 'Thao tác', width: '110px',
            render: (u) => (
                <div className="actions-cell">
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

    const inputStyle: React.CSSProperties = {
        width: '100%', padding: '10px 12px', border: '1px solid #d1d5db',
        borderRadius: '8px', fontSize: '14px', boxSizing: 'border-box',
    };

    return (
        <div>
            <PageHeader
                title="Người dùng"
                subtitle="Quản lý tài khoản người dùng trong hệ thống"
                actions={
                    <button className="btn-primary" onClick={() => setShowCreate(true)}>
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
                        <option value="USER">USER — Người dùng</option>
                        <option value="SUPERVISOR">SUPERVISOR — Giám sát</option>
                        <option value="ADMIN">ADMIN — Quản trị</option>
                    </select>
                    <button type="submit" className="btn-primary" disabled={saving} style={{ justifyContent: 'center' }}>
                        {saving ? 'Đang lưu...' : 'Tạo người dùng'}
                    </button>
                </form>
            </Modal>
        </div>
    );
};

export default Users;
