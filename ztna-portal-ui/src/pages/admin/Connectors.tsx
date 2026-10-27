import { useState } from 'react';
import { Plus, Search, Pencil, Trash2, Wifi, WifiOff, AlertTriangle } from 'lucide-react';
import PageHeader from '../../components/common/PageHeader';
import DataTable from '../../components/common/DataTable';
import { mockConnectors } from '../../data/mockData';
import type { Connector, Column } from '../../types';
import './CommonPages.css';

const statusConfig = {
    online: { icon: <Wifi size={14} />, className: 'status-online', label: 'Online' },
    offline: { icon: <WifiOff size={14} />, className: 'status-offline', label: 'Offline' },
    degraded: { icon: <AlertTriangle size={14} />, className: 'status-degraded', label: 'Degraded' },
};

const Connectors: React.FC = () => {
    const [search, setSearch] = useState('');

    const filtered = mockConnectors.filter(c =>
        c.name.toLowerCase().includes(search.toLowerCase()) ||
        c.connector_zone.toLowerCase().includes(search.toLowerCase())
    );

    const columns: Column<Connector>[] = [
        { key: 'id', header: 'ID', width: '60px' },
        { key: 'name', header: 'Tên', render: (c) => <strong>{c.name}</strong> },
        { key: 'connector_zone', header: 'Zone', render: (c) => <span className="badge badge-info">{c.connector_zone}</span> },
        {
            key: 'status', header: 'Trạng thái',
            render: (c) => {
                const config = statusConfig[c.status];
                return (
                    <span className={`status-indicator ${config.className}`}>
                        {config.icon}
                        {config.label}
                    </span>
                );
            },
        },
        { key: 'last_seen', header: 'Last Seen' },
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
                title="Connectors"
                subtitle="Quản lý kết nối đến các data center"
                actions={
                    <button className="btn-primary">
                        <Plus size={16} />
                        <span>Thêm connector</span>
                    </button>
                }
            />
            <div className="page-search">
                <div className="search-wrapper">
                    <Search size={18} className="search-icon" />
                    <input
                        type="text"
                        className="search-input"
                        placeholder="Tìm kiếm connector..."
                        value={search}
                        onChange={(e) => setSearch(e.target.value)}
                    />
                </div>
            </div>
            <DataTable columns={columns} data={filtered} emptyMessage="Không tìm thấy connector" />
        </div>
    );
};

export default Connectors;
