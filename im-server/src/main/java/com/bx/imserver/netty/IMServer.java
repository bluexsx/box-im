package com.bx.imserver.netty;

public interface IMServer {

    boolean isReady();

    void start();

    void stop();
}
