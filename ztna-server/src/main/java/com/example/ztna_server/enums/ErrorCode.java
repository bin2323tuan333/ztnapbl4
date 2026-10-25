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
    CANNOT_MODIFY_SELF(HttpStatus.BAD_REQUEST, "USR_004", "Cannot block, delete or change role of yourself"),
    USER_IN_USE(HttpStatus.CONFLICT, "USR_005", "User is referenced by other data, block it instead of deleting"),
    BAD_REQUEST(HttpStatus.BAD_REQUEST, "VAL_002", "Invalid request data"),
    ROLE_EXISTED(HttpStatus.CONFLICT, "ROLE_001", "Role name already exists"),
    ROLE_IN_USE(HttpStatus.CONFLICT, "ROLE_002", "Role still has members or permissions, remove them first"),
    ROLE_ALREADY_ASSIGNED(HttpStatus.CONFLICT, "ROLE_003", "User already has this role"),
    ROLE_NOT_ASSIGNED(HttpStatus.NOT_FOUND, "ROLE_004", "User does not have this role"),
    PERMISSION_EXISTED(HttpStatus.CONFLICT, "PERM_001", "Permission already granted"),
    PERMISSION_NOT_FOUND(HttpStatus.NOT_FOUND, "PERM_002", "Permission not found"),
    INVALID_EXPIRY(HttpStatus.BAD_REQUEST, "PERM_003", "Expiry time must be in the future"),
    APP_EXISTED(HttpStatus.CONFLICT, "APP_001", "Application name already exists"),
    APP_IN_USE(HttpStatus.CONFLICT, "APP_002", "Application still has permissions or logs, disable it instead of deleting"),
    INVALID_BACKEND_URL(HttpStatus.BAD_REQUEST, "APP_003", "Backend URL must contain host and port, e.g. http://10.0.0.5:8080"),
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "SYS_001", "Something went wrong");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
