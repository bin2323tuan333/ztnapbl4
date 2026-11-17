package com.example.configuration;

import lombok.Data;
import lombok.Value;

@Data
public class ConnectorConfig {
    private final String SERVER_URL;
    private final String TOKEN;


    private static ConnectorConfig instance = null;
    public static ConnectorConfig getInstance() {
        if (instance == null) instance = new ConnectorConfig();
        return instance;
    }

    private ConnectorConfig() {
        this.SERVER_URL = getEnv("CONNECTOR_SERVER_URL");
        this.TOKEN = getEnv("CONNECTOR_TOKEN");
    }

    private String getEnv(String key) {
        String value = System.getenv(key);
        return value;
    }



    public String getSERVER_URL() {
        return SERVER_URL;
    }

    public String getTOKEN() {
        return TOKEN;
    }
}
