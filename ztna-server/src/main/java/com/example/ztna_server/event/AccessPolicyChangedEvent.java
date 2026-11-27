package com.example.ztna_server.event;

/**
 * Phát ra khi admin thay đổi bất cứ thứ gì ảnh hưởng tới quyền truy cập
 * (rule, người dùng, ứng dụng, connector).
 * SessionLifecycleService nhận sự kiện này (sau khi transaction commit) để kiểm lại
 * mọi phiên đang mở và đóng các phiên không còn được phép (xác minh liên tục – Zero Trust).
 *
 * @param reason mô tả ngắn thay đổi, dùng để ghi log
 */
public record AccessPolicyChangedEvent(String reason) {
}
