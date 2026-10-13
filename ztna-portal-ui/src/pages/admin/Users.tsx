import { useState } from 'react';
import { Plus, Search, Pencil, Trash2 } from 'lucide-react';
import PageHeader from '../../components/common/PageHeader';
import DataTable from '../../components/common/DataTable';
import { mockUsers } from '../../data/mockData';
import type { User, Column } from '../../types';
import './CommonPages.css';

const Users: React.FC = () => {
    const [search, setSearch] = useState('');

    const filtered = mockUsers.filter(u =>
        u.username.toLowerCase().includes(search.toLowerCase()) ||
        u.email.toLowerCase().includes(search.toLowerCase())
    );

    const columns: Column<User>[] = [
        { key: 'id', header: 'ID', width: '60px' },
        { key: 'username', header: 'Username', render: (u) => <strong>{u.username}</strong> },
        { key: 'email', header: 'Email' },
        {
            key: 'enabled', header: 'Trạng thái',
            render: (u) => (
                <span className={`badge ${u.enabled ? 'badge-success' : 'badge-danger'}`}>
                    {u.enabled ? 'Active' : 'Disabled'}
                </span>
            ),
        },
        { key: 'last_login_at', header: 'Last Login', render: (u) => <span>{u.last_login_at || '—'}</span> },
        { key: 'created_at', header: 'Created' },
        {
            key: 'actions', header: 'Actions', width: '100px',
            render: () => (
                <div className="actions-cell">
                    <button className="action-btn" title="Sửa"><Pencil size={16} /></button>
                    <button className="action-btn action-btn-danger" title="Xóa"><Trash2 size={16} /></button>
                </div>
            ),
        },
    ];

    return (
        <div>
            <PageHeader
                title="Người dùng"
                subtitle="Quản lý tài khoản người dùng trong hệ thống"
                actions={
                    <button className="btn-primary">
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
                        placeholder="Tìm kiếm người dùng..."
                        value={search}
                        onChange={(e) => setSearch(e.target.value)}
                    />
                </div>
            </div>
            <DataTable columns={columns} data={filtered} emptyMessage="Không tìm thấy người dùng" />
        </div>
    );
};

export default Users;
