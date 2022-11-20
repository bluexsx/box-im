package com.bx.imserver;


import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;


@EnableAsync
@EnableScheduling
@ComponentScan(basePackages={"com.bx"})
@SpringBootApplication
public class IMServerApp implements CommandLineRunner {


    public static void main(String[] args) {
        SpringApplication.run(IMServerApp.class,args);
    }


    public void run(String... args) throws Exception {
    }
}
