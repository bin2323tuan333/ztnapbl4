package com.example.routes;

import com.example.model.App;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public class RoutingTable {
    private static RoutingTable instance = new RoutingTable();
    private volatile  Map<String, App> routes = new ConcurrentHashMap<>();


    private RoutingTable() {

    }

    public static RoutingTable getInstance() {
        return instance;
    }


    public void replaceAll(Map<String, App> newRoutes) {
        Map<String, App> fresh = new ConcurrentHashMap<>(newRoutes);
        this.routes = fresh;
    }

    public void put(App app) {
        routes.put(app.getId(), app);
    }

    public void remove(String appId) {
        routes.remove(appId);
    }

    public App get(String appId) {
        return routes.get(appId);
    }

    public boolean contains(String appId) {
        return routes.containsKey(appId);
    }

    public int size() {
        return routes.size();
    }

    public Map<String, App> getAll() {
        return new HashMap<>(this.routes);
    }


    public void clear() {
        routes.clear();
    }
}
