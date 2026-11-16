package com.example.ztna_server.service;

import com.example.ztna_server.dto.response.report.ReportResponse;
import com.example.ztna_server.dto.response.report.ReportResponse.LogRow;
import com.example.ztna_server.dto.response.report.ReportResponse.TopItem;
import com.example.ztna_server.dto.response.report.ReportResponse.TrendPoint;
import com.example.ztna_server.entity.AccessLog;
import com.example.ztna_server.entity.Resource;
import com.example.ztna_server.enums.Decision;
import com.example.ztna_server.repository.AccessLogRepository;
import com.example.ztna_server.repository.ResourceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReportService {

    private static final ZoneId ZONE = ZoneId.of("Asia/Ho_Chi_Minh");
    private static final DateTimeFormatter DAY = DateTimeFormatter.ofPattern("dd/MM");
    private static final int TOP_SIZE = 5;
    private static final int MAX_LOGS = 500;
    private static final int MAX_POINTS = 12;   // biểu đồ tối đa ~12 điểm cho dễ nhìn

    private final AccessLogRepository accessLogRepository;
    private final ResourceRepository resourceRepository;

    /**
     * @param from       ngày bắt đầu (tính cả ngày này)
     * @param to         ngày kết thúc (tính cả ngày này)
     * @param resourceId lọc theo 1 ứng dụng, null = tất cả
     */
    public ReportResponse getReport(LocalDate from, LocalDate to, UUID resourceId) {
        Instant start = from.atStartOfDay(ZONE).toInstant();
        Instant end = to.plusDays(1).atStartOfDay(ZONE).toInstant();

        List<AccessLog> logs = accessLogRepository
                .findByCreatedAtGreaterThanEqualAndCreatedAtLessThan(start, end).stream()
                .filter(l -> resourceId == null || resourceId.equals(l.getResourceId()))
                .sorted(Comparator.comparing(AccessLog::getCreatedAt).reversed())
                .toList();

        long allowed = logs.stream().filter(l -> l.getDecision() == Decision.ALLOW).count();

        return new ReportResponse(
                logs.size(),
                allowed,
                logs.size() - allowed,
                buildTrend(logs, from, to),
                top(logs, l -> String.valueOf(l.getUserId()), AccessLog::getUserName),
                top(logs, l -> String.valueOf(l.getResourceId()), AccessLog::getResourceName),
                buildLogRows(logs)
        );
    }

    // ───────────── Biểu đồ ─────────────

    private List<TrendPoint> buildTrend(List<AccessLog> logs, LocalDate from, LocalDate to) {
        long days = ChronoUnit.DAYS.between(from, to) + 1;

        // Chỉ 1 ngày: chia 6 khung 4 giờ
        if (days == 1) {
            long[] ok = new long[6], blocked = new long[6];
            for (AccessLog l : logs) {
                int i = l.getCreatedAt().atZone(ZONE).getHour() / 4;
                if (l.getDecision() == Decision.ALLOW) ok[i]++; else blocked[i]++;
            }
            List<TrendPoint> points = new ArrayList<>();
            for (int i = 0; i < 6; i++) {
                points.add(new TrendPoint(String.format("%02d:00", i * 4), ok[i], blocked[i]));
            }
            return points;
        }

        // Nhiều ngày: mỗi điểm gộp "step" ngày để biểu đồ không quá dày
        int step = (int) Math.ceil(days / (double) MAX_POINTS);
        Map<LocalDate, long[]> byDay = new HashMap<>();
        for (AccessLog l : logs) {
            LocalDate d = l.getCreatedAt().atZone(ZONE).toLocalDate();
            long[] c = byDay.computeIfAbsent(d, k -> new long[2]);
            if (l.getDecision() == Decision.ALLOW) c[0]++; else c[1]++;
        }

        List<TrendPoint> points = new ArrayList<>();
        for (LocalDate d = from; !d.isAfter(to); d = d.plusDays(step)) {
            long ok = 0, blocked = 0;
            for (int i = 0; i < step && !d.plusDays(i).isAfter(to); i++) {
                long[] c = byDay.getOrDefault(d.plusDays(i), new long[2]);
                ok += c[0];
                blocked += c[1];
            }
            points.add(new TrendPoint(d.format(DAY), ok, blocked));
        }
        return points;
    }

    // ───────────── Top người dùng / ứng dụng ─────────────

    private List<TopItem> top(List<AccessLog> logs,
                              Function<AccessLog, String> idOf,
                              Function<AccessLog, String> nameOf) {
        Map<String, Long> counts = logs.stream()
                .collect(Collectors.groupingBy(idOf, Collectors.counting()));
        Map<String, String> names = new HashMap<>();
        logs.forEach(l -> names.putIfAbsent(idOf.apply(l), nameOf.apply(l)));

        return counts.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(TOP_SIZE)
                .map(e -> new TopItem(e.getKey(), names.get(e.getKey()), e.getValue()))
                .toList();
    }

    // ───────────── Log chi tiết ─────────────

    private List<LogRow> buildLogRows(List<AccessLog> logs) {
        // Lấy loại ứng dụng (WEB/SSH/MYSQL) để hiện ở cột "Giao thức"
        Set<UUID> ids = logs.stream().map(AccessLog::getResourceId)
                .filter(Objects::nonNull).collect(Collectors.toSet());
        Map<UUID, String> typeById = resourceRepository.findAllById(ids).stream()
                .collect(Collectors.toMap(Resource::getId, r -> String.valueOf(r.getType())));

        return logs.stream()
                .limit(MAX_LOGS)
                .map(l -> new LogRow(
                        String.valueOf(l.getId()),
                        l.getCreatedAt(),
                        l.getUserName(),
                        l.getResourceName(),
                        typeById.getOrDefault(l.getResourceId(), "—"),
                        l.getDecision().name(),
                        l.getReason()))
                .toList();
    }
}
