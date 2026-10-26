package org.example;

import org.example.launcher.*;
import org.example.listener.LocalServer;

import java.util.HashMap;
import java.util.Map;

public class AgentController {
    private final AgentBootstrap bootstrap;
    private final Map<String, AppLauncher> launchers = new HashMap<>();

    public AgentController(AgentBootstrap bootstrap) {
        this.bootstrap = bootstrap;

        launchers.put("web",   new WebLauncher());
        launchers.put("mysql", new MysqlLauncher());
        launchers.put("ssh",   new SshLauncher());
    }

    public int openApp(String resourceId, String appType, Map<String, String> params) throws Exception {
        String jwt = params.getOrDefault("jwt", "");
        LocalServer server = new LocalServer(
                resourceId,
                jwt,
                bootstrap.getBossGroup(),
                bootstrap.getWorkerGroup(),
                bootstrap.getTunnelClient(),
                bootstrap.getRegistry()
        );
        int port = server.start();
        params.put("port", String.valueOf(port));

        AppLauncher launcher = launchers.get(appType.toLowerCase());
        if (launcher != null) {
            System.out.println("[AgentController] " + launcher.getClass().getSimpleName() + " cho app " + resourceId + " | port=" + port);
            launcher.launch(port, params);
        } else {
            System.err.println("[AgentController] Không tìm thấy Launcher cho appType = " + appType);
        }

        return port;
    }
}