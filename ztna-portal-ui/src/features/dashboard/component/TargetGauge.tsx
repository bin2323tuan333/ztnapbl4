import { PieChart, Pie, Cell, ResponsiveContainer } from 'recharts';

// src/features/dashboard/component/TargetGauge.tsx

interface Props {
    data: {
        successRate: number;
        failedRate: number;
        percentage: string;
        change: number;
        text: string;
    } | null;
}

const COLORS = ['#10b981', '#ef4444'];

export const TargetGauge = ({ data }: Props) => {
    if (!data) return null;

    const chartData = [
        { name: 'Thành công', value: data.successRate },
        { name: 'Bị chặn', value: data.failedRate },
    ];

    const isPositive = data.change >= 0;

    return (
        <div className="dashboard-panel">
            <div className="panel-header">
                <h3>Tỷ lệ truy cập thành công</h3>
            </div>
            <div style={{ position: 'relative', width: '100%', height: 200, display: 'flex', justifyContent: 'center' }}>
                <ResponsiveContainer width="100%" height="100%">
                    <PieChart>
                        <Pie
                            data={chartData}
                            cx="50%"
                            cy="100%"
                            startAngle={180}
                            endAngle={0}
                            innerRadius={120}
                            outerRadius={160}
                            paddingAngle={0}
                            dataKey="value"
                            stroke="none"
                        >
                            {chartData.map((entry, index) => (
                                <Cell key={`cell-${index}`} fill={COLORS[index % COLORS.length]} />
                            ))}
                        </Pie>
                    </PieChart>
                </ResponsiveContainer>

                <div style={{ position: 'absolute', top: '65%', textAlign: 'center' }}>
                    <div style={{ fontSize: 28, fontWeight: 700, color: '#111827' }}>
                        {data.percentage}%
                    </div>
                    <div style={{
                        fontSize: 12,
                        color: isPositive ? '#10b981' : '#ef4444',
                        background: isPositive ? '#ecfdf5' : '#fef2f2',
                        padding: '2px 8px', borderRadius: 10, display: 'inline-block', marginTop: 4
                    }}>
                        {isPositive ? '↑' : '↓'} {Math.abs(data.change)}%
                    </div>
                </div>
            </div>
            <p style={{ textAlign: 'center', color: '#6b7280', fontSize: 13, marginTop: 10 }}>
                {data.text}
            </p>
        </div>
    );
};