import { Users, Package, Plug, ShieldCheck, KeyRound, ScrollText } from 'lucide-react';
import { mockUsers, mockResources, mockConnectors, mockUserPermissions, mockAccessLogs, mockAuditLogs } from '../../data/mockData';
import './Home.css';

const Home: React.FC = () => {
    const stats = [
        { label: 'Người dùng', value: mockUsers.length, icon: <Users size={24} />, color: '#9333ea', bg: '#f3e8ff' },
        { label: 'Tài nguyên', value: mockResources.length, icon: <Package size={24} />, color: '#2563eb', bg: '#dbeafe' },
        { label: 'Connectors', value: mockConnectors.length, icon: <Plug size={24} />, color: '#059669', bg: '#dcfce7' },
        { label: 'Quyền truy cập', value: mockUserPermissions.length, icon: <KeyRound size={24} />, color: '#d97706', bg: '#fef3c7' },
        { label: 'Access Logs', value: mockAccessLogs.length, icon: <ShieldCheck size={24} />, color: '#dc2626', bg: '#fee2e2' },
        { label: 'Audit Logs', value: mockAuditLogs.length, icon: <ScrollText size={24} />, color: '#7c3aed', bg: '#ede9fe' },
    ];

    const onlineConnectors = mockConnectors.filter(c => c.status === 'online').length;
    const activeUsers = mockUsers.filter(u => u.enabled).length;

    return (
        <div>
            <h1 className="home-title">Dashboard</h1>
            <p className="home-subtitle">Tổng quan hệ thống ZTNA</p>

            <div className="home-stats-grid">
                {stats.map((stat, index) => (
                    <div key={index} className="home-stat-card">
                        <div className="home-stat-icon" style={{ background: stat.bg, color: stat.color }}>
                            {stat.icon}
                        </div>
                        <div className="home-stat-info">
                            <span className="home-stat-value">{stat.value}</span>
                            <span className="home-stat-label">{stat.label}</span>
                        </div>
                    </div>
                ))}
            </div>

            <div className="home-summary">
                <div className="home-summary-card">
                    <h3>Trạng thái hệ thống</h3>
                    <div className="home-summary-items">
                        <div className="home-summary-item">
                            <span>Connectors Online</span>
                            <span className="home-summary-value success">{onlineConnectors}/{mockConnectors.length}</span>
                        </div>
                        <div className="home-summary-item">
                            <span>Người dùng Active</span>
                            <span className="home-summary-value info">{activeUsers}/{mockUsers.length}</span>
                        </div>
                        <div className="home-summary-item">
                            <span>Tài nguyên Enabled</span>
                            <span className="home-summary-value warning">{mockResources.filter(r => r.enabled).length}/{mockResources.length}</span>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default Home;
