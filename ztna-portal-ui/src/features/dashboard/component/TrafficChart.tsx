// src/features/dashboard/component/TrafficChart.tsx
import { BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer, Legend } from 'recharts';

interface Props {
    data: any[];
}

export const TrafficChart = ({ data }: Props) => {
    return (
        // Thêm display flex để nó có thể giãn theo chiều dọc
        <div className="dashboard-panel" style={{ display: 'flex', flexDirection: 'column', height: '100%' }}>
            <div className="panel-header">
                <h3>Traffic summary</h3>
            </div>
            {/* 🔥 Đổi height: 300 thành flex: 1, minHeight: 300 */}
            <div style={{ width: '100%', flex: 1, minHeight: 300 }}>
                <ResponsiveContainer>
                    <BarChart data={data} margin={{ top: 20, right: 30, left: -20, bottom: 5 }}>
                        <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="#e5e7eb" />
                        <XAxis dataKey="name" axisLine={false} tickLine={false} tick={{ fill: '#6b7280', fontSize: 12 }} />
                        <YAxis axisLine={false} tickLine={false} tick={{ fill: '#6b7280', fontSize: 12 }} />
                        <Legend wrapperStyle={{ paddingTop: '10px' }} />
                        <Tooltip cursor={{ fill: '#f3f4f6' }} />
                        <Bar dataKey="networkLogs" name="Network Logs" fill="#3b82f6" radius={[4, 4, 0, 0]} barSize={12} />
                        <Bar dataKey="auditLogs" name="Audit Logs" fill="#10b981" radius={[4, 4, 0, 0]} barSize={12} />
                    </BarChart>
                </ResponsiveContainer>
            </div>
        </div>
    );
};