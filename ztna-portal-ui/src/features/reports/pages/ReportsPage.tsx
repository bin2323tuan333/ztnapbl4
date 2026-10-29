// src/features/reports/pages/ReportsPage.tsx
import { useReports } from '../hooks/useReports';
import { ReportFilters } from '../components/ReportFilters';
import { TrendLineChart } from '../components/TrendLineChart';
import { TopLists } from '../components/TopLists';
import { DetailedLogsTable } from '../components/DetailedLogsTable';
import '../styles/Reports.css';

export const ReportsPage = () => {
    const { data, isLoading, error, refetch, filters } = useReports();

    const handleExport = () => {
        alert('Đang xuất file báo cáo CSV... (Demo)');
    };

    return (
        <div className="reports-container">
            {/* Đã xóa phần Header ở đây */}

            <ReportFilters
                {...filters}
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
                    <TrendLineChart data={data?.trendData || []} />
                    <TopLists topUsers={data?.topUsers || []} topApps={data?.topApps || []} />
                    <DetailedLogsTable logs={data?.logs || []} />
                </div>
            )}
        </div>
    );
};