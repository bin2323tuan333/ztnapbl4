package com.example.ztna_server.service;

import com.example.ztna_server.dto.response.AuditLogResponse;
import com.example.ztna_server.entity.AuditLog;
import com.example.ztna_server.enums.Action;
import com.example.ztna_server.enums.TargetType;
import com.example.ztna_server.repository.AuditLogRepository;
import com.example.ztna_server.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    // ───────────────────────── Ghi log ─────────────────────────

    @Transactional
    public void log(UUID actorId,
                    String actorUsername,
                    String actionStr,          
                    TargetType targetType,
                    String targetId,
                    String extraInfo,          
                    String ipAddress) {

        Action action = parseAction(actionStr);
        String details = translateAction(action, extraInfo);   
        AuditLog log = AuditLog.builder()
                .actorId(actorId)
                .actorUsername(actorUsername)
                .action(action)
                .targetType(targetType)
                .targetId(targetId)
                .details(details)
                .ipAddress(ipAddress)
                .build();

        auditLogRepository.save(log);
    }

    /** Overload khi đã đăng nhập */
    @Transactional
    public void log(String actionStr,
                    TargetType targetType,
                    String targetId,
                    String extraInfo,
                    String ipAddress) {

        UUID actorId = SecurityUtils.currentUserId();
        String actorUsername = SecurityUtils.currentUsername();

        log(actorId, actorUsername, actionStr, targetType, targetId, extraInfo, ipAddress);
    }

    // ───────────────────────── Parse + Translate ─────────────────────────

    private Action parseAction(String actionStr) {
        try {
            return Action.valueOf(actionStr.toUpperCase());
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid action: " + actionStr);
        }
    }

    /**
     * Dịch action chung → câu tiếng Việt cụ thể
     */
    private String translateAction(Action action, String extraInfo) {
        String target = (extraInfo != null && !extraInfo.isBlank()) ? extraInfo : "";

        return switch (action) {
            case LOGIN -> "Đăng nhập thành công";
            case LOGIN_FAILED -> "Đăng nhập thất bại" + (target.isBlank() ? "" : " với tài khoản: " + target);
            case LOGOUT -> "Đăng xuất";
            case REFRESH_TOKEN -> "Làm mới access token";

            case CREATE_USER -> "Tạo người dùng: " + target;
            case UPDATE_USER -> "Cập nhật thông tin người dùng: " + target;
            case BLOCK_USER -> "Khóa tài khoản: " + target;
            case UNBLOCK_USER -> "Mở khóa tài khoản: " + target;
            case RESET_PASSWORD -> "Đặt lại mật khẩu cho: " + target;
            case DELETE_USER -> "Xóa người dùng: " + target;

            case CREATE_RESOURCE -> "Tạo resource: " + target;
            case UPDATE_RESOURCE -> "Cập nhật resource: " + target;
            case DELETE_RESOURCE -> "Xóa resource: " + target;
            case ENABLE_RESOURCE -> "Kích hoạt resource: " + target;
            case DISABLE_RESOURCE -> "Vô hiệu hóa resource: " + target;

            case CREATE_CONNECTOR -> "Tạo connector: " + target;
            case UPDATE_CONNECTOR -> "Cập nhật connector: " + target;
            case DELETE_CONNECTOR -> "Xóa connector: " + target;
            case ENABLE_CONNECTOR -> "Kích hoạt connector: " + target;
            case DISABLE_CONNECTOR -> "Vô hiệu hóa connector: " + target;
            case CONNECTOR_ONLINE -> "Connector online: " + target;
            case CONNECTOR_OFFLINE -> "Connector offline: " + target;

            case CREATE_RULE -> "Tạo rule: " + target;
            case UPDATE_RULE -> "Cập nhật rule: " + target;
            case DELETE_RULE -> "Xóa rule: " + target;
            case ENABLE_RULE -> "Kích hoạt rule: " + target;
            case DISABLE_RULE -> "Vô hiệu hóa rule: " + target;
            case ASSIGN_USER_TO_RULE -> "Gán user vào rule: " + target;
            case ASSIGN_RESOURCE_TO_RULE -> "Gán resource vào rule: " + target;

            default -> action.name() + (target.isBlank() ? "" : " - " + target);
        };
    }

    // ───────────────────────── Query ─────────────────────────

    @Transactional(readOnly = true)
    public Page<AuditLogResponse> search(String actorUsername,
                                         Action action,
                                         TargetType targetType,
                                         Instant from,
                                         Instant to,
                                         Pageable pageable) {

        return auditLogRepository
                .search(actorUsername, action, targetType, from, to, pageable)
                .map(this::toResponse);
    }

    private AuditLogResponse toResponse(AuditLog log) {
        return AuditLogResponse.builder()
                .id(log.getId())
                .actorId(log.getActorId())
                .actorUsername(log.getActorUsername())
                .action(log.getAction().name())         
                .targetType(log.getTargetType())
                .targetId(log.getTargetId())
                .details(log.getDetails())               
                .ipAddress(log.getIpAddress())
                .createdAt(log.getCreatedAt())
                .build();
    }
}