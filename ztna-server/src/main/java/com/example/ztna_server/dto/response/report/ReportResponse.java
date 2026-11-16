package com.example.ztna_server.dto.response.report;

import java.time.Instant;
import java.util.List;

/**
 * Dữ liệu cho trang Báo cáo & Thống kê
 * (GET /api/v1/admin/reports?from=yyyy-MM-dd&to=yyyy-MM-dd&resourceId=...).
 * Tên trường khớp với frontend features/reports.
 */
public record ReportResponse(
        long total,                 // tổng số lượt truy cập trong khoảng
        long allowed,               // số lượt ALLOW
        long denied,                // số lượt DENY
        List<TrendPoint> trendData, // biểu đồ đường
        List<TopItem> topUsers,     // 5 người dùng truy cập nhiều nhất
        List<TopItem> topApps,      // 5 ứng dụng được truy cập nhiều nhất
        List<LogRow> logs           // log chi tiết, mới nhất trước (tối đa 500 dòng)
) {
    /** Một điểm trên biểu đồ: date là "dd/MM" (theo ngày) hoặc "HH:00" (khi chỉ chọn 1 ngày) */
    public record TrendPoint(String date, long requests, long blocked) {}

    public record TopItem(String id, String name, long count) {}

    public record LogRow(
            String id,
            Instant time,
            String user,
            String app,
            String protocol,   // loại ứng dụng: WEB / SSH / MYSQL
            String status,     // ALLOW / DENY
            String reason
    ) {}
}
