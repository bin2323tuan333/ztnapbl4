package com.example.ztna_server.routes;

import com.example.ztna_server.dto.App;
import com.example.ztna_server.entity.Resource;
import com.example.ztna_server.repository.ResourceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RoutingManager {

    private final RoutingTable routingTable;
    private final ResourceRepository resourceRepository;

    @EventListener(ApplicationReadyEvent.class)
    public void init() {
        log.info("[ROUTING-MANAGER] Khởi động: Đang load dữ liệu từ Database vào Routing Table...");
        refreshRoutingTable();
    }

    @Scheduled(fixedDelay = 60000)
    @Transactional(readOnly = true)
    public synchronized void refreshRoutingTable() {
        log.info("[ROUTING-MANAGER] Bắt đầu quét DB để làm mới Routing Table...");
        List<Resource> activeResources = resourceRepository.findAllByEnabledTrueAndConnectorEnabledTrue();

        Map<String, List<Resource>> byConnector = activeResources.stream()
                .collect(Collectors.groupingBy(
                        res -> res.getConnector().getId().toString()
                ));

        routingTable.clearAll();

        byConnector.forEach((connectorId, resources) -> {
            List<App> appList = resources.stream()
                    .map(res -> {
                        App app = new App();
                        app.setId(res.getId().toString());
                        app.setName(res.getName());
                        app.setHost(res.getHost());
                        app.setPort(res.getPort());
                        return app;
                    })
                    .toList();

            routingTable.loadRouting(connectorId, appList);
        });

        log.info("[ROUTING-MANAGER] Đã load xong {} apps từ DB vào Cache", activeResources.size());
    }
}