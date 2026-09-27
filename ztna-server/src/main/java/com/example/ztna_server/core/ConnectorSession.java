package com.example.ztna_server.core;

import io.netty.channel.Channel;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class ConnectorSession {
    private final String id;
    private final List<String> services;
    private final Channel channel;

    @Override
    public String toString() {
        return "ConnectorSession{id='" + id + "', services=" + services + '}';
    }
}
