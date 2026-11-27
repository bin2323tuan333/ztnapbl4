package org.example;

import org.example.http.HttpServer;

public class Agent {
    public static void main(String[] args) throws Exception {
        // Đọc địa chỉ Server (tham số đầu tiên hoặc biến môi trường SERVER_URL) trước khi khởi tạo tunnel
        AgentConfig.load(args);
        Factory.init();

        HttpServer httpServer = new HttpServer(9999);
        httpServer.start();

        Thread.currentThread().join();
    }
}