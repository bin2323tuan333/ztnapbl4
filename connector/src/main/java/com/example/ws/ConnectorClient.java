package com.example.ws;

import com.example.protocol.frames.CloseFrame;
import com.example.tcp.TcpClient;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketClientProtocolHandler;
import io.netty.handler.codec.http.websocketx.WebSocketVersion;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.net.ssl.SSLException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

@Slf4j
public class ConnectorClient {
    private static ConnectorClient _instance;
    public static ConnectorClient getInstance() {
        if (_instance == null) _instance = new ConnectorClient();
        return _instance;
    }

    private ConnectorClient() {}

    private static final int CONNECT_TIMEOUT_MS = 10_000;
    private static final int TCP_CHECK_TIMEOUT_MS = 3_000;
    private static final int MAX_FRAME_SIZE = 1024 * 1024;
    private static final int RECONNECT_DELAY_SEC = 1;

    private final ConnectorConfig config = ConnectorConfig.getInstance();
    private final EventLoopGroup group = new NioEventLoopGroup();
    private final AtomicBoolean stopped = new AtomicBoolean(false);
    private volatile Channel currentChannel;

    public void start() {
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



    private void connectOnce() throws Exception {
        URI uri = URI.create(config.getSERVER_URL());
        String host = uri.getHost();
        int port = resolvePort(uri);

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
                        p.addLast(new HttpObjectAggregator(MAX_FRAME_SIZE));

                        DefaultHttpHeaders headers = new DefaultHttpHeaders();
                        headers.add(
                                "Authorization",
                                "Bearer " + config.getTOKEN()
                        );

                        p.addLast(
                                new WebSocketClientProtocolHandler(
                                        uri,
                                        WebSocketVersion.V13,
                                        null,
                                        false,
                                        headers,
                                        MAX_FRAME_SIZE
                                )
                        );

                        p.addLast(new ConnectorHandler(
                                ConnectorConfig.getInstance(),
                                TcpClient.getInstance()
                        ));
                    }
                });



        Channel channel = b.connect(host, port).sync().channel();
        currentChannel = channel;

        log.info("Connected to {}", config.getSERVER_URL());
        channel.closeFuture().sync();

        currentChannel = null;
        log.info("Disconnected from {}", config.getSERVER_URL());
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

    public void sendClose(String streamId, byte reason) {
        CloseFrame closeFrame = new CloseFrame(streamId, reason);
        currentChannel.writeAndFlush(new BinaryWebSocketFrame(
                Unpooled.wrappedBuffer(closeFrame.encode())
        ));
    }
}