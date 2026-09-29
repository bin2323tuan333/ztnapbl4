package com.example;

import java.util.Map;

public class ConnectorConfig {
    private String serverUrl;
    private String token;
    private Map<String, String> services;


    public ConnectorConfig(String serverUrl, String token, Map<String, String> services) {
        this.serverUrl = serverUrl;
        this.token = token;
        this.services = services;
    }

    public String getServerUrl() {
        return serverUrl;
    }

    public void setServerUrl(String serverUrl) {
        this.serverUrl = serverUrl;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Map<String, String> getServices() {
        return services;
    }

    public void setServices(Map<String, String> services) {
        this.services = services;
    }
}
