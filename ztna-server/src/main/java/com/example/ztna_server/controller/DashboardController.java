package com.example.ztna_server.controller;

import com.example.ztna_server.dto.response.dashboard.DashboardResponse;
import com.example.ztna_server.service.DashboardService;
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

@RestController
@RequestMapping("/api/v1/admin/dashboard")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class DashboardController {

    private final DashboardService dashboardService;

    // GET /api/v1/admin/dashboard?date=2026-11-12 (bỏ trống = hôm nay)
    @GetMapping
    public ResponseEntity<DashboardResponse> getDashboard(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        LocalDate day = date != null ? date : LocalDate.now(ZoneId.of("Asia/Ho_Chi_Minh"));
        return ResponseEntity.ok(dashboardService.getDashboard(day));
    }
}
