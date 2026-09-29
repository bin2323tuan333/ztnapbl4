package com.example.ztna_server.config.properties;

import lombok.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Value
@ConfigurationProperties(prefix = "ztna.netty")
public class NettyProperties {
    int port;
    int bossThreads;
    int workerThreads;
}
