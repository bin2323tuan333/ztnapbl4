package org.example;

import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

public class Agent {
    public static void main(String[] args) throws Exception {
        AgentBootstrap bootstrap = new AgentBootstrap(AgentConfig.load());
        bootstrap.start();
        AgentController controller = new AgentController(bootstrap);

        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 9999), 0);
        server.createContext("/open", ex -> {
            try {
                if ("OPTIONS".equalsIgnoreCase(ex.getRequestMethod())) {
                    ex.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
                    ex.sendResponseHeaders(204, -1);
                    return;
                }

                Map<String, String> p = parseQuery(ex.getRequestURI().getQuery());

                String appId = p.get("appId");
                String appType = appId.contains("web") ? "web"
                        : appId.contains("mysql") ? "mysql" : "ssh";

                Map<String, String> params = new HashMap<>();
                params.put("jwt", p.get("jwt"));

                int port = controller.openApp(appId, appType, params);

                String resp = "{\"status\":\"success\",\"port\":" + port + "}";
                ex.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
                ex.sendResponseHeaders(200, resp.getBytes().length);
                ex.getResponseBody().write(resp.getBytes());

            } catch (Exception e) {
                String err = "{\"error\":\"" + e.getMessage() + "\"}";
                ex.sendResponseHeaders(500, err.getBytes().length);
                ex.getResponseBody().write(err.getBytes());
            } finally {
                ex.close();
            }
        });

        server.start();
        Thread.currentThread().join();
    }

    private static Map<String, String> parseQuery(String query) {
        Map<String, String> map = new HashMap<>();
        if (query == null || query.isBlank()) return map;
        for (String pair : query.split("&")) {
            String[] kv = pair.split("=", 2);
            if (kv.length == 2) map.put(kv[0], kv[1]);
        }
        return map;
    }
}