import { useState } from 'react';
import { Plus, Search, Pencil, Trash2 } from 'lucide-react';
import PageHeader from '../../components/common/PageHeader';
import DataTable from '../../components/common/DataTable';
import { mockUserPermissions } from '../../data/mockData';
import type { UserPermission, Column } from '../../types';
import './CommonPages.css';

const Permissions: React.FC = () => {
    const [search, setSearch] = useState('');

    const filtered = mockUserPermissions.filter(p =>
        p.username.toLowerCase().includes(search.toLowerCase()) ||
        p.resource_name.toLowerCase().includes(search.toLowerCase())
    );

    const columns: Column<UserPermission>[] = [
        { key: 'id', header: 'ID', width: '60px' },
        { key: 'username', header: 'Username', render: (p) => <strong>{p.username}</strong> },
        { key: 'resource_name', header: 'Tài nguyên' },
        { key: 'granted_by', header: 'Cấp bởi' },
        { key: 'granted_at', header: 'Ngày cấp' },
        { key: 'expires_at', header: 'Hết hạn', render: (p) => <span>{p.expires_at || 'Không giới hạn'}</span> },
        { key: 'reason', header: 'Lý do' },
        {
            key: 'enabled', header: 'Trạng thái',
            render: (p) => (
                <span className={`badge ${p.enabled ? 'badge-success' : 'badge-danger'}`}>
                    {p.enabled ? 'Active' : 'Disabled'}
                </span>
            ),
        },
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
                title="Cấp quyền"
                subtitle="Quản lý quyền truy cập tài nguyên"
                actions={
                    <button className="btn-primary">
                        <Plus size={16} />
                        <span>Cấp quyền mới</span>
                    </button>
                }
            />
            <div className="page-search">
                <div className="search-wrapper">
                    <Search size={18} className="search-icon" />
                    <input
                        type="text"
                        className="search-input"
                        placeholder="Tìm kiếm quyền truy cập..."
                        value={search}
                        onChange={(e) => setSearch(e.target.value)}
                    />
                </div>
            </div>
            <DataTable columns={columns} data={filtered} emptyMessage="Không tìm thấy quyền truy cập" />
        </div>
    );
};

export default Permissions;
