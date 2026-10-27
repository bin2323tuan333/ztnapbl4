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
                "toilaconnector1"
        );

        TcpClient tcpClient = new TcpClient();
        ConnectorClient client = new ConnectorClient(config, () -> new ConnectorHandler(config, tcpClient));
        client.start();
    }
}