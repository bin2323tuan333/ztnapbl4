// src/features/dashboard/pages/Dashboard.tsx
import { Users, Package, ShieldCheck, Lock } from 'lucide-react';
import './../styles/Dashboard.css';
import { useCalendar } from '../hooks/useCalendar';
import { useDashboard } from '../hooks/useDashboard';
import { StatCard } from '../component/StatCard';
import { TrafficChart } from '../component/TrafficChart';
import { TargetGauge } from '../component/TargetGauge';
import { CalendarWidget } from '../component/CalendarWidget';

export const Dashboard = () => {
    const calendar = useCalendar();
    const { selectedDate } = calendar;

    const { data, isLoading, error, refetch } = useDashboard(selectedDate || new Date());

    if (isLoading && !data) return <div style={{ padding: 24 }}>Đang tải dữ liệu...</div>;

    // 🔥 ĐÃ SỬA: Lấy đúng từ data.stats
    const statCards = [
        { label: 'Tổng Người dùng', value: data?.stats?.totalUsers || 0, icon: <Users size={24} />, color: '#8b5cf6', bg: '#f5f3ff' },
        { label: 'Đang hoạt động', value: data?.stats?.activeUsers || 0, icon: <ShieldCheck size={24} />, color: '#10b981', bg: '#ecfdf5' },
        { label: 'Bị khóa', value: data?.stats?.lockedUsers || 0, icon: <Lock size={24} />, color: '#ef4444', bg: '#fef2f2' },
        { label: 'Tổng Tài nguyên', value: data?.stats?.totalResources || 0, icon: <Package size={24} />, color: '#3b82f6', bg: '#eff6ff' },
    ];

    return (
        <div className="dashboard-container">
            {error && (
                <div className="dashboard-error-banner">
                    <span>⚠️ {error}</span>
                    <button onClick={refetch}>Thử lại</button>
                </div>
            )}

            <div className="dashboard-layout-grid">
                <div className="dashboard-left-col">
                    <div className="dashboard-kpi-grid">
                        {statCards.map((stat, index) => (
                            <StatCard key={index} {...stat} />
                        ))}
                    </div>

                    <div className={isLoading ? 'opacity-50 pointer-events-none' : ''} style={{ flex: 1, display: 'flex', flexDirection: 'column' }}>
                        <TrafficChart data={data?.trafficData || []} />
                    </div>
                </div>

                <div className="dashboard-right-col">
                    <div className={isLoading ? 'opacity-50 pointer-events-none' : ''}>
                        <TargetGauge data={data?.targetData || null} />
                    </div>

                    <CalendarWidget {...calendar} />
                </div>
            </div>
        </div>
    );
};