package org.example.launcher;

import java.util.Map;

public class SshLauncher implements AppLauncher {
    @Override
    public void launch(int port, Map<String, String> params) throws Exception {
        String user = params.getOrDefault("user", "root"); // Lấy user từ params, mặc định là root
        String cmd = String.format("cmd.exe /c start \"\" cmd.exe /k ssh -v -o StrictHostKeyChecking=no -p %d %s@127.0.0.1", port, user);
        System.out.println("[SshLauncher] " + cmd);
        Runtime.getRuntime().exec(cmd);
    }
}