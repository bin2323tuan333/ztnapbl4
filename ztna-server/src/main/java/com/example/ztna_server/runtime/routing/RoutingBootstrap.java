package com.example.ztna_server.runtime.routing;

import com.example.ztna_server.entity.Resource;
import com.example.ztna_server.repository.ResourceRepository;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class RoutingBootstrap {
    private final RoutingRegistry    routingRegistry;
    private final ResourceRepository resourceRepository;

    @EventListener(ApplicationReadyEvent.class)
    @Transactional(readOnly = true)
    public void loadOnStartup() {
        try {
            refresh();
            log.info("[Routing] Bootstrap: loaded routes");
        } catch (Exception e) {
            log.error("[Routing] Bootstrap failed {}", e.getMessage());
        }
    }

    @Scheduled(fixedDelayString = "10000", initialDelayString = "3000")
    @Transactional(readOnly = true)
    public void poll() {
        try {
            refresh();
        } catch (Exception e) {
            log.error("[Routing] Poll failed after {} ms", e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public void refresh() {
        List<Resource> resources = resourceRepository.findAllByEnabledTrueAndConnectorEnabledTrue();

        Map<String, RouteTarget> targets = new HashMap<>(resources.size() * 2);
        for (Resource res : resources) {
            RouteTarget t = mapResource(res);
            targets.put(t.resourceId(), t);
        }

        routingRegistry.replaceAll(targets);
    }

    private RouteTarget mapResource(Resource res) {
        return new RouteTarget(
                res.getId().toString(),
                res.getName(),
                res.getHost(),
                res.getPort(),
                res.getConnector().getId().toString());
    }

    @PreDestroy
    public void shutdown() {
        log.info("[Routing] Shutdown, final registry size={}", routingRegistry.size());
    }
}