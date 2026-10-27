package com.example.tcp;

import com.example.routes.TcpManager;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import lombok.extern.slf4j.Slf4j;

import java.util.function.Consumer;

import static java.nio.charset.StandardCharsets.UTF_8;

@Slf4j
public class TcpClient {
    private final EventLoopGroup group = new NioEventLoopGroup();

    public ChannelFuture connect(String streamId, String host, int port, Channel wsChannel) {
        Bootstrap b = new Bootstrap();
        b.group(group)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) {
                        Consumer<byte[]> sender = encoded -> {
                            if (wsChannel.isActive()) {
                                wsChannel.writeAndFlush(new BinaryWebSocketFrame(Unpooled.wrappedBuffer(encoded)));
                            } else {
                                log.warn("[stream={}] WS channel không active", streamId);
                            }
                        };
                        ch.pipeline().addLast(new TcpToWsHandler(streamId, sender));
                    }
                });

        ChannelFuture future = b.connect(host, port);

        future.addListener((ChannelFutureListener) f -> {
            if (f.isSuccess()) {
                TcpManager.getInstance().add(streamId, f.channel());
                log.info("TCP connected: stream={}, {}:{}", streamId, host, port);
            } else {
                log.error("TCP failed: stream={}, {}:{}, err={}", streamId, host, port, f.cause().getMessage());
            }
        });

        return future;
    }

    public void close(String streamId) {
        TcpManager tcpManager = TcpManager.getInstance();
        Channel channel = tcpManager.getChannel(streamId);
        tcpManager.remove(streamId);
        if (channel != null && channel.isActive()) {
            channel.close();
            log.info("[TcpClient] Đã đóng stream={}", streamId);
        } else {
            log.debug("[TcpClient] Stream {} đã đóng trước đó", streamId);
        }
    }

    public void sendData(String streamId, byte[] payload) {
        if (payload == null || payload.length == 0) {
            return;
        }

        Channel tcpChannel = TcpManager.getInstance().getChannel(streamId);
        if (tcpChannel == null) {
            log.warn("[TcpClient] Stream không tồn tại: {}", streamId);
            return;
        }

        if (!tcpChannel.isActive()) {
            log.warn("[TcpClient] TCP channel không active: stream={}", streamId);
            TcpManager.getInstance().remove(streamId);
            return;
        }


        ByteBuf buf = Unpooled.wrappedBuffer(payload);
        tcpChannel.writeAndFlush(buf).addListener(f -> {
            if (!f.isSuccess()) {
                log.error("[TcpClient] Ghi vào App fail stream={}", streamId, f.cause());
                close(streamId);
            }
        });

        log.debug("[TcpClient] Server → App: stream={}, {} bytes", streamId, payload.length);
    }
}