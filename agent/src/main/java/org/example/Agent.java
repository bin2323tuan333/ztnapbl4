package org.example;

import org.example.http.HttpServer;

public class Agent {
    public static void main(String[] args) throws Exception {
        Factory.init();

        HttpServer httpServer = new HttpServer(9999);
        httpServer.start();

        Thread.currentThread().join();
    }
}