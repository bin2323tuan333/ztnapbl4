package org.example;

import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import org.example.cache.ClientOpenRegistry;
import org.example.cache.PendingHttpRequest;
import org.example.http.HttpServer;
import org.example.cache.StreamRegistry;
import org.example.tunnel.TunnelClient;

public class Factory {
    public static AgentConfig agentConfig;
    public static StreamRegistry streamRegistry;
    public static PendingHttpRequest pendingHttpRequest;
    public static TunnelClient tunnelClient;
    public static HttpServer httpServer;
    public static ClientOpenRegistry clientOpenRegistry;

    public static EventLoopGroup bossGroup;
    public static EventLoopGroup workerGroup;
    public static EventLoopGroup tunnelGroup;

    public static void init() throws Exception {
        System.out.println("[Factory] Bắt đầu khởi tạo hệ thống...");

        agentConfig = AgentConfig.getInstance();

        bossGroup = new NioEventLoopGroup(1);
        workerGroup = new NioEventLoopGroup(2);
        tunnelGroup = new NioEventLoopGroup(1);

        streamRegistry = new StreamRegistry();
        pendingHttpRequest = PendingHttpRequest.getInstance();
        clientOpenRegistry = new ClientOpenRegistry();

        tunnelClient = new TunnelClient();
        httpServer = new HttpServer(9999);

        System.out.println("[Factory] Khởi tạo thành công!");
    }

    public static void shutdown() {
        if (tunnelClient != null) tunnelClient.close();
        if (tunnelGroup != null) tunnelGroup.shutdownGracefully();
        if (bossGroup != null) bossGroup.shutdownGracefully();
        if (workerGroup != null) workerGroup.shutdownGracefully();
    }
}