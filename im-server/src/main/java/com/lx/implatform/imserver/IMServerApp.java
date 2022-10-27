package com.lx.implatform.imserver;


import com.lx.implatform.imserver.websocket.WebsocketServer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;


@EnableAsync
@EnableScheduling
@ComponentScan(basePackages={"com.lx"})
@SpringBootApplication
public class IMServerApp implements CommandLineRunner {

    @Value("${websocket.port}")
    private int port;


    public static void main(String[] args) {
            SpringApplication.run(IMServerApp.class);
    }


    public void run(String... args) throws Exception {
        new WebsocketServer().start(port);
    }
}
