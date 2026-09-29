package com.example.ztna_server.core;

import io.netty.channel.Channel;
import lombok.*;


@Value
public class StreamSession {
    private int streamId;
    private Channel user;
    private Channel connector;
    private String service;
}
