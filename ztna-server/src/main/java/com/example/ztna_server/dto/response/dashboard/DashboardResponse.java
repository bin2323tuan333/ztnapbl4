package com.example.ztna_server.dto.response.dashboard;

import java.util.List;

/**
 * Dữ liệu cho trang Bảng điều khiển (GET /api/v1/admin/dashboard?date=yyyy-MM-dd).
 * Tên trường khớp với frontend features/dashboard.
 */
public record DashboardResponse(
        Stats stats,
        List<TrafficPoint> trafficData,
        Target targetData
) {
    /** 4 thẻ số liệu: người dùng và ứng dụng (tính tại thời điểm gọi) */
    public record Stats(
            long totalUsers,
            long activeUsers,
            long lockedUsers,
            long totalResources,
            long enabledResources
    ) {}

    /** Một cột trên biểu đồ: số log trong khung 2 giờ, vd name = "08:00" */
    public record TrafficPoint(
            String name,
            long networkLogs,   // số lượt truy cập ứng dụng (access log)
            long auditLogs      // số thao tác quản trị (audit log)
    ) {}

    /** Tỷ lệ truy cập thành công trong ngày và mức thay đổi so với hôm trước */
    public record Target(
            long allowed,
            long denied,
            double successRate,  // % ALLOW
            double failedRate,   // % DENY
            double change        // điểm % chênh lệch so với ngày hôm trước
    ) {}
}
