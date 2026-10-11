package com.example.ztna_server.service;

import com.example.ztna_server.api.dto.AuditLogResponse;
import com.example.ztna_server.entity.AuditLog;
import com.example.ztna_server.entity.enums.AuditAction;
import com.example.ztna_server.repo.AuditLogRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Service
@RequiredArgsConstructor
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    // ========== Ghi log ==========

    @Transactional
    public void log(Long actorId, String actorUsername, AuditAction action,
                    String targetType, Long targetId, String details) {
        AuditLog log = AuditLog.builder()
                .actorId(actorId)
                .actorUsername(actorUsername)
                .action(action)
                .targetType(targetType)
                .targetId(targetId)
                .details(details)
                .ipAddress(resolveClientIp())
                .build();

        auditLogRepository.save(log);
    }

    //Khi chưa có user (login fail, ...)
    @Transactional
    public void log(String actorUsername, AuditAction action,
                    String targetType, Long targetId, String details) {
        log(null, actorUsername, action, targetType, targetId, details);
    }


    public Page<AuditLogResponse> getAll(Pageable pageable) {
        return auditLogRepository.findAll(pageable).map(this::toResponse);
    }

    public Page<AuditLogResponse> getByActor(Long actorId, Pageable pageable) {
        return auditLogRepository.findByActorId(actorId, pageable).map(this::toResponse);
    }

    public Page<AuditLogResponse> getByAction(AuditAction action, Pageable pageable) {
        return auditLogRepository.findByAction(action, pageable).map(this::toResponse);
    }


    private AuditLogResponse toResponse(AuditLog log) {
        return AuditLogResponse.builder()
                .id(log.getId())
                .actorId(log.getActorId())
                .actorUsername(log.getActorUsername())
                .action(translateAction(log.getAction()))
                .targetType(log.getTargetType())
                .targetId(log.getTargetId())
                .details(log.getDetails())
                .ipAddress(log.getIpAddress())
                .createdAt(log.getCreatedAt())
                .build();
    }

    private String translateAction(AuditAction action) {
        if (action == null) {
            return "Không xác định";
        }
        return switch (action) {
            case LOGIN -> "Đăng nhập";
            case LOGIN_FAILED -> "Đăng nhập thất bại";
            case REGISTER -> "Đăng ký tài khoản";
            case CREATE_USER -> "Tạo tài khoản mới";
            case CHANGE_PASSWORD -> "Đổi mật khẩu";
            case ENABLE_USER -> "Mở khóa tài khoản";
            case DISABLE_USER -> "Khóa tài khoản";
        };
    }


    private String resolveClientIp() {
        try {
            ServletRequestAttributes attrs =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs == null) {
                return null;
            }

            HttpServletRequest request = attrs.getRequest();
            String xff = request.getHeader("X-Forwarded-For");
            if (xff != null && !xff.isBlank()) {
                return xff.split(",")[0].trim();
            }
            return request.getRemoteAddr();
        } catch (Exception e) {
            return null;
        }
    }
}