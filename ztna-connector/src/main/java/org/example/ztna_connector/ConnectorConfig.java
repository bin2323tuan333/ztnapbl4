package org.example.ztna_connector;


import java.util.Map;

public record ConnectorConfig(
        String serverUrl,
        String token,
        Map<String, String> services
) {}