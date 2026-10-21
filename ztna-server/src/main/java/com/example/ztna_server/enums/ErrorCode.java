package com.example.ztna_server.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "AUTH_001","Invalid username or password"),
    UNAUTHENTICATED(HttpStatus.UNAUTHORIZED, "AUTH_002", "Authentication required"),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH_004", "Token not valid"),
    REFRESH_TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "AUTH_004", "Token has expired"),
    ACCOUNT_LOCKED(HttpStatus.FORBIDDEN, "AUTH_010", "Account is blocked"),
    ACCESS_DENIED(HttpStatus.FORBIDDEN, "AUTH_011", "You do not have permission"),
    VALIDATION_ERROR(HttpStatus.BAD_REQUEST, "VAL_001", "Validation failed"),
    RESOURCE_NOT_FOUND(HttpStatus.NOT_FOUND, "USR_001", "Resource not found"),
    USERNAME_EXISTED(HttpStatus.CONFLICT, "USR_002", "Username already exists"),
    EMAIL_EXISTED(HttpStatus.CONFLICT, "USR_003", "Email already exists"),
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "SYS_001", "Something went wrong");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
