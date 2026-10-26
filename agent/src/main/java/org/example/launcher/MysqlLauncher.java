package org.example.launcher;

import java.util.Map;

public class MysqlLauncher implements AppLauncher {
    @Override
    public void launch(int port, Map<String, String> params) throws Exception {
        String cmd = String.format(
                "cmd.exe /c start \"\" cmd.exe /k mysql -h 127.0.0.1 -P %d -p",
                port
        );
        System.out.println("[MysqlLauncher] " + cmd);
        Runtime.getRuntime().exec(cmd);
    }
}