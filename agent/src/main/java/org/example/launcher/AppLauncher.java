package org.example.launcher;

import java.util.Map;

public interface AppLauncher {
    void launch(int localPort, Map<String, String> params) throws Exception;
}
