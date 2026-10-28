package org.example.tunnel;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.websocketx.WebSocketClientProtocolHandler;
import io.netty.handler.codec.http.websocketx.WebSocketFrameAggregator;
import io.netty.handler.codec.http.websocketx.WebSocketVersion;
import org.example.AgentConfig;
import org.example.Factory;
import org.example.cache.StreamRegistry;

import java.net.URI;

public class TunnelChannelInitializer extends ChannelInitializer<SocketChannel> {
    private final AgentConfig agentConfig = Factory.agentConfig;
    private final StreamRegistry streamRegistry = Factory.streamRegistry;

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        URI uri = URI.create(agentConfig.getGatewayUrl());

        ch.pipeline()
                .addLast(new HttpClientCodec())
                .addLast(new HttpObjectAggregator(65536))
                .addLast(new WebSocketClientProtocolHandler(
                        uri, WebSocketVersion.V13,
                        null,
                        true,
                        new DefaultHttpHeaders(),
                        65536))
                .addLast(new WebSocketFrameAggregator(65536))
                .addLast(new TunnelHandler());
    }
}