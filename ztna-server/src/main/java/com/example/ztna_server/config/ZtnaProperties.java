package com.example.ztna_server.config;

import lombok.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Value
@ConfigurationProperties(prefix = "ztna")
public class ZtnaProperties {
    Netty netty;
    Jwt jwt;
    Connector connector;

    @Value
    public static class Netty {
        int port;
        int bossThreads;
        int workerThreads;
    }

    @Value
    public static class Jwt {
        String secret;
        int ttlMinutes;
    }

    @Value
    public static class Connector {
        String token;
    }
}