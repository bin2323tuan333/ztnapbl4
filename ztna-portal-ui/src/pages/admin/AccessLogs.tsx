import React, { useState } from 'react';
import { Search, RotateCw, CheckCircle, XCircle, Monitor, Globe, Radio, Server } from 'lucide-react';
import DataTable from '../../components/common/DataTable';
import { mockAccessLogs } from '../../data/mockData';
import type { AccessLog, Column } from '../../types';
import './AccessLogs.css';
import './CommonPages.css';

const AccessLogs: React.FC = () => {
    const [search, setSearch] = useState('');
    const [selectedLog, setSelectedLog] = useState<AccessLog | null>(mockAccessLogs[0] || null);

    const filtered = mockAccessLogs.filter(log =>
        log.username.toLowerCase().includes(search.toLowerCase()) ||
        log.resource_name.toLowerCase().includes(search.toLowerCase()) ||
        (log.device_name || '').toLowerCase().includes(search.toLowerCase())
    );

    const columns: Column<AccessLog>[] = [
        { key: 'created_at', header: 'Time', render: (log) => <span style={{ fontSize: '13px' }}>{log.created_at}</span> },
        { key: 'username', header: 'User Name', render: (log) => <strong>{log.username}</strong> },
        { key: 'device_name', header: 'Device Name', render: (log) => <span>{log.device_name || '—'}</span> },
        { key: 'source', header: 'Source', render: (log) => <code className="al-code">{log.source || log.client_ip}</code> },
        { key: 'destination', header: 'Destination', render: (log) => <code className="al-code">{log.destination || '—'}</code> },
        {
            key: 'status', header: 'Status',
            render: (log) => (
                <span className={`al-status ${log.status === 'success' ? 'al-status-success' : 'al-status-failed'}`}>
                    {log.status === 'success' ? <CheckCircle size={14} /> : <XCircle size={14} />}
                    <span>{log.status === 'success' ? 'Success' : 'Failed'}</span>
                </span>
            ),
        },
        {
            key: 'latency', header: 'Total Latency',
            render: (log) => <span>{log.latency ? `${log.latency}ms` : '—'}</span>,
        },
        { key: 'destination_port', header: 'Port' },
        { key: 'protocol', header: 'Protocol', render: (log) => <span className="badge badge-muted">{log.protocol}</span> },
        { key: 'resource_name', header: 'Application Name' },
        {
            key: 'connector_zone', header: 'Connector Zone',
            render: (log) => log.connector_zone ? <span className="badge badge-info">{log.connector_zone}</span> : <span>—</span>,
        },
    ];

    const now = new Date().toLocaleString('vi-VN');

    return (
        <div className="al-page">
            <h1 className="al-title">Log truy cập</h1>
            
            <div className="al-info-bar">
                <span className="al-updated">
                    Last updated on {now}
                </span>
                <button className="al-refresh-btn" title="Refresh">
                    <RotateCw size={16} />
                </button>
            </div>

            <div className="al-search">
                <div className="al-search-wrapper">
                    <Search size={18} className="search-icon" />
                    <input
                        type="text"
                        className="search-input al-search-input"
                        placeholder="Tìm kiếm log..."
                        value={search}
                        onChange={(e) => setSearch(e.target.value)}
                    />
                </div>
            </div>

            <div className="al-table-wrapper" onClick={(e) => {
                const row = (e.target as HTMLElement).closest('.dt-row');
                if (row) {
                    const rowIndex = Array.from(row.parentElement!.children).indexOf(row);
                    if (filtered[rowIndex]) {
                        setSelectedLog(filtered[rowIndex]);
                    }
                }
            }}>
                <DataTable columns={columns} data={filtered} emptyMessage="Không tìm thấy log" />
            </div>

            {selectedLog && selectedLog.latency && (
                <div className="al-path-section">
                    <h3 className="al-path-title">Path Visualization</h3>
                    <div className="al-path-container">
                        <div className="al-path-node">
                            <div className="al-path-circle">
                                <Monitor size={18} />
                            </div>
                            <span className="al-path-label">Source</span>
                            <span className="al-path-value">{selectedLog.source || selectedLog.client_ip}</span>
                        </div>

                        <div className="al-path-connector">
                            <span className="al-path-latency">{Math.round(selectedLog.latency * 0.6)}ms</span>
                            <div className="al-path-line" />
                        </div>

                        <div className="al-path-node">
                            <div className="al-path-circle">
                                <Globe size={18} />
                            </div>
                            <span className="al-path-label">Pop Location</span>
                            <span className="al-path-value">Edge Node</span>
                        </div>

                        <div className="al-path-connector">
                            <span className="al-path-latency">{Math.round(selectedLog.latency * 0.3)}ms</span>
                            <div className="al-path-line" />
                        </div>

                        <div className="al-path-node">
                            <div className="al-path-circle">
                                <Radio size={18} />
                            </div>
                            <span className="al-path-label">Connector Zone</span>
                            <span className="al-path-value">{selectedLog.connector_zone || '—'}</span>
                        </div>

                        <div className="al-path-connector">
                            <span className="al-path-latency">{Math.round(selectedLog.latency * 0.1)}ms</span>
                            <div className="al-path-line" />
                        </div>

                        <div className="al-path-node">
                            <div className="al-path-circle">
                                <Server size={18} />
                            </div>
                            <span className="al-path-label">Destination</span>
                            <span className="al-path-value">{selectedLog.destination || '—'}</span>
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
};

export default AccessLogs;
