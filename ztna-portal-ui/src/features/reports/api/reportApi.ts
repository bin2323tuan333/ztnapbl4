// src/features/reports/api/reportApi.ts
import { format, subDays, parseISO, eachDayOfInterval, differenceInCalendarDays } from 'date-fns';

export interface TrendData { date: string; requests: number; blocked: number; }
// ... các interface khác giữ nguyên

const delay = (ms: number) => new Promise(resolve => setTimeout(resolve, ms));

export const reportApi = {
    getReportData: async (fromDate: string, toDate: string, app: string) => {
        await delay(600);

        let trendData: TrendData[] = [];

        // Nếu không có ngày, mặc định lấy 7 ngày
        if (!fromDate || !toDate) {
            const defaultDays = Array.from({ length: 7 }).map((_, i) => subDays(new Date(), 6 - i));
            trendData = defaultDays.map(d => ({
                date: format(d, 'dd/MM'),
                requests: Math.floor(Math.random() * 3000) + 3000,
                blocked: Math.floor(Math.random() * 500) + 100,
            }));
        } else {
            const start = parseISO(fromDate);
            const end = parseISO(toDate);
            const totalDays = differenceInCalendarDays(end, start) + 1; // Tính tổng số ngày

            // 🟢 TRƯỜNG HỢP 1: CHỌN 1 NGÀY -> CHIA THEO GIỜ
            if (totalDays === 1) {
                const hours = ['00:00', '04:00', '08:00', '12:00', '16:00', '20:00'];
                trendData = hours.map(hour => ({
                    date: hour,
                    requests: Math.floor(Math.random() * 2000) + 1000,
                    blocked: Math.floor(Math.random() * 200) + 50,
                }));
            }
            // 🟢 TRƯỜNG HỢP 2: TỪ 2 ĐẾN 10 NGÀY -> MỖI NGÀY 1 CHẤM
            else if (totalDays <= 10) {
                const daysList = eachDayOfInterval({ start, end });
                trendData = daysList.map(d => ({
                    date: format(d, 'dd/MM'),
                    requests: Math.floor(Math.random() * 3000) + 3000,
                    blocked: Math.floor(Math.random() * 500) + 100,
                }));
            }
            // 🟢 TRƯỜNG HỢP 3: TRÊN 10 NGÀY -> CÁCH QUÃNG (Mỗi 2, 3 ngày 1 chấm)
            else {
                // Tính bước nhảy (step) để biểu đồ có khoảng 10-15 điểm là đẹp nhất
                const step = Math.ceil(totalDays / 12);
                const daysList = eachDayOfInterval({ start, end });

                // Lọc từng ngày dựa trên bước nhảy
                trendData = daysList
                    .filter((_, index) => index % step === 0)
                    .map(d => ({
                        date: format(d, 'dd/MM'),
                        // Vì cách quãng nên nhân số liệu lên cho biểu đồ cao hơn chút
                        requests: Math.floor((Math.random() * 3000 + 3000) * (step / 2)),
                        blocked: Math.floor((Math.random() * 500 + 100) * (step / 2)),
                    }));
            }
        }

        // ... (Phần Top Users, Top Apps, Logs giữ nguyên như cũ)
        const topUsers = [
            { id: '1', name: 'Nguyễn Văn A', count: 1250 },
            { id: '2', name: 'Trần Thị B', count: 980 },
            { id: '3', name: 'Lê Hoàng C', count: 756 },
        ];
        const topApps = [
            { id: '1', name: 'MySQL Production', count: 3400 },
            { id: '2', name: 'AWS Console', count: 2100 },
            { id: '3', name: 'Internal GitLab', count: 1500 },
        ];
        const logs = Array.from({ length: 15 }).map((_, i) => ({
            id: `log-${i}`, time: format(subDays(new Date(), i % 3), 'dd/MM/yyyy HH:mm:ss'),
            user: topUsers[i % 3].name, app: topApps[i % 3].name,
            status: i % 5 === 0 ? 'BLOCKED' : 'SUCCESS', protocol: i % 2 === 0 ? 'HTTPS' : 'TCP',
        }));

        return { trendData, topUsers, topApps, logs };
    }
};