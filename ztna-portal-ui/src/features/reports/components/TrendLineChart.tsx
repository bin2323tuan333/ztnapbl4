// src/features/reports/components/TrendLineChart.tsx
import { LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer, Legend } from 'recharts';

export const TrendLineChart = ({ data }: { data: any[] }) => {
    return (
        <div className="report-panel">
            <div className="panel-header">
                <h3>Lưu lượng truy cập</h3>
            </div>
            <div style={{ width: '100%', height: 320 }}>
                <ResponsiveContainer>
                    <LineChart data={data} margin={{ top: 10, right: 20, left: -10, bottom: 5 }}>
                        <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="#e5e7eb" />

                        {/* 🔥 Cấu hình trục X tự động giãn nhãn */}
                        <XAxis
                            dataKey="date"
                            axisLine={false}
                            tickLine={false}
                            tick={{ fill: '#6b7280', fontSize: 12 }}
                            minTickGap={30} // Khoảng cách tối thiểu giữa các nhãn là 30px
                            dy={10} // Đẩy nhãn xuống dưới một chút cho thoáng
                        />

                        <YAxis
                            axisLine={false}
                            tickLine={false}
                            tick={{ fill: '#6b7280', fontSize: 12 }}
                        />

                        <Tooltip
                            contentStyle={{ borderRadius: 8, border: 'none', boxShadow: '0 4px 12px rgba(0,0,0,0.1)' }}
                        />

                        <Legend wrapperStyle={{ paddingTop: 20 }} />

                        <Line
                            type="monotone"
                            dataKey="requests"
                            name="Truy cập thành công"
                            stroke="#3b82f6"
                            strokeWidth={3}
                            dot={{ r: 4, strokeWidth: 2 }}
                            activeDot={{ r: 6 }}
                        />
                        <Line
                            type="monotone"
                            dataKey="blocked"
                            name="Bị chặn"
                            stroke="#ef4444"
                            strokeWidth={3}
                            dot={{ r: 4, strokeWidth: 2 }}
                        />
                    </LineChart>
                </ResponsiveContainer>
            </div>
        </div>
    );
};