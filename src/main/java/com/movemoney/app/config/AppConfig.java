package com.movemoney.app.config;

public class AppConfig {
    private int serverThreads;
    private int workerThreads;
    private int port;

    public AppConfig(int serverThreads, int workerThreads, int port) {
        this.serverThreads = serverThreads;
        this.workerThreads = workerThreads;
        this.port = port;
    }

    public int getServerThreads() {
        return serverThreads;
    }

    public int getWorkerThreads() {
        return workerThreads;
    }

    public int getPort() {
        return port;
    }
}