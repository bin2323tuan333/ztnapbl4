package org.example;

public class AgentConfig {
    private static AgentConfig instance;

    private String gatewayUrl = "ws://localhost:9443/app";

    private AgentConfig() {
    }

    public static synchronized AgentConfig getInstance() {
        if (instance == null) {
            instance = new AgentConfig();
            System.out.println("[AgentConfig] Đã khởi tạo Singleton. Gateway URL: " + instance.gatewayUrl);
        }
        return instance;
    }

    public static AgentConfig load() {
        return getInstance();
    }

    public String getGatewayUrl() {
        return gatewayUrl;
    }

    public void setGatewayUrl(String gatewayUrl) {
        this.gatewayUrl = gatewayUrl;
    }
}