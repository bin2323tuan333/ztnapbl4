package com.example.configuration;

import lombok.Data;

@Data
public class ConnectorConfig {
    private final String SERVER_URL = "ws://localhost:8443/connector";
    private final String TOKEN = "toilaconnector1";


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
