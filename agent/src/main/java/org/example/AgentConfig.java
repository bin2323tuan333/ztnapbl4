package org.example;

import java.net.URI;

/**
 * Cấu hình của Agent (singleton).
 *
 * Địa chỉ ZTNA Server được lấy theo thứ tự ưu tiên:
 *   1. Tham số dòng lệnh đầu tiên:  java ... org.example.Agent ws://192.168.1.10:9443/app
 *   2. Biến môi trường SERVER_URL:  SERVER_URL=ws://192.168.1.10:9443/app
 *   3. Mặc định: ws://localhost:9443/app (Server chạy cùng máy)
 */
public class AgentConfig {
    public static final String DEFAULT_GATEWAY_URL = "ws://localhost:9443/app";
    public static final String ENV_SERVER_URL = "SERVER_URL";

    private static AgentConfig instance;

    private String gatewayUrl = DEFAULT_GATEWAY_URL;

    private AgentConfig() {
    }

    public static synchronized AgentConfig getInstance() {
        if (instance == null) {
            instance = new AgentConfig();
        }
        return instance;
    }

    /** Đọc địa chỉ Server từ tham số dòng lệnh / biến môi trường. Gọi một lần lúc khởi động. */
    public static AgentConfig load(String[] args) {
        AgentConfig config = getInstance();

        String fromArgs = (args != null && args.length > 0) ? args[0] : null;
        String fromEnv = System.getenv(ENV_SERVER_URL);

        String source;
        String url;
        if (fromArgs != null && !fromArgs.isBlank()) {
            url = fromArgs.trim();
            source = "tham số dòng lệnh";
        } else if (fromEnv != null && !fromEnv.isBlank()) {
            url = fromEnv.trim();
            source = "biến môi trường " + ENV_SERVER_URL;
        } else {
            url = DEFAULT_GATEWAY_URL;
            source = "mặc định";
        }

        validate(url);
        config.setGatewayUrl(url);
        System.out.println("[AgentConfig] Gateway URL: " + url + " (lấy từ " + source + ")");
        return config;
    }

    /** Chỉ hỗ trợ ws:// vì Agent chưa có TLS. Sai định dạng thì dừng ngay, báo rõ lý do. */
    private static void validate(String url) {
        URI uri;
        try {
            uri = URI.create(url);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Địa chỉ Server không hợp lệ: " + url
                    + ". Ví dụ đúng: ws://192.168.1.10:9443/app", e);
        }
        if (!"ws".equalsIgnoreCase(uri.getScheme()) || uri.getHost() == null) {
            throw new IllegalArgumentException("Địa chỉ Server phải có dạng ws://<host>:<port>/app, nhận được: " + url);
        }
    }

    public String getGatewayUrl() {
        return gatewayUrl;
    }

    public void setGatewayUrl(String gatewayUrl) {
        this.gatewayUrl = gatewayUrl;
    }
}
