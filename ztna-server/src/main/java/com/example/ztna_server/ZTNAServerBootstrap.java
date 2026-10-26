package com.example.ztna_server;

import com.example.ztna_server.dto.App;
import com.example.ztna_server.netty.ConnectorWsHandler;
import com.example.ztna_server.netty.ResourceWsHandler;
import com.example.ztna_server.routes.RoutingTable;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.timeout.IdleStateHandler;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class ZTNAServerBootstrap {
    private static final int CONNECTOR_PORT = 8443;
    private static final int APP_PORT = 9443;
    private static final int MAX_FRAME_SIZE = 1024 * 1024;

    private static final String DEMO_CONNECTOR_ID = "conn_01";

    private final ObjectProvider<ConnectorWsHandler> connectorProvider;
    private final ObjectProvider<ResourceWsHandler> appProvider;
    private final RoutingTable routingTable;

    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;

    private Channel connectorChannel;
    private Channel appChannel;


    @PostConstruct
    public void start() throws InterruptedException {
        hardCodeApps();
        bossGroup = new NioEventLoopGroup(1);
        workerGroup = new NioEventLoopGroup();

        startConnectorServer();
        startAppServer();

        log.info("═".repeat(60));
        log.info("[Server] Admin HTTPS:  https://localhost:443    (Spring Boot)");
        log.info("[Server] Connector WS: ws://localhost:{}/connector", CONNECTOR_PORT);
        log.info("[Server] App WS:       ws://localhost:{}/app", APP_PORT);
        log.info("═".repeat(60));
    }

    private void hardCodeApps() {
        List<App> apps = List.of(
                App.builder()
                        .id("app_web_01")
                        .name("Demo Web")
                        .host("localhost")
                        .port(8082)
                        .build(),
                App.builder()
                        .id("app_ssh_01")
                        .name("Demo SSH")
                        .host("localhost")
                        .port(8083)
                        .build(),
                App.builder()
                        .id("app_mysql_01")
                        .name("Demo MySQL")
                        .host("localhost")
                        .port(8081)
                        .build()
        );

        routingTable.loadRouting(DEMO_CONNECTOR_ID, apps);

        log.info("═".repeat(60));
        log.info("[BOOTSTRAP] Hard-coded {} apps for connector {}",
                apps.size(), DEMO_CONNECTOR_ID);
        apps.forEach(a -> log.info("  • {} ({}) → {}:{}",
                a.getId(), a.getName(), a.getHost(), a.getPort()));
        log.info("═".repeat(60));
    }

    private void startAppServer() throws InterruptedException {
        ServerBootstrap b = new ServerBootstrap();
        b.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) {
                        ch.pipeline()
                                .addLast(new IdleStateHandler(60, 30, 0, TimeUnit.SECONDS))
                                .addLast(new HttpServerCodec())
                                .addLast(new HttpObjectAggregator(65536))
                                .addLast(new io.netty.channel.ChannelInboundHandlerAdapter() {
                                    @Override
                                    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                                        log.info("[PIPE] Nhận: {}", msg.getClass().getSimpleName());
                                        ctx.fireChannelRead(msg);
                                    }
                                })
                                .addLast(new WebSocketServerProtocolHandler(
                                        "/app",
                                        null,
                                        true,
                                        MAX_FRAME_SIZE,
                                        false,
                                        true
                                ))
                                .addLast(appProvider.getObject());
                    }
                });

        appChannel = b.bind(APP_PORT).sync().channel();
        log.info("[Server] App WS bound on port {}", APP_PORT);
    }


    private void startConnectorServer() throws InterruptedException {
        ServerBootstrap b = new ServerBootstrap();
        b.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) {
                        ch.pipeline()
                                .addLast(new IdleStateHandler(60, 30, 0, TimeUnit.SECONDS))
                                .addLast(new HttpServerCodec())
                                .addLast(new HttpObjectAggregator(65536))

                                .addLast(new WebSocketServerProtocolHandler(
                                        "/connector",
                                        null,
                                        true,
                                        MAX_FRAME_SIZE,
                                        false,
                                        true
                                ))
                                .addLast(connectorProvider.getObject());
                    }
                });

        connectorChannel = b.bind(CONNECTOR_PORT).sync().channel();
        log.info("[Server] Connector WS bound on port {}", CONNECTOR_PORT);
    }

    @PreDestroy
    public void stop() {
        log.info("[Server] Shutting down...");

        if (connectorChannel != null) connectorChannel.close();
        if (appChannel != null) appChannel.close();

        if (bossGroup != null) bossGroup.shutdownGracefully();
        if (workerGroup != null) workerGroup.shutdownGracefully();

        log.info("[Server] Stopped");
    }
}