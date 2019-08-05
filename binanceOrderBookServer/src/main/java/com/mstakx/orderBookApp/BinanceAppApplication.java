package com.mstakx.orderBookApp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class BinanceAppApplication {

    public static void main(String[] args) {
        SpringApplication.run(BinanceAppApplication.class, args);
    }
}
