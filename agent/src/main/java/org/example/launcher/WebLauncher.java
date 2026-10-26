package org.example.launcher;

import java.util.Map;

public class WebLauncher implements AppLauncher {
    @Override
    public void launch(int port, Map<String, String> params) throws Exception {
        String url = String.format("http://127.0.0.1:%d/", port);
        String cmd = String.format("cmd.exe /c start chrome \"%s\"", url);
        System.out.println("[WebLauncher] " + cmd);
        Runtime.getRuntime().exec(cmd);
    }
}