import type { ReactNode } from 'react';

interface Props {
    label: string;
    value: number;
    icon: ReactNode;
    bg: string;
    color: string;
}

export const StatCard = ({ label, value, icon, bg, color }: Props) => {
    return (
        <div className="dashboard-stat-card">
            <div className="dashboard-stat-icon" style={{ background: bg, color: color }}>
                {icon}
            </div>
            <div className="dashboard-stat-info">
                <span className="dashboard-stat-value">{value}</span>
                <span className="dashboard-stat-label">{label}</span>
            </div>
        </div>
    );
};