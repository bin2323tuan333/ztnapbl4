package com.example.ztna_server.config;

import com.example.ztna_server.netty.WsPathRouter;
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
    private final ZtnaProperties props;
    private final WsPathRouter wsPathRouter;

    private EventLoopGroup connection;
    private EventLoopGroup handler;
    private Channel serverChannel;

    public NettyConfig(ZtnaProperties props, WsPathRouter wsPathRouter) {
        this.props = props;
        this.wsPathRouter = wsPathRouter;
    }

    @PostConstruct
    public void start() throws InterruptedException {
        connection = new NioEventLoopGroup(props.getNetty().getBossThreads());
        handler = new NioEventLoopGroup(props.getNetty().getWorkerThreads());

        ServerBootstrap b = new ServerBootstrap();
        b.group(connection, handler)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) {
                        ch.pipeline()
                                .addLast(new HttpServerCodec())
                                .addLast(new HttpObjectAggregator(65536))
                                .addLast(new WebSocketServerProtocolHandler("/ws", null, true, 65536, false, true))
                                .addLast(wsPathRouter);
                    }
                });

        serverChannel = b.bind(props.getNetty().getPort()).sync().channel();
        log.info("Netty WSS listening on port {}", props.getNetty().getPort());
    }

    @PreDestroy
    public void stop() {
        if (serverChannel != null) serverChannel.close();
        if (connection != null) connection.shutdownGracefully();
        if (handler != null) handler.shutdownGracefully();
        log.info("Netty stopped");
    }
}