package org.example.listener;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.example.Factory;
import org.example.cache.StreamRegistry;
import org.example.tunnel.TunnelClient;

import java.net.InetSocketAddress;

public class LocalServer {
    private final EventLoopGroup bossGroup = Factory.bossGroup;
    private final EventLoopGroup workerGroup = Factory.workerGroup;
    private final TunnelClient tunnelClient = Factory.tunnelClient;
    private final StreamRegistry streamRegistry = Factory.streamRegistry;

    private Channel channel;
    private String resourceId;

    public LocalServer(String resourceId) {
        this.resourceId = resourceId;
    }

    public int start()  {
        ServerBootstrap b = new ServerBootstrap();
        b.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(new LocalServerInitializer(resourceId));

        try {
            channel = b.bind("127.0.0.1", 0).sync().channel();
            int port = ((InetSocketAddress) channel.localAddress()).getPort();
            return port;
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return 0;
    }


    public Channel getChannel() {
        return channel;
    }

    public String getResourceId() {
        return resourceId;
    }
}