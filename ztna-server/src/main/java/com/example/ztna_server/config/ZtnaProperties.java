package com.example.ztna_server.config;

import com.example.ztna_server.config.properties.ConnectorProperties;
import com.example.ztna_server.config.properties.JwtProperties;
import com.example.ztna_server.config.properties.NettyProperties;
import lombok.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Value
public class ZtnaProperties {
    NettyProperties netty;
    JwtProperties jwt;
    ConnectorProperties connector;
}