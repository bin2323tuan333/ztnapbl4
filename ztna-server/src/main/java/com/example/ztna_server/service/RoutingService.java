package com.example.ztna_server.service;

import com.example.ztna_server.dto.App;
import com.example.ztna_server.entity.Connector;
import com.example.ztna_server.entity.ConnectorResource;
import com.example.ztna_server.entity.Resource;
import com.example.ztna_server.protocol.frames.RoutingResFrame;
import com.example.ztna_server.repository.ConnectorRepository;
import com.example.ztna_server.repository.ConnectorResourceRepository;
import com.example.ztna_server.routes.ConnectionRegister;
import com.example.ztna_server.routes.RoutingTable;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

/**
 * Nạp bảng định tuyến (app nào đi qua connector nào) từ database,
 * thay cho danh sách app hard-code trong ZTNAServerBootstrap.
 * Connector được nhận diện bằng tên (vd "conn_01") — chính là connectorId trong ROUTING_REQUEST.
 * Id của app trong bảng định tuyến = id (UUID) của Resource trong database.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RoutingService {

    private final ConnectorRepository connectorRepository;
    private final ConnectorResourceRepository connectorResourceRepository;
    private final RoutingTable routingTable;
    private final ConnectionRegister connectionRegister;

    /** Chạy 1 lần khi server khởi động xong (sau DataInit). */
    @EventListener(ApplicationReadyEvent.class)
    @Transactional(readOnly = true)
    public void reloadAllOnStartup() {
        for (Connector connector : connectorRepository.findAll()) {
            reload(connector.getName());
        }
    }

    /**
     * Đọc lại danh sách app của 1 connector từ database và cập nhật bảng định tuyến.
     * Chỉ lấy app đang bật, liên kết đang bật, connector đang bật.
     */
    @Transactional(readOnly = true)
    public List<App> reload(String connectorName) {
        List<App> apps = new ArrayList<>();

        connectorRepository.findByName(connectorName)
                .filter(Connector::isEnabled)
                .ifPresent(connector -> {
                    for (ConnectorResource link : connectorResourceRepository.findByConnectorId(connector.getId())) {
                        Resource r = link.getResource();
                        if (!link.isEnabled() || !Boolean.TRUE.equals(r.getEnabled())) continue;

                        App app = toApp(r);
                        if (app != null) apps.add(app);
                    }
                });

        routingTable.loadRouting(connectorName, apps);
        log.info("[ROUTING] {} → {} app từ database", connectorName, apps.size());
        apps.forEach(a -> log.info("  • {} ({}) → {}:{}", a.getId(), a.getName(), a.getHost(), a.getPort()));
        return apps;
    }

    /**
     * Đọc lại bảng định tuyến và, nếu connector đang online, gửi ngay ROUTING_RESPONSE mới
     * để connector cập nhật mà không cần kết nối lại.
     */
    @Transactional(readOnly = true)
    public void reloadAndPush(String connectorName) {
        List<App> apps = reload(connectorName);

        Channel channel = connectionRegister.getChannel(connectorName);
        if (channel == null || !channel.isActive()) return;

        RoutingResFrame res = new RoutingResFrame();
        res.setAppCount(apps.size());
        res.setApps(apps);
        channel.writeAndFlush(new BinaryWebSocketFrame(Unpooled.wrappedBuffer(res.encode())));
        log.info("[ROUTING] Đã gửi bảng định tuyến mới cho {}", connectorName);
    }

    /** Đổi Resource trong database thành App của giao thức (id, tên, host, port). */
    private App toApp(Resource r) {
        HostPort hp = parseBackendUrl(r.getBackendUrl());
        if (hp == null) {
            log.warn("[ROUTING] Bỏ qua app {}: backendUrl không hợp lệ '{}'", r.getName(), r.getBackendUrl());
            return null;
        }
        return App.builder()
                .id(r.getId().toString())
                .name(r.getName())
                .host(hp.host())
                .port(hp.port())
                .build();
    }

    /**
     * Tách host và port từ backendUrl. Chấp nhận "http://10.0.0.5:8080", "tcp://db:3306", "10.0.0.5:22".
     * Không ghi port thì http = 80, https = 443. Trả null nếu không hợp lệ.
     */
    public static HostPort parseBackendUrl(String url) {
        if (url == null || url.isBlank()) return null;
        try {
            String u = url.trim();
            URI uri = URI.create(u.contains("://") ? u : "tcp://" + u);
            String host = uri.getHost();
            int port = uri.getPort();
            if (port == -1) {
                port = switch (uri.getScheme().toLowerCase()) {
                    case "http" -> 80;
                    case "https" -> 443;
                    default -> -1;
                };
            }
            if (host == null || host.isBlank() || port < 1 || port > 65535) return null;
            return new HostPort(host, port);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public record HostPort(String host, int port) {}
}
