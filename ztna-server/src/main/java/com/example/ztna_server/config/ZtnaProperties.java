package com.example.ztna_server.config;

import com.example.ztna_server.config.properties.ConnectorProperties;
import com.example.ztna_server.config.properties.JwtProperties;
import com.example.ztna_server.config.properties.NettyProperties;

import lombok.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Value
@Component
@ConfigurationProperties(prefix = "ztna")
public class ZtnaProperties {

    NettyProperties netty;
    JwtProperties jwt;
    ConnectorProperties connector;
}