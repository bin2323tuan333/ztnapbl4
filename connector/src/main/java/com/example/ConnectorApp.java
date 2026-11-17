package com.example;

import com.example.ws.ConnectorClient;

public class ConnectorApp {

    public static void main(String[] args) {
        ConnectorClient connectorClient = ConnectorClient.getInstance();
        connectorClient.start();
    }
}