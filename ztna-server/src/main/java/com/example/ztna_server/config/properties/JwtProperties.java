package com.example.ztna_server.config.properties;

import lombok.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Value
@ConfigurationProperties(prefix = "ztna.jwt")
public class JwtProperties {
    String secret;
    int ttlMinutes;
}
