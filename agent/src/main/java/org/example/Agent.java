package org.example;

import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;

public class Agent {
    public static void main(String[] args) throws Exception {
        AgentBootstrap bootstrap = new AgentBootstrap(AgentConfig.load());
        bootstrap.start();
        AgentController controller = new AgentController(bootstrap);

        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 9999), 0);
        // Bổ sung ThreadPool để xử lý request đồng thời
        server.setExecutor(Executors.newCachedThreadPool());

        server.createContext("/open", ex -> {
            try {
                if ("OPTIONS".equalsIgnoreCase(ex.getRequestMethod())) {
                    ex.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
                    ex.getResponseHeaders().add("Access-Control-Allow-Headers", "Authorization, Content-Type");
                    ex.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, OPTIONS");
                    ex.sendResponseHeaders(204, -1);
                    return;
                }

                Map<String, String> p = parseQuery(ex.getRequestURI().getQuery());
                String appId = p.get("appId");
                String appType = p.get("type");
                if (appId == null) throw new IllegalArgumentException("Thiếu tham số appId");

                String jwt = "";
                String authHeader = ex.getRequestHeaders().getFirst("Authorization");
                if (authHeader != null && authHeader.startsWith("Bearer ")) {
                    jwt = authHeader.substring(7);
                }

                Map<String, String> params = new HashMap<>();
                params.put("jwt", jwt);

                int port = controller.openApp(appId, appType, params);

                String resp = "{\"status\":\"success\",\"port\":" + port + "}";
                ex.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
                ex.getResponseHeaders().set("Content-Type", "application/json");
                ex.sendResponseHeaders(200, resp.getBytes(StandardCharsets.UTF_8).length);
                ex.getResponseBody().write(resp.getBytes(StandardCharsets.UTF_8));

            } catch (Exception e) {
                String errMsg = e.getMessage() == null ? "Unknown error" : e.getMessage().replace("\"", "'");
                String err = "{\"error\":\"" + errMsg + "\"}";
                ex.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
                ex.sendResponseHeaders(500, err.getBytes(StandardCharsets.UTF_8).length);
                ex.getResponseBody().write(err.getBytes(StandardCharsets.UTF_8));
            } finally {
                ex.close();
            }
        });

        server.start();
        System.out.println("[Agent] HTTP Server đang chạy tại http://127.0.0.1:9999");
        Thread.currentThread().join();
    }

    private static Map<String, String> parseQuery(String query) {
        Map<String, String> map = new HashMap<>();
        if (query == null || query.isBlank()) return map;
        for (String pair : query.split("&")) {
            String[] kv = pair.split("=", 2);
            if (kv.length == 2) {
                try {
                    map.put(URLDecoder.decode(kv[0], StandardCharsets.UTF_8), URLDecoder.decode(kv[1], StandardCharsets.UTF_8));
                } catch (Exception ignored) {}
            }
        }
        return map;
    }
}