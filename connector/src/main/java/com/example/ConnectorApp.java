package com.example;

import com.example.tcp.TcpClient;
import com.example.ws.ConnectorClient;
import com.example.ws.ConnectorConfig;
import com.example.ws.ConnectorHandler;

import java.util.Map;

public class ConnectorApp {

    public static void main(String[] args) {
        ConnectorClient connectorClient = ConnectorClient.getInstance();
        connectorClient.start();
    }
}