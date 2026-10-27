import { useState } from 'react';
import { Plus, Search, Pencil, Trash2 } from 'lucide-react';
import PageHeader from '../../components/common/PageHeader';
import DataTable from '../../components/common/DataTable';
import { mockResources } from '../../data/mockData';
import type { Resource, Column } from '../../types';
import './CommonPages.css';

const Resources: React.FC = () => {
    const [search, setSearch] = useState('');

    const filtered = mockResources.filter(r =>
        r.name.toLowerCase().includes(search.toLowerCase()) ||
        r.description.toLowerCase().includes(search.toLowerCase())
    );

    const columns: Column<Resource>[] = [
        { key: 'id', header: 'ID', width: '60px' },
        { key: 'name', header: 'Tên', render: (r) => <strong>{r.name}</strong> },
        { key: 'description', header: 'Mô tả' },
        {
            key: 'connector_zone', header: 'Connector Zone',
            render: (r) => <span className="badge badge-info">{r.connector_zone}</span>,
        },
        {
            key: 'backend_url', header: 'Backend URL',
            render: (r) => <code style={{ fontSize: '12px', background: '#f3f4f6', padding: '2px 6px', borderRadius: '4px' }}>{r.backend_url}</code>,
        },
        {
            key: 'enabled', header: 'Enabled',
            render: (r) => (
                <span className={`badge ${r.enabled ? 'badge-success' : 'badge-danger'}`}>
                    {r.enabled ? 'Yes' : 'No'}
                </span>
            ),
        },
        { key: 'created_at', header: 'Ngày tạo' },
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
                title="Tài nguyên"
                subtitle="Quản lý tài nguyên được bảo vệ"
                actions={
                    <button className="btn-primary">
                        <Plus size={16} />
                        <span>Thêm tài nguyên</span>
                    </button>
                }
            />
            <div className="page-search">
                <div className="search-wrapper">
                    <Search size={18} className="search-icon" />
                    <input
                        type="text"
                        className="search-input"
                        placeholder="Tìm kiếm tài nguyên..."
                        value={search}
                        onChange={(e) => setSearch(e.target.value)}
                    />
                </div>
            </div>
            <DataTable columns={columns} data={filtered} emptyMessage="Không tìm thấy tài nguyên" />
        </div>
    );
};

export default Resources;
