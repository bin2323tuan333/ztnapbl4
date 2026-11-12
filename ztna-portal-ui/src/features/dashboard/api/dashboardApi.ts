// src/features/dashboard/api/dashboardApi.ts
import { getRequest } from '@/shared/utils/apiClient';
import { format } from 'date-fns';

// Khớp với DashboardResponse của backend
interface DashboardResponse {
    stats: {
        totalUsers: number;
        activeUsers: number;
        lockedUsers: number;
        totalResources: number;
        enabledResources: number;
    };
    trafficData: { name: string; networkLogs: number; auditLogs: number }[];
    targetData: {
        allowed: number;
        denied: number;
        successRate: number;
        failedRate: number;
        change: number;
    };
}

export const dashboardApi = {
    // GET /api/v1/admin/dashboard?date=yyyy-MM-dd
    getDashboardData: async (date: Date) => {
        const res: DashboardResponse = await getRequest(`/api/v1/admin/dashboard?date=${format(date, 'yyyy-MM-dd')}`);
        const { allowed, denied, successRate, failedRate, change } = res.targetData;
        const day = format(date, 'dd/MM/yyyy');

        return {
            stats: res.stats,
            trafficData: res.trafficData,
            // Giữ đúng dạng dữ liệu TargetGauge đang dùng
            targetData: {
                successRate,
                failedRate,
                percentage: successRate.toFixed(1),
                change,
                text: allowed + denied === 0
                    ? `Chưa có lượt truy cập nào ngày ${day}`
                    : `${allowed} lượt cho phép, ${denied} lượt bị chặn ngày ${day}`,
            },
        };
    },
};
