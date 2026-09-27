package com.example.ztna_connector.tcp;

import io.netty.channel.Channel;

public class TcpSession {

    private final long streamId;
    private final String service;
    private final Channel tcpChannel;
    private final Channel wsChannel;

    public TcpSession(long streamId, String service, Channel tcpChannel, Channel wsChannel) {
        this.streamId = streamId;
        this.service = service;
        this.tcpChannel = tcpChannel;
        this.wsChannel = wsChannel;
    }

    public long getStreamId() { return streamId; }
    public String getService() { return service; }
    public Channel getTcpChannel() { return tcpChannel; }
    public Channel getWsChannel() { return wsChannel; }
}