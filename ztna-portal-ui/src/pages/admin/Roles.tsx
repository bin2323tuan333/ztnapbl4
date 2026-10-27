import { useState } from 'react';
import { Plus, Search, Pencil, Trash2 } from 'lucide-react';
import PageHeader from '../../components/common/PageHeader';
import DataTable from '../../components/common/DataTable';
import { mockRoles } from '../../data/mockData';
import type { SystemRole, Column } from '../../types';
import './CommonPages.css';

const Roles: React.FC = () => {
    const [search, setSearch] = useState('');

    const filtered = mockRoles.filter(r =>
        r.name.toLowerCase().includes(search.toLowerCase()) ||
        r.display_name.toLowerCase().includes(search.toLowerCase())
    );

    const columns: Column<SystemRole>[] = [
        { key: 'id', header: 'ID', width: '60px' },
        { key: 'name', header: 'Tên vai trò', render: (r) => <span className="badge badge-info">{r.name}</span> },
        { key: 'display_name', header: 'Tên hiển thị', render: (r) => <strong>{r.display_name}</strong> },
        { key: 'description', header: 'Mô tả' },
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
                title="Vai trò"
                subtitle="Quản lý vai trò hệ thống"
                actions={
                    <button className="btn-primary">
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
                        placeholder="Tìm kiếm vai trò..."
                        value={search}
                        onChange={(e) => setSearch(e.target.value)}
                    />
                </div>
            </div>
            <DataTable columns={columns} data={filtered} emptyMessage="Không tìm thấy vai trò" />
        </div>
    );
};

export default Roles;
