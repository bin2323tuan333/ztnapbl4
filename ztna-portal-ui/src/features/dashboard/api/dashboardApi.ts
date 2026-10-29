// src/features/dashboard/api/dashboardApi.ts
import { getRequest } from '@/shared/utils/apiClient';
import { format } from 'date-fns';

// Hàm sinh dữ liệu giả lập dựa trên ngày
const getDynamicMockData = (dateStr: string) => {
    const seed = dateStr.split('-').reduce((acc, char) => acc + char.charCodeAt(0), 0);
    const multiplier = (seed % 5) + 1;

    const hours = Array.from({ length: 12 }, (_, i) => {
        const h = i * 2;
        return `${String(h).padStart(2, '0')}:00`;
    });

    const trafficData = hours.map((hour, index) => {
        const timeFactor = index < 4 ? 1 : index < 8 ? 2 : 1.5;
        return {
            name: hour,
            networkLogs: Math.floor((Math.random() * 1500 + 500) * multiplier * timeFactor),
            auditLogs: Math.floor((Math.random() * 500 + 100) * multiplier * timeFactor),
        };
    });

    // TÍNH TOÁN 4 THẺ KPI
    const totalUsers = 20 + (seed % 10);
    const lockedUsers = 3 + (seed % 4);
    const totalResources = 10 + (seed % 5);
    const successRate = 85 + (seed % 14);

    return {
        stats: {
            totalUsers: totalUsers,
            activeUsers: totalUsers - lockedUsers,
            lockedUsers: lockedUsers,
            totalResources: totalResources,
            enabledResources: totalResources - 1,
        },
        trafficData,
        targetData: {
            successRate: successRate,
            failedRate: 100 - successRate,
            percentage: successRate.toFixed(2),
            change: (seed % 5) - 2,
            text: `Thống kê truy cập ngày ${format(new Date(dateStr), 'dd/MM/yyyy')}`
        }
    };
};

const delay = (ms: number) => new Promise(resolve => setTimeout(resolve, ms));

export const dashboardApi = {
    getDashboardData: async (date: Date) => {
        const dateStr = format(date, 'yyyy-MM-dd');
        await delay(600);
        return getDynamicMockData(dateStr);
    }
};