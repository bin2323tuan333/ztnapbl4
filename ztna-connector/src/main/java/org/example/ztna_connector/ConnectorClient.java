package org.example.ztna_connector;


import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.websocketx.WebSocketClientProtocolHandler;
import io.netty.handler.codec.http.websocketx.WebSocketVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.concurrent.TimeUnit;

public class ConnectorClient {

    private static final Logger log = LoggerFactory.getLogger(ConnectorClient.class);

    private final ConnectorConfig config;
    private final EventLoopGroup group = new NioEventLoopGroup();

    public ConnectorClient(ConnectorConfig config) {
        this.config = config;
    }

    public void start() throws Exception {
        while (true) {
            try {
                connectOnce();
            } catch (Exception e) {
                log.error("Connection failed: {}. Reconnecting in 5s...", e.getMessage());
            }
            TimeUnit.SECONDS.sleep(5);
        }
    }

    private void connectOnce() throws Exception {
        URI uri = URI.create(config.serverUrl());
        String host = uri.getHost();
        int port = uri.getPort() == -1 ? 80 : uri.getPort();

        Bootstrap b = new Bootstrap();
        b.group(group)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) {
                        ChannelPipeline p = ch.pipeline();
                        p.addLast(new HttpClientCodec());
                        p.addLast(new HttpObjectAggregator(65536));

                        DefaultHttpHeaders headers = new DefaultHttpHeaders();
                        headers.add("Authorization", "Bearer " + config.token());

                        p.addLast(new WebSocketClientProtocolHandler(
                                uri, WebSocketVersion.V13, null, false, headers, 1024 * 1024));
                        p.addLast(new ConnectorHandler(config));
                    }
                });

        Channel channel = b.connect(host, port).sync().channel();
        log.info("Connected to {}", config.serverUrl());
        channel.closeFuture().sync();
        log.info("Disconnected. Reconnecting...");
    }
}