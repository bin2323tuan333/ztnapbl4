
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
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class TunnelClient {

    private final AgentConfig agentConfig = Factory.agentConfig;
    private final EventLoopGroup tunnelGroup = Factory.tunnelGroup;

    private final Queue<byte[]> pendingFrames = new ConcurrentLinkedQueue<>();

    private volatile Channel currentChannel;
    private volatile Channel connectingChannel;

    private boolean connecting = false;
    private boolean stopped = false;

    public void sendHttpAccess(String reqId, String resourceId, String jwt) {
        sendAndConnect(
                new HttpAccessFrame(reqId, resourceId, jwt).encode()
        );
    }

    public void sendResourceAccess(String reqId, String resourceId, String ticket) {
        sendAndConnect(
                new ResourceAccessFrame(reqId, resourceId, ticket).encode()
        );
    }

    public boolean sendStreamData(String streamId, byte[] data) {
        return sendIfReady(
                new DataFrame(streamId, data).encode()
        );
    }

    public boolean sendStreamClose(String streamId) {
        return sendIfReady(
                new CloseFrame(streamId).encode()
        );
    }

    private void sendAndConnect(byte[] data) {
        synchronized (this) {
            if (stopped) {
                System.err.println("[TunnelClient] Client đã shutdown.");
                return;
            }

            if (isReady()) {
                writeFrame(currentChannel, data);
                return;
            }

            pendingFrames.offer(data);

            if (!connecting) {
                connect();
            }
        }
    }

    private boolean sendIfReady(byte[] data) {
        synchronized (this) {
            if (stopped || !isReady()) {
                System.err.println("[TunnelClient] Tunnel chưa sẵn sàng, không thể gửi DATA.");
                return false;
            }

            writeFrame(currentChannel, data);
            return true;
        }
    }

    private void connect() {
        if (stopped || connecting || isReady()) {
            return;
        }

        connecting = true;

        try {
            URI uri = URI.create(agentConfig.getGatewayUrl());

            String host = uri.getHost();

            int port = uri.getPort() != -1
                    ? uri.getPort()
                    : ("wss".equalsIgnoreCase(uri.getScheme()) ? 443 : 80);

            System.out.println("[TunnelClient] Đang kết nối tới " + host + ":" + port + "...");
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(tunnelGroup)
                    .channel(NioSocketChannel.class)
                    .handler(new TunnelChannelInitializer());
            ChannelFuture future = bootstrap.connect(host, port);
            future.addListener(result -> {
                synchronized (TunnelClient.this) {
                    if (result.isSuccess()) {
                        connectingChannel = future.channel();
                        System.out.println("[TunnelClient] TCP connected. " + "Đang chờ WebSocket handshake...");
                    } else {
                        connecting = false;
                        System.err.println("[TunnelClient] Kết nối thất bại: " + result.cause());
                    }
                }
            });

        } catch (Exception e) {
            connecting = false;
            System.err.println("[TunnelClient] Lỗi kết nối: " + e.getMessage());
        }
    }

    public void onHandshakeComplete(Channel channel) {
        synchronized (this) {
            if (stopped || connectingChannel != channel) {
                channel.close();
                return;
            }

            currentChannel = channel;
            connectingChannel = null;
            connecting = false;

            Factory.tunnelCurrent = channel;

            System.out.println("[TunnelClient] WebSocket handshake thành công!");

            byte[] data;

            while ((data = pendingFrames.poll()) != null) {
                writeFrame(channel, data);
            }
        }
    }

    public void onDisconnected(Channel channel) {
        synchronized (this) {
            if (currentChannel == channel) {
                currentChannel = null;

                if (Factory.tunnelCurrent == channel) {
                    Factory.tunnelCurrent = null;
                }
            }

            if (connectingChannel == channel) {
                connectingChannel = null;
                connecting = false;
            }
        }

        System.out.println("[TunnelClient] Đã mất kết nối với Gateway.");
    }

    private void writeFrame(Channel channel, byte[] data) {
        if (channel == null || !channel.isActive()) {
            return;
        }

        channel.writeAndFlush(new BinaryWebSocketFrame(
                        Unpooled.wrappedBuffer(data)
        )).addListener(future -> {
            if (!future.isSuccess()) {
                System.err.println("[TunnelClient] Gửi frame thất bại: " + future.cause());
            }
        });
    }

    public boolean isReady() {
        return currentChannel != null && currentChannel.isActive();
    }

    public void close() {
        synchronized (this) {
            stopped = true;

            pendingFrames.clear();

            if (currentChannel != null) {
                currentChannel.close();
                currentChannel = null;
            }

            if (connectingChannel != null) {
                connectingChannel.close();
                connectingChannel = null;
            }

            connecting = false;

            Factory.tunnelCurrent = null;
        }

        System.out.println("[TunnelClient] Đã đóng kết nối.");
    }
}
