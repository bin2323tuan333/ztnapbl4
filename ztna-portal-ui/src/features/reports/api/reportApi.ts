// src/features/reports/api/reportApi.ts
import { getRequest } from '@/shared/utils/apiClient';

// ───────────── Kiểu dữ liệu (khớp ReportResponse của backend) ─────────────

export interface TrendData { date: string; requests: number; blocked: number; }
export interface TopItem { id: string; name: string; count: number; }
export interface LogRow {
    id: string;
    time: string;       // ISO
    user: string;
    app: string;
    protocol: string;   // WEB / SSH / MYSQL
    status: 'ALLOW' | 'DENY';
    reason: string;
}
export interface ReportData {
    total: number;
    allowed: number;
    denied: number;
    trendData: TrendData[];
    topUsers: TopItem[];
    topApps: TopItem[];
    logs: LogRow[];
}
export interface ResourceOption { id: string; name: string; type: string; }

// ───────────── API ─────────────

export const reportApi = {
    // GET /api/v1/admin/reports?from=yyyy-MM-dd&to=yyyy-MM-dd&resourceId=...
    // from/to rỗng = 7 ngày gần nhất; app = 'ALL' là tất cả ứng dụng
    getReportData: (fromDate: string, toDate: string, app: string): Promise<ReportData> => {
        const params = new URLSearchParams();
        if (fromDate) params.set('from', fromDate);
        if (toDate) params.set('to', toDate);
        if (app && app !== 'ALL') params.set('resourceId', app);
        const query = params.toString();
        return getRequest(`/api/v1/admin/reports${query ? `?${query}` : ''}`);
    },

    // Danh sách ứng dụng cho ô lọc
    getResources: (): Promise<ResourceOption[]> => getRequest('/api/v1/admin/resources'),
};
