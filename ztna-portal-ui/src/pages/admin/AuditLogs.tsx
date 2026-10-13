import React, { useState } from 'react';
import { Search } from 'lucide-react';
import PageHeader from '../../components/common/PageHeader';
import DataTable from '../../components/common/DataTable';
import { mockAuditLogs } from '../../data/mockData';
import type { AuditLog, Column } from '../../types';
import './CommonPages.css';

const actionBadgeMap: Record<string, string> = {
    'CREATE_USER': 'badge-success',
    'GRANT_PERMISSION': 'badge-info',
    'UPDATE_RESOURCE': 'badge-warning',
    'DELETE_PERMISSION': 'badge-danger',
};

const AuditLogs: React.FC = () => {
    const [search, setSearch] = useState('');

    const filtered = mockAuditLogs.filter(log =>
        log.actor_username.toLowerCase().includes(search.toLowerCase()) ||
        log.action.toLowerCase().includes(search.toLowerCase()) ||
        log.details.toLowerCase().includes(search.toLowerCase())
    );

    const columns: Column<AuditLog>[] = [
        { key: 'created_at', header: 'Thời gian', render: (log) => <span style={{ fontSize: '13px' }}>{log.created_at}</span> },
        { key: 'actor_username', header: 'Actor', render: (log) => <strong>{log.actor_username}</strong> },
        {
            key: 'action', header: 'Hành động',
            render: (log) => (
                <span className={`badge ${actionBadgeMap[log.action] || 'badge-muted'}`}>
                    {log.action}
                </span>
            ),
        },
        { key: 'target_type', header: 'Loại mục tiêu' },
        { key: 'target_id', header: 'Target ID', width: '80px' },
        { key: 'details', header: 'Chi tiết' },
        { key: 'ip_address', header: 'IP', render: (log) => <code style={{ fontSize: '12px', background: '#f3f4f6', padding: '2px 6px', borderRadius: '4px' }}>{log.ip_address}</code> },
    ];

    return (
        <div>
            <PageHeader
                title="Log kiểm toán"
                subtitle="Theo dõi mọi hành động quản trị trong hệ thống"
            />
            <div className="page-search">
                <div className="search-wrapper">
                    <Search size={18} className="search-icon" />
                    <input
                        type="text"
                        className="search-input"
                        placeholder="Tìm kiếm audit log..."
                        value={search}
                        onChange={(e) => setSearch(e.target.value)}
                    />
                </div>
            </div>
            <DataTable columns={columns} data={filtered} emptyMessage="Không tìm thấy audit log" />
        </div>
    );
};

export default AuditLogs;
