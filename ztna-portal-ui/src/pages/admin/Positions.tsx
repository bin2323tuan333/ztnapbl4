import { useState } from 'react';
import { Plus, Search, Pencil, Trash2 } from 'lucide-react';
import PageHeader from '../../components/common/PageHeader';
import DataTable from '../../components/common/DataTable';
import { mockPositions } from '../../data/mockData';
import type { Position, Column } from '../../types';
import './CommonPages.css';

const Positions: React.FC = () => {
    const [search, setSearch] = useState('');

    const filtered = mockPositions.filter(p =>
        p.name.toLowerCase().includes(search.toLowerCase()) ||
        p.display_name.toLowerCase().includes(search.toLowerCase())
    );

    const columns: Column<Position>[] = [
        { key: 'id', header: 'ID', width: '60px' },
        { key: 'name', header: 'Mã chức vụ', render: (p) => <strong>{p.name}</strong> },
        { key: 'display_name', header: 'Tên hiển thị' },
        {
            key: 'enabled', header: 'Trạng thái',
            render: (p) => (
                <span className={`badge ${p.enabled ? 'badge-success' : 'badge-danger'}`}>
                    {p.enabled ? 'Active' : 'Disabled'}
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
                title="Chức vụ"
                subtitle="Quản lý chức vụ trong tổ chức"
                actions={
                    <button className="btn-primary">
                        <Plus size={16} />
                        <span>Thêm chức vụ</span>
                    </button>
                }
            />
            <div className="page-search">
                <div className="search-wrapper">
                    <Search size={18} className="search-icon" />
                    <input
                        type="text"
                        className="search-input"
                        placeholder="Tìm kiếm chức vụ..."
                        value={search}
                        onChange={(e) => setSearch(e.target.value)}
                    />
                </div>
            </div>
            <DataTable columns={columns} data={filtered} emptyMessage="Không tìm thấy chức vụ" />
        </div>
    );
};

export default Positions;
