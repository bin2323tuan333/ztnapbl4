package com.example.ws;

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
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

@Slf4j
public class ConnectorClient {

    private static final int CONNECT_TIMEOUT_MS = 10_000;
    private static final int TCP_CHECK_TIMEOUT_MS = 3_000;
    private static final int MAX_FRAME_SIZE = 1024 * 1024;
    private static final int RECONNECT_DELAY_SEC = 1;

    private final ConnectorConfig config;
    private final Supplier<ConnectorHandler> handlerFactory;   // ★ Factory
    private final EventLoopGroup group = new NioEventLoopGroup();
    private final AtomicBoolean stopped = new AtomicBoolean(false);
    private volatile Channel currentChannel;

    public ConnectorClient(ConnectorConfig config,
                           Supplier<ConnectorHandler> handlerFactory) {
        this.config = config;
        this.handlerFactory = handlerFactory;
    }

    public void start() {
        // ① Check TCP tới từng service TRƯỚC
        Map<String, String> availableServices = checkServices();

        if (availableServices.isEmpty()) {
            log.error("No available services! Check your apps are running.");
        } else {
            log.info("Available services: {}", availableServices.keySet());

            // ② Update config với services OK
            config.getServices().clear();
            config.getServices().putAll(availableServices);
        }

        while (!stopped.get()) {
            try {
                connectOnce();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                log.error("Connection failed: {}", e.getMessage());
            }

            if (stopped.get()) break;

            try {
                TimeUnit.SECONDS.sleep(RECONNECT_DELAY_SEC);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }

        log.info("Connector stopped");
    }
    private Map<String, String> checkServices() {
        log.info("Checking services...");
        Map<String, String> ok = new HashMap<>();

        for (Map.Entry<String, String> entry : config.getServices().entrySet()) {
            String name = entry.getKey();
            String target = entry.getValue();

            String host = target.split(":")[0];
            int port = Integer.parseInt(target.split(":")[1]);

            boolean reachable = checkTcp(name, host, port);

            if (reachable) {
                ok.put(name, target);
                log.info("  ✅ {} → {}:{} — OK", name, host, port);
            } else {
                log.warn("  ❌ {} → {}:{} — FAIL", name, host, port);
            }
        }

        return ok;
    }

    private boolean checkTcp(String name, String host, int port) {
        try (java.net.Socket socket = new java.net.Socket()) {
            socket.connect(
                    new java.net.InetSocketAddress(host, port),
                    TCP_CHECK_TIMEOUT_MS
            );
            return true;
        } catch (Exception e) {
            log.warn("  ❌ {} → {}:{} — {}", name, host, port, e.getMessage());
            return false;
        }
    }

    private void connectOnce() throws Exception {
        URI uri = URI.create(config.getServerUrl());
        String host = uri.getHost();
        int port = resolvePort(uri);

        log.info("Connecting to {}:{}...", host, port);

        Bootstrap b = new Bootstrap();
        b.group(group)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, CONNECT_TIMEOUT_MS)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) {
                        ChannelPipeline p = ch.pipeline();

                        p.addLast(new IdleStateHandler(60, 30, 0, TimeUnit.SECONDS));
                        p.addLast(new HttpClientCodec());
                        p.addLast(new HttpObjectAggregator(65536));

                        DefaultHttpHeaders headers = new DefaultHttpHeaders();
                        headers.add("Authorization", "Bearer " + config.getToken());

                        p.addLast(new WebSocketClientProtocolHandler(
                                uri, WebSocketVersion.V13, null, false,
                                headers, MAX_FRAME_SIZE));

                        p.addLast(handlerFactory.get());
                    }
                });

        Channel channel = b.connect(host, port).sync().channel();
        currentChannel = channel;

        log.info("Connected to {}", config.getServerUrl());
        channel.closeFuture().sync();

        currentChannel = null;
        log.info("Disconnected from {}", config.getServerUrl());
    }

    public void stop() {
        if (!stopped.compareAndSet(false, true)) return;

        log.info("Stopping connector...");

        Channel ch = currentChannel;
        if (ch != null && ch.isActive()) {
            ch.close();
        }

        group.shutdownGracefully();
    }

    private int resolvePort(URI uri) {
        if (uri.getPort() != -1) return uri.getPort();
        return switch (uri.getScheme()) {
            case "wss", "https" -> 443;
            case "ws", "http" -> 80;
            default -> 80;
        };
    }
}