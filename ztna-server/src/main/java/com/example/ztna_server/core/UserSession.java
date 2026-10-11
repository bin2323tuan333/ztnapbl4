package com.example.ztna_server.core;

import io.netty.channel.Channel;
import lombok.Value;

import java.time.Instant;

@Value
public class UserSession {
    String userId;
    Channel channel;
    Instant connectedAt;
}