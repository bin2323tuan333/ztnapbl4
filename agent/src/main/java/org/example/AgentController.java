package org.example;

import org.example.launcher.*;
import org.example.listener.LocalServer;
import org.example.protocol.frames.OpenResFrame;

import java.util.HashMap;
import java.util.Map;

public class AgentController {
    private final AgentBootstrap bootstrap;
    private final Map<String, AppLauncher> launchers = new HashMap<>();

    public AgentController(AgentBootstrap bootstrap) {
        this.bootstrap = bootstrap;
        launchers.put("web", new WebLauncher());
        launchers.put("mysql", new MysqlLauncher());
        launchers.put("ssh", new SshLauncher());
    }

    public synchronized int openApp(String resourceId, String appType, Map<String, String> params) throws Exception {
        String jwt = params.getOrDefault("jwt", "");

        System.out.println("\n[AgentController] Đang xin phép Gateway mở app: " + resourceId);
        bootstrap.getTunnelClient().sendOpenReq(resourceId, jwt);

        OpenResFrame res = bootstrap.getRegistry().waitForOpenResponse();

        if (res == null) {
            throw new Exception("Gateway không phản hồi (Timeout)");
        }
        if (!res.isOk()) {
            throw new Exception("Từ chối: " + res.getReason());
        }

        String streamId = res.getStreamId();
        System.out.println("[AgentController] ✅ Được cấp streamId=" + streamId + ". Bắt đầu mở LocalServer.");

        LocalServer server = new LocalServer(
                resourceId,
                streamId,
                bootstrap.getBossGroup(),
                bootstrap.getWorkerGroup(),
                bootstrap.getTunnelClient(),
                bootstrap.getRegistry()
        );
        int port = server.start();
        params.put("port", String.valueOf(port));

        AppLauncher launcher = launchers.get(appType.toLowerCase());
        if (launcher != null) {
            launcher.launch(port, params);
        }

        return port;
    }
}