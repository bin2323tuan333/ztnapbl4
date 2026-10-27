package com.example.ztna_server.controller;

import com.example.ztna_server.dto.response.AuditLogResponse;
import com.example.ztna_server.enums.Action;
import com.example.ztna_server.enums.TargetType;
import com.example.ztna_server.service.AuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@RestController
@RequestMapping("/api/admin/audit-logs")
@RequiredArgsConstructor
public class AuditController {

    private final AuditService auditService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<AuditLogResponse>> search(
            @RequestParam(required = false) String actorUsername,
            @RequestParam(required = false) Action action,
            @RequestParam(required = false) TargetType targetType,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable
    ) {
        Page<AuditLogResponse> result = auditService.search(
                actorUsername,
                action,
                targetType,
                from,
                to,
                pageable
        );
        return ResponseEntity.ok(result);
    }
}