package com.example.ztna_server.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
public enum ErrorCode {

    // ===== Chung =====
    UNCATEGORIZED_EXCEPTION(9999, "Lỗi hệ thống chưa xác định", HttpStatus.INTERNAL_SERVER_ERROR),
    VALIDATION_ERROR(1000, "Dữ liệu không hợp lệ", HttpStatus.BAD_REQUEST),
    BAD_REQUEST(1001, "Yêu cầu không hợp lệ", HttpStatus.BAD_REQUEST),

    // ===== Auth =====
    UNAUTHENTICATED(1100, "Chưa đăng nhập hoặc token không hợp lệ", HttpStatus.UNAUTHORIZED),
    UNAUTHORIZED(1101, "Bạn không có quyền thực hiện hành động này", HttpStatus.FORBIDDEN),
    INVALID_CREDENTIALS(1102, "Tên đăng nhập hoặc mật khẩu không đúng", HttpStatus.UNAUTHORIZED),
    ACCOUNT_DISABLED(1103, "Tài khoản đã bị khóa", HttpStatus.FORBIDDEN),

    // ===== User =====
    USER_EXISTED(1200, "Tên đăng nhập đã tồn tại", HttpStatus.BAD_REQUEST),
    EMAIL_EXISTED(1201, "Email đã được sử dụng", HttpStatus.BAD_REQUEST),
    USER_NOT_EXISTED(1202, "Người dùng không tồn tại", HttpStatus.NOT_FOUND),
    OLD_PASSWORD_INCORRECT(1203, "Mật khẩu cũ không đúng", HttpStatus.BAD_REQUEST);

    private final int code;
    private final String message;
    private final HttpStatusCode statusCode;

    ErrorCode(int code, String message, HttpStatusCode statusCode) {
        this.code = code;
        this.message = message;
        this.statusCode = statusCode;
    }
}