package com.example.ztna_server.config;

import com.example.ztna_server.netty.ConnectorWsHandler;
import com.example.ztna_server.netty.UserWsHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class NettyConfig {
    private final ConnectorWsHandler connectorWsHandler;
    private final UserWsHandler userWsHandler;

    private EventLoopGroup userBoss;
    private EventLoopGroup userWorker;
    private EventLoopGroup connectorBoss;
    private EventLoopGroup connectorWorker;

    private Channel userChannel;
    private Channel connectorChannel;

    public NettyConfig(ConnectorWsHandler connectorWsHandler,
                       UserWsHandler userWsHandler) {
        this.connectorWsHandler = connectorWsHandler;
        this.userWsHandler = userWsHandler;
    }

    @PostConstruct
    public void start() throws InterruptedException {
        startUserServer();
        startConnectorServer();
    }

    private void startUserServer() throws InterruptedException {
        userBoss = new NioEventLoopGroup(1);
        userWorker = new NioEventLoopGroup();

        ServerBootstrap b = new ServerBootstrap();
        b.group(userBoss, userWorker)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) {
                        ch.pipeline()
                                .addLast(new HttpServerCodec())
                                .addLast(new HttpObjectAggregator(65536))
                                .addLast(new WebSocketServerProtocolHandler(
                                        "/ws",
                                        null,
                                        true,
                                        65536,
                                        false,
                                        true
                                ))
                                .addLast(userWsHandler);
                    }
                });

        userChannel = b.bind(443).sync().channel();
        log.info("User server listening on port 443");
    }

    private void startConnectorServer() throws InterruptedException {
        connectorBoss = new NioEventLoopGroup(1);
        connectorWorker = new NioEventLoopGroup();

        ServerBootstrap b = new ServerBootstrap();
        b.group(connectorBoss, connectorWorker)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) {
                        ch.pipeline()
                                .addLast(new HttpServerCodec())
                                .addLast(new HttpObjectAggregator(65536))
                                .addLast(new WebSocketServerProtocolHandler(
                                        "/ws",
                                        null,
                                        true,
                                        65536,
                                        false,
                                        true
                                ))
                                .addLast(connectorWsHandler);
                    }
                });

        connectorChannel = b.bind(8443).sync().channel();
        log.info("Connector server listening on port 8443");
    }

    @PreDestroy
    public void stop() {
        if (userChannel != null) userChannel.close();
        if (connectorChannel != null) connectorChannel.close();
        if (userBoss != null) userBoss.shutdownGracefully();
        if (userWorker != null) userWorker.shutdownGracefully();
        if (connectorBoss != null) connectorBoss.shutdownGracefully();
        if (connectorWorker != null) connectorWorker.shutdownGracefully();
        log.info("Netty servers stopped gracefully");
    }
}