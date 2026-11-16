// src/features/reports/pages/ReportsPage.tsx
import { useReports } from '../hooks/useReports';
import { ReportFilters } from '../components/ReportFilters';
import { TrendLineChart } from '../components/TrendLineChart';
import { TopLists } from '../components/TopLists';
import { DetailedLogsTable } from '../components/DetailedLogsTable';
import type { LogRow } from '../api/reportApi';
import '../styles/Reports.css';

// Bọc ô CSV trong dấu ngoặc kép, nhân đôi dấu " bên trong
const csvCell = (v: string) => `"${(v ?? '').replace(/"/g, '""')}"`;

const formatCsvTime = (iso: string) => {
    const d = new Date(iso);
    const p = (n: number) => String(n).padStart(2, '0');
    return `${d.getFullYear()}-${p(d.getMonth() + 1)}-${p(d.getDate())} ${p(d.getHours())}:${p(d.getMinutes())}:${p(d.getSeconds())}`;
};

// Tạo file CSV từ log và cho trình duyệt tải về
const exportCsv = (logs: LogRow[]) => {
    const header = ['Thời gian', 'Người dùng', 'Ứng dụng', 'Giao thức', 'Trạng thái', 'Lý do'];
    const rows = logs.map(l => [
        formatCsvTime(l.time),
        l.user, l.app, l.protocol,
        l.status === 'ALLOW' ? 'Cho phép' : 'Bị chặn',
        l.reason,
    ]);
    const csv = [header, ...rows].map(r => r.map(csvCell).join(',')).join('\r\n');

    // "\uFEFF" (BOM) để Excel đọc đúng tiếng Việt
    const blob = new Blob(['\uFEFF' + csv], { type: 'text/csv;charset=utf-8' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `bao-cao-truy-cap-${new Date().toISOString().slice(0, 10)}.csv`;
    a.click();
    URL.revokeObjectURL(url);
};

export const ReportsPage = () => {
    const { data, isLoading, error, refetch, resources, filters } = useReports();

    const handleExport = () => {
        if (!data || data.logs.length === 0) {
            alert('Không có dữ liệu để xuất');
            return;
        }
        exportCsv(data.logs);
    };

    return (
        <div className="reports-container">
            <ReportFilters
                {...filters}
                resources={resources}
                onExport={handleExport}
            />

            {error && (
                <div className="dashboard-error-banner">
                    <span>{error}</span>
                    <button onClick={refetch}>Thử lại</button>
                </div>
            )}

            {isLoading ? (
                <div className="reports-loading">Đang tải dữ liệu báo cáo...</div>
            ) : (
                <div className="reports-content">
                    {data && (
                        <div className="text-muted" style={{ fontSize: 14 }}>
                            Tổng <strong>{data.total}</strong> lượt truy cập:{' '}
                            <strong style={{ color: '#16a34a' }}>{data.allowed}</strong> cho phép,{' '}
                            <strong style={{ color: '#dc2626' }}>{data.denied}</strong> bị chặn
                        </div>
                    )}
                    <TrendLineChart data={data?.trendData || []} />
                    <TopLists topUsers={data?.topUsers || []} topApps={data?.topApps || []} />
                    <DetailedLogsTable logs={data?.logs || []} />
                </div>
            )}
        </div>
    );
};
