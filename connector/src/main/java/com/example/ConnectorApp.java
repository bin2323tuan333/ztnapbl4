package com.example;

import com.example.tcp.TcpClient;
import com.example.ws.ConnectorClient;
import com.example.ws.ConnectorConfig;
import com.example.ws.ConnectorHandler;

import java.util.Map;

public class ConnectorApp {

    public static void main(String[] args) {
        ConnectorConfig config = new ConnectorConfig(
                "ws://localhost:8443/connector",
                "connector-demo-token-abc123",
                Map.of(
                        "nginx", "127.0.0.1:8082",
                        "ssh",   "127.0.0.1:8083",
                        "mysql", "127.0.0.1:8081"
                )
        );

        TcpClient tcpClient = new TcpClient();
        ConnectorClient client = new ConnectorClient(config, () -> new ConnectorHandler(config, tcpClient));
        client.start();
    }
}