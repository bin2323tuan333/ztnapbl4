package com.example.ztna_server.controller;

import com.example.ztna_server.dto.response.AccessLogResponse;
import com.example.ztna_server.enums.Decision;
import com.example.ztna_server.service.AccessLogService;
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
@RequestMapping("/api/v1/admin/access-logs")
@RequiredArgsConstructor
public class AccessLogController {

    private final AccessLogService accessLogService;

    /**
     * GET /api/admin/access-logs
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISOR')")
    public ResponseEntity<Page<AccessLogResponse>> search(
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String resourceName,
            @RequestParam(required = false) Decision decision,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable
    ) {
        Page<AccessLogResponse> result = accessLogService.search(
                username, resourceName, decision, from, to, pageable
        );
        return ResponseEntity.ok(result);
    }
}