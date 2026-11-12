package com.example.ztna_server.service;

import com.example.ztna_server.dto.response.dashboard.DashboardResponse;
import com.example.ztna_server.dto.response.dashboard.DashboardResponse.Stats;
import com.example.ztna_server.dto.response.dashboard.DashboardResponse.Target;
import com.example.ztna_server.dto.response.dashboard.DashboardResponse.TrafficPoint;
import com.example.ztna_server.entity.AccessLog;
import com.example.ztna_server.entity.AuditLog;
import com.example.ztna_server.enums.Decision;
import com.example.ztna_server.repository.AccessLogRepository;
import com.example.ztna_server.repository.AuditLogRepository;
import com.example.ztna_server.repository.ResourceRepository;
import com.example.ztna_server.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardService {

    // Giờ Việt Nam: "một ngày" tính từ 00:00 đến 24:00 theo giờ VN
    private static final ZoneId ZONE = ZoneId.of("Asia/Ho_Chi_Minh");
    private static final int BUCKET_HOURS = 2;          // mỗi cột biểu đồ = 2 giờ
    private static final int BUCKETS = 24 / BUCKET_HOURS;

    private final UserRepository userRepository;
    private final ResourceRepository resourceRepository;
    private final AccessLogRepository accessLogRepository;
    private final AuditLogRepository auditLogRepository;

    public DashboardResponse getDashboard(LocalDate date) {
        Instant from = date.atStartOfDay(ZONE).toInstant();
        Instant to = date.plusDays(1).atStartOfDay(ZONE).toInstant();

        List<AccessLog> accessLogs = accessLogRepository.findByCreatedAtGreaterThanEqualAndCreatedAtLessThan(from, to);
        List<AuditLog> auditLogs = auditLogRepository.findByCreatedAtGreaterThanEqualAndCreatedAtLessThan(from, to);

        return new DashboardResponse(buildStats(), buildTraffic(accessLogs, auditLogs), buildTarget(accessLogs, date));
    }

    private Stats buildStats() {
        long totalUsers = userRepository.count();
        long lockedUsers = userRepository.countByEnabled(false);
        long totalResources = resourceRepository.count();
        long enabledResources = resourceRepository.countByEnabled(true);
        return new Stats(totalUsers, totalUsers - lockedUsers, lockedUsers, totalResources, enabledResources);
    }

    // Chia ngày thành 12 khung 2 giờ, đếm số log rơi vào từng khung
    private List<TrafficPoint> buildTraffic(List<AccessLog> accessLogs, List<AuditLog> auditLogs) {
        long[] network = new long[BUCKETS];
        long[] audit = new long[BUCKETS];
        accessLogs.forEach(l -> network[bucketOf(l.getCreatedAt())]++);
        auditLogs.forEach(l -> audit[bucketOf(l.getCreatedAt())]++);

        List<TrafficPoint> points = new ArrayList<>();
        for (int i = 0; i < BUCKETS; i++) {
            String label = String.format("%02d:00", i * BUCKET_HOURS);
            points.add(new TrafficPoint(label, network[i], audit[i]));
        }
        return points;
    }

    private int bucketOf(Instant time) {
        return time.atZone(ZONE).getHour() / BUCKET_HOURS;
    }

    private Target buildTarget(List<AccessLog> todayLogs, LocalDate date) {
        long allowed = todayLogs.stream().filter(l -> l.getDecision() == Decision.ALLOW).count();
        long denied = todayLogs.size() - allowed;
        double rate = successRate(allowed, todayLogs.size());

        // So với ngày hôm trước
        Instant prevFrom = date.minusDays(1).atStartOfDay(ZONE).toInstant();
        Instant prevTo = date.atStartOfDay(ZONE).toInstant();
        List<AccessLog> prevLogs = accessLogRepository.findByCreatedAtGreaterThanEqualAndCreatedAtLessThan(prevFrom, prevTo);
        long prevAllowed = prevLogs.stream().filter(l -> l.getDecision() == Decision.ALLOW).count();
        double prevRate = successRate(prevAllowed, prevLogs.size());

        double failed = todayLogs.isEmpty() ? 0 : round1(100 - rate);
        return new Target(allowed, denied, rate, failed, round1(rate - prevRate));
    }

    private double successRate(long allowed, long total) {
        return total == 0 ? 0 : round1(allowed * 100.0 / total);
    }

    private double round1(double v) {
        return Math.round(v * 10) / 10.0;
    }
}
