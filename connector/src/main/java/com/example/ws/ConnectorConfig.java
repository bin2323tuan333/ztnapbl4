package com.example.ws;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@Data
public class ConnectorConfig {
    private static ConnectorConfig instance = null;

    public static ConnectorConfig getInstance() {
        if (instance == null) instance = new ConnectorConfig();
        return instance;
    }

    private ConnectorConfig() {}


    private final String SERVER_URL ="ws://localhost:8443/connector";

    private final String TOKEN = "toilaconnector1";


    public String getSERVER_URL() {
        return SERVER_URL;
    }

    public String getTOKEN() {
        return TOKEN;
    }
}
