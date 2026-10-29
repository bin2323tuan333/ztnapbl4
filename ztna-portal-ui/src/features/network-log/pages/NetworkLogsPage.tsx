import { useState } from 'react';
import { RefreshCw, Filter, Calendar } from 'lucide-react';
import { useNetworkLogs } from '../hooks/useNetworkLogs';
import { LogTable } from '../components/LogTable';
import { RouteTrace } from '../components/RouteTrace';
import '../styles/NetworkLogs.css';

export const NetworkLogsPage = () => {
    const { logs, isLoading, selectedLogId, handleSelectLog, routeData } = useNetworkLogs();
    const [timeRange, setTimeRange] = useState('7d');

    return (
        <div className="network-logs-layout">
            {/* HEADER BÊN NGOÀI */}
            <div className="page-header">
                <h1 className="page-title">Network</h1>
                <div className="page-filters">
                    <button className="filter-btn">
                        <Calendar size={14} /> Last 7 days
                    </button>
                    <button className="filter-btn">
                        <Filter size={14} /> Filters
                    </button>
                </div>
            </div>

            {/* KHỐI 1: CARD CHỨA BẢNG LOG */}
            <div className="logs-table-card">
                <div className="logs-subheader">
                    <button className="icon-btn" title="Làm mới">
                        <RefreshCw size={14} />
                    </button>
                    <span>Last updated on November 9, 2024 10:15:20 PM</span>
                </div>

                <div className="logs-table-scroll-area">
                    <LogTable
                        logs={logs}
                        isLoading={isLoading}
                        selectedId={selectedLogId}
                        onSelect={handleSelectLog}
                    />
                </div>
            </div>

            {/* KHỐI 2: CARD CHỨA ROUTE TRACE */}
            <div className="route-trace-card">
                {routeData ? (
                    <RouteTrace data={routeData} />
                ) : (
                    <div className="empty-trace-state">
                        <div className="skeleton-flow">
                            <div className="skeleton-node">
                                <div className="skeleton-circle"></div>
                                <div className="skeleton-line-label"></div>
                            </div>
                            <div className="skeleton-line"></div>
                            <div className="skeleton-node">
                                <div className="skeleton-circle"></div>
                                <div className="skeleton-line-label"></div>
                            </div>
                            <div className="skeleton-line"></div>
                            <div className="skeleton-node">
                                <div className="skeleton-circle"></div>
                                <div className="skeleton-line-label"></div>
                            </div>
                        </div>
                        <p className="empty-text">Chọn một bản ghi bên trên để xem chi tiết đường truyền.</p>
                    </div>
                )}
            </div>
        </div>
    );
};