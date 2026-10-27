package com.example.ws;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@Data
@NoArgsConstructor
public class ConnectorConfig {
    private String serverUrl;
    private String token;
    private Map<String, String> services;

    public ConnectorConfig(String serverUrl, String token) {
        this.serverUrl = serverUrl;
        this.token = token;
    }
}
