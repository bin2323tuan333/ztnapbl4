package org.example.ztna_connector;


import java.util.Map;

public class ConnectorApp {
    public static void main(String[] args) throws Exception {
        ConnectorConfig config = new ConnectorConfig(
                "ws://localhost:8443/ws/connector",
                "connector-demo-token-abc123",
                Map.of(
                        "nginx", "127.0.0.1:8081",
                        "ssh",   "127.0.0.1:2222",
                        "mysql", "127.0.0.1:3307"
                )
        );
        new ConnectorClient(config).start();
    }
}