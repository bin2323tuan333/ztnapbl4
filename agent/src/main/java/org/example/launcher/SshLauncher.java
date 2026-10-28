package org.example.launcher;

public class SshLauncher implements AppLauncher {
    @Override
    public void launch(int port) throws Exception {
//        String cmd = String.format("cmd.exe /c start \"\" cmd.exe /k ssh -v -o StrictHostKeyChecking=no -p %d %s@127.0.0.1", port, user);
//        System.out.println("[SshLauncher] " + cmd);
//        Runtime.getRuntime().exec(cmd);
    }
}