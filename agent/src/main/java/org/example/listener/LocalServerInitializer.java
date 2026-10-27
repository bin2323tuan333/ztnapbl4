package org.example.listener;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.socket.SocketChannel;
import org.example.stream.StreamRegistry;
import org.example.tunnel.TunnelClient;

public class LocalServerInitializer extends ChannelInitializer<SocketChannel> {
    private final String resourceId;
    private final TunnelClient tunnelClient;
    private final StreamRegistry streamRegistry;

    private String streamId;
    private String jwt;

    public LocalServerInitializer(String resourceId, String streamId, TunnelClient tunnelClient, StreamRegistry streamRegistry) {
        this.resourceId = resourceId;
        this.streamId = streamId;
        this.tunnelClient = tunnelClient;
        this.streamRegistry = streamRegistry;
    }

    @Override
    protected void initChannel(SocketChannel ch) {
        ch.config().setOption(ChannelOption.TCP_NODELAY, true);
        ch.pipeline().addLast(new LocalHandler(resourceId, streamId, tunnelClient, streamRegistry));
    }
}