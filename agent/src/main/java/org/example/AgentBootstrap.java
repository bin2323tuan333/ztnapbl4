package org.example;

import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import org.example.listener.LocalServer;
import org.example.stream.StreamRegistry;
import org.example.tunnel.TunnelClient;

public class AgentBootstrap {
    private final AgentConfig config;

    private EventLoopGroup tunnelGroup;

    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;

    private StreamRegistry registry;

    private TunnelClient tunnelClient;

    public AgentBootstrap(AgentConfig config) {
        this.config = config;
    }

    public synchronized void start() throws Exception {
        this.tunnelGroup = new NioEventLoopGroup(1);
        this.bossGroup   = new NioEventLoopGroup(1);
        this.workerGroup = new NioEventLoopGroup(2);

        this.registry = new StreamRegistry();

        try {
            this.tunnelClient = new TunnelClient(config, tunnelGroup, registry);
            this.tunnelClient.connect().sync();
            System.out.println("[AgentBootstrap] Đã kết nối tới Gateway thành công!");
        } catch (Exception e) {
            stop();
            System.err.println("[AgentBootstrap] CẢNH BÁO: Không kết nối được Gateway (" + e.getMessage() + ")");
            System.err.println("[AgentBootstrap] Chuyển sang chế độ OFFLINE TEST (Chỉ mở Terminal, không truyền dữ liệu).");
            throw e;
        }
    }

    public synchronized void stop() {
        if (tunnelClient != null) tunnelClient.close();
        if (tunnelGroup != null) tunnelGroup.shutdownGracefully();
        if (bossGroup != null)   bossGroup.shutdownGracefully();
        if (workerGroup != null) workerGroup.shutdownGracefully();

    }


    public EventLoopGroup getBossGroup() { return bossGroup; }
    public EventLoopGroup getWorkerGroup() { return workerGroup; }
    public StreamRegistry getRegistry() { return registry; }
    public TunnelClient getTunnelClient() { return tunnelClient; }
}
