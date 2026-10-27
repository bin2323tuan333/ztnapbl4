import { useEffect, useState } from 'react';
import { Users, Package, ShieldCheck, Lock } from 'lucide-react';
import { getRequest } from '../../utils/apiClient';
import type { AdminUser } from '../../types/model/User';
import type { Resource } from '../../types/model/Resource';
import './Home.css';

const Home: React.FC = () => {
    const [users, setUsers] = useState<AdminUser[]>([]);
    const [resources, setResources] = useState<Resource[]>([]);
    const [error, setError] = useState('');

    useEffect(() => {
        Promise.all([getRequest('/admin/users'), getRequest('/resources')])
            .then(([u, r]) => {
                setUsers(u);
                setResources(r);
            })
            .catch((err) => setError(err.message || 'Không tải được dữ liệu'));
    }, []);

    const activeUsers = users.filter(u => u.enabled).length;
    const enabledResources = resources.filter(r => r.enabled).length;

    const stats = [
        { label: 'Người dùng', value: users.length, icon: <Users size={24} />, color: '#9333ea', bg: '#f3e8ff' },
        { label: 'Đang hoạt động', value: activeUsers, icon: <ShieldCheck size={24} />, color: '#059669', bg: '#dcfce7' },
        { label: 'Bị khóa', value: users.length - activeUsers, icon: <Lock size={24} />, color: '#dc2626', bg: '#fee2e2' },
        { label: 'Tài nguyên', value: resources.length, icon: <Package size={24} />, color: '#2563eb', bg: '#dbeafe' },
    ];

    return (
        <div>
            <h1 className="home-title">Dashboard</h1>
            <p className="home-subtitle">Tổng quan hệ thống ZTNA</p>

            {error && <div style={{ color: '#dc2626', marginBottom: '12px' }}>{error}</div>}

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
                            <span>Người dùng Active</span>
                            <span className="home-summary-value info">{activeUsers}/{users.length}</span>
                        </div>
                        <div className="home-summary-item">
                            <span>Tài nguyên Enabled</span>
                            <span className="home-summary-value warning">{enabledResources}/{resources.length}</span>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default Home;
