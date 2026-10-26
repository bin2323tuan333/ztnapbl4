package org.example.listener;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.example.stream.StreamRegistry;
import org.example.tunnel.TunnelClient;

import java.net.InetSocketAddress;

public class LocalServer {
    private final String resourceId;
    private final EventLoopGroup bossGroup;
    private final EventLoopGroup workerGroup;
    private final TunnelClient tunnelClient;
    private final StreamRegistry streamRegistry;

    private Channel channel;
    private String streamId;
    private String jwt;

    public LocalServer(String resourceId,
                       String jwt,
                       EventLoopGroup bossGroup,
                       EventLoopGroup workerGroup,
                       TunnelClient tunnelClient,
                       StreamRegistry streamRegistry) {
        this.resourceId = resourceId;
        this.bossGroup = bossGroup;
        this.workerGroup = workerGroup;
        this.tunnelClient = tunnelClient;
        this.jwt = jwt;
        this.streamRegistry = streamRegistry;
    }

    public int start() throws Exception {
        ServerBootstrap b = new ServerBootstrap();
        b.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(new LocalServerInitializer(resourceId, tunnelClient, streamRegistry, jwt));


        channel = b.bind("127.0.0.1", 0).sync().channel();
        int port = ((InetSocketAddress) channel.localAddress()).getPort();
        System.out.println("[LocalServer:" + resourceId + "] listen 127.0.0.1:" + port);
        return port;
    }

    public Channel getChannel() {
        return channel;
    }

    public String getStreamId() {
        return streamId;
    }

    public void setStreamId(String streamId) {
        this.streamId = streamId;
    }
}