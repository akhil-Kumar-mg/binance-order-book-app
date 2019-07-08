package com.mstakx.orderBookApp;

import com.mstakx.orderBookApp.config.InfluxDbConfig;
import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.Pong;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class BinanceAppApplication {

    public static void main(String[] args) {
        SpringApplication.run(BinanceAppApplication.class, args);
    }
}
