package com.example.tcp;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import lombok.extern.slf4j.Slf4j;

import static java.nio.charset.StandardCharsets.UTF_8;

@Slf4j
public class TcpClient {

    private final TcpSessionManager sessionManager;

    public TcpClient(TcpSessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    public void connect(int streamId, String service, String host, int port, Channel wsChannel) {
        Bootstrap b = new Bootstrap();
        b.group(wsChannel.eventLoop())
                .channel(NioSocketChannel.class)
                .option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) {
                        ch.pipeline().addLast(
                                new TcpToWsHandler(
                                        streamId,
                                        wsChannel,
                                        id -> sessionManager.remove(id)
                                )
                        );
                    }
                });

        b.connect(host, port).addListener((ChannelFuture f) -> {
            if (f.isSuccess()) {
                Channel tcpChannel = f.channel();
                TcpSession session = new TcpSession(streamId, service, tcpChannel, wsChannel);
                sessionManager.put(streamId, session);

                log.info("TCP connected: stream={}, {}:{}", streamId, host, port);
                sendConnectAck(wsChannel, streamId, (byte) 0x00, null);
            } else {
                log.error("TCP failed: stream={}, {}:{}, err={}",
                        streamId, host, port, f.cause().getMessage());
                sendConnectAck(wsChannel, streamId, (byte) 0x03,
                        f.cause().getMessage());
            }
        });
    }

    public void close(int streamId) {
        TcpSession session = sessionManager.remove(streamId);
        if (session != null && session.getTcpChannel().isActive()) {
            session.getTcpChannel().close();
        }
    }

    private void sendConnectAck(Channel wsChannel, int streamId, byte status, String reason) {
        byte[] reasonBytes = reason == null ? new byte[0] : reason.getBytes(UTF_8);

        ByteBuf buf = wsChannel.alloc().buffer(1 + 4 + 1 + 2 + reasonBytes.length);
        buf.writeByte(0x04);
        buf.writeInt(streamId);
        buf.writeByte(status);
        buf.writeShort(reasonBytes.length);
        buf.writeBytes(reasonBytes);

        wsChannel.writeAndFlush(new BinaryWebSocketFrame(buf));
    }
}