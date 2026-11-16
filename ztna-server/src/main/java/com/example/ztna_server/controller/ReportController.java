package com.example.ztna_server.controller;

import com.example.ztna_server.dto.response.report.ReportResponse;
import com.example.ztna_server.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/reports")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class ReportController {

    private final ReportService reportService;

    // GET /api/v1/admin/reports?from=2026-11-10&to=2026-11-16&resourceId=...
    // Bỏ trống from/to = 7 ngày gần nhất; bỏ trống resourceId = tất cả ứng dụng
    @GetMapping
    public ResponseEntity<ReportResponse> getReport(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) UUID resourceId
    ) {
        LocalDate end = to != null ? to : LocalDate.now(ZoneId.of("Asia/Ho_Chi_Minh"));
        LocalDate start = from != null ? from : end.minusDays(6);
        if (start.isAfter(end)) {
            LocalDate tmp = start; start = end; end = tmp;
        }
        return ResponseEntity.ok(reportService.getReport(start, end, resourceId));
    }
}
