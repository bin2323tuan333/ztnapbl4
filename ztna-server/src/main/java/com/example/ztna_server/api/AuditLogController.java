package com.example.ztna_server.api;

import com.example.ztna_server.api.dto.AuditLogResponse;
import com.example.ztna_server.entity.enums.AuditAction;
import com.example.ztna_server.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/audit-logs")
@RequiredArgsConstructor
public class AuditLogController {

    private final AuditLogService auditLogService;

    /**
     * Lấy tất cả audit log (phân trang)
     * GET /api/audit-logs?page=0&size=20&sort=createdAt,desc
     */
    @GetMapping
    public ResponseEntity<Page<AuditLogResponse>> getAll(Pageable pageable) {
        return ResponseEntity.ok(auditLogService.getAll(pageable));
    }

    /**
     * Lấy log theo actor
     * GET /api/audit-logs/actor/{actorId}
     */
    @GetMapping("/actor/{actorId}")
    public ResponseEntity<Page<AuditLogResponse>> getByActor(
            @PathVariable Long actorId,
            Pageable pageable
    ) {
        return ResponseEntity.ok(auditLogService.getByActor(actorId, pageable));
    }

    /**
     * Lấy log theo action
     * GET /api/audit-logs/action/{action}
     */
    @GetMapping("/action/{action}")
    public ResponseEntity<Page<AuditLogResponse>> getByAction(
            @PathVariable AuditAction action,
            Pageable pageable
    ) {
        return ResponseEntity.ok(auditLogService.getByAction(action, pageable));
    }
}