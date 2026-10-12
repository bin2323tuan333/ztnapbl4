package com.example.ztna_server.routes;

import com.example.ztna_server.dto.App;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class RoutingTable {
//    appId → AppRoute{connectorId, App}
    private final Map<String, AppRoute> routes = new ConcurrentHashMap<>();

//    connectorId → Set<appId>
    private final Map<String, Set<String>> connectorIndex = new ConcurrentHashMap<>();


    public void loadRouting(String connectorId, List<App> appList) {
        Set<String> newAppIds = ConcurrentHashMap.newKeySet();

        for (App app : appList) {
            AppRoute old = routes.put(app.getId(), new AppRoute(connectorId, app));

            if (old != null && !old.getConnectorId().equals(connectorId)) {
                removeFromIndex(old.getConnectorId(), app.getId());
            }
            newAppIds.add(app.getId());
        }

        Set<String> oldAppIds = connectorIndex.put(connectorId, newAppIds);

        if (oldAppIds != null) {
            for (String oldAppId : oldAppIds) {
                if (!newAppIds.contains(oldAppId)) {
                    routes.remove(oldAppId);
                }
            }
        }

        log.info("[ROUTING] Loaded {} apps for connector {}", appList.size(), connectorId);
    }

    public void clearConnector(String connectorId) {
        Set<String> appIds = connectorIndex.remove(connectorId);
        if (appIds != null) {
            appIds.forEach(routes::remove);
            log.info("[ROUTING] Cleared {} apps of connector {}", appIds.size(), connectorId);
        }
    }

    private void removeFromIndex(String connectorId, String appId) {
        Set<String> set = connectorIndex.get(connectorId);
        if (set != null) {
            set.remove(appId);
            if (set.isEmpty()) {
                connectorIndex.remove(connectorId);
            }
        }
    }


    public String getConnectorId(String appId) {
        AppRoute r = routes.get(appId);
        return r == null ? null : r.getConnectorId();
    }

    public App getApp(String appId) {
        AppRoute r = routes.get(appId);
        return r == null ? null : r.getApp();
    }

    public AppRoute getAppRoute(String appId) {
        AppRoute r = routes.get(appId);
        return r;
    }

    public List<App> getAppsByConnector(String connectorId) {
        Set<String> ids = connectorIndex.get(connectorId);
        if (ids == null) return List.of();
        return ids.stream()
                .map(routes::get)
                .filter(Objects::nonNull)
                .map(AppRoute::getApp)
                .toList();
    }

    public boolean hasApp(String appId) {
        return routes.containsKey(appId);
    }

    public int totalApps() {
        return routes.size();
    }
}
