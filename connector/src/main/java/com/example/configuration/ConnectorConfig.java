package com.example.configuration;

import lombok.Data;

@Data
public class ConnectorConfig {
    private final String SERVER_URL = System.getenv("SERVER_URL");
    private final String TOKEN = System.getenv("CONNECTOR_TOKEN");


    private static ConnectorConfig instance = null;
    public static ConnectorConfig getInstance() {
        if (instance == null) instance = new ConnectorConfig();
        return instance;
    }

    private ConnectorConfig() {}



    public String getSERVER_URL() {
        return SERVER_URL;
    }

    public String getTOKEN() {
        return TOKEN;
    }
}
