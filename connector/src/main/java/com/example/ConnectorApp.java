package com.example.ws;

import com.example.tcp.TcpClient;
import com.example.tcp.TcpSessionManager;

import java.util.Map;

public class ConnectorApp {

    public static void main(String[] args) {
        ConnectorConfig config = new ConnectorConfig(
                "ws://localhost:8443/ws/connector",
                "connector-demo-token-abc123",
                Map.of(
                        "nginx", "127.0.0.1:8081",
                        "ssh",   "127.0.0.1:2222",
                        "mysql", "127.0.0.1:3307"
                )
        );

        TcpSessionManager sessionManager = new TcpSessionManager();
        TcpClient tcpClient = new TcpClient(sessionManager);

        ConnectorClient client = new ConnectorClient(config, () -> new ConnectorHandler(config, tcpClient, sessionManager));

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Shutdown signal received");
            client.stop();
            sessionManager.closeAll();
        }, "shutdown-hook"));

        client.start();
    }
}