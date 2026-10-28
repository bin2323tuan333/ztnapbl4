package org.example.tunnel;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import org.example.AgentConfig;
import org.example.Factory;
import org.example.protocol.frames.CloseFrame;
import org.example.protocol.frames.DataFrame;
import org.example.protocol.frames.HttpAccessFrame;
import org.example.protocol.frames.ResourceAccessFrame;

import java.net.URI;

public class TunnelClient {
    private final AgentConfig agentConfig = Factory.agentConfig;
    private final EventLoopGroup tunnelGroup = Factory.tunnelGroup;
    private volatile Channel channel;

    public TunnelClient() {
        try {
            this.channel = connect().sync().channel();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public ChannelFuture connect() {
        Bootstrap b = new Bootstrap();
        b.group(tunnelGroup)
                .channel(NioSocketChannel.class)
                .handler(new TunnelChannelInitializer());

        URI uri = URI.create(agentConfig.getGatewayUrl());
        String host = uri.getHost();
        int port = uri.getPort() != -1 ? uri.getPort() : ("wss".equals(uri.getScheme()) ? 443 : 80);

        System.out.print("[TunnelClient] Đang kết nối tới " + host + ":" + port + "...");

        ChannelFuture future = b.connect(host, port);
        future.addListener(f -> {
            if (f.isSuccess()) {
                this.channel = future.channel();
            } else {
                System.err.println("[TunnelClient] Kết nối thất bại: " + f.cause());
            }
        });

        return future;
    }


    public void sendHttpAccess(String reqId, String resourceId, String jwt) {
        send(new HttpAccessFrame(reqId, resourceId, jwt).encode());
    }

    public void sendResourceAccess(String reqId, String resourceId) {
        send(new ResourceAccessFrame(reqId, resourceId).encode());
    }

    public void sendStreamData(String streamId, byte[] data) {
        send(new DataFrame(streamId, data).encode());
    }

    public void sendStreamClose(String streamId) {
        send(new CloseFrame(streamId).encode());
    }

    public void send(byte[] data) {
        if (channel != null && channel.isActive()) {
            System.out.println("[TunnelClient] Đang gửi " + data.length + " bytes lên Gateway...");
            channel.writeAndFlush(new BinaryWebSocketFrame(Unpooled.wrappedBuffer(data)));
        } else {
            System.err.println("[TunnelClient] Mất kết nối, không thể gửi data!");
        }
    }

    public boolean isReady() {
        return channel != null && channel.isActive();
    }

    public void close() {
        if (channel != null) {
            channel.close();
            System.out.println("[TunnelClient] Đã đóng kết nối tới Gateway.");
        }
    }
}