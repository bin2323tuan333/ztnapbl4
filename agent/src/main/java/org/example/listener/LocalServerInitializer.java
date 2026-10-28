package org.example.listener;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.socket.SocketChannel;
import org.example.Factory;
import org.example.cache.StreamRegistry;
import org.example.tunnel.TunnelClient;

public class LocalServerInitializer extends ChannelInitializer<SocketChannel> {
    private final TunnelClient tunnelClient = Factory.tunnelClient;
    private final StreamRegistry streamRegistry = Factory.streamRegistry;

    private String resourceId;

    public LocalServerInitializer(String resourceId) {
        this.resourceId = resourceId;
    }

    @Override
    protected void initChannel(SocketChannel ch) {
        ch.config().setOption(ChannelOption.TCP_NODELAY, true);
        ch.pipeline().addLast(new LocalHandler(resourceId));
    }
}