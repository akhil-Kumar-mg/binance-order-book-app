package com.mstakx.orderBookApp.service;

import com.mstakx.orderBookApp.config.InfluxDbConfig;
import org.influxdb.InfluxDB;
import org.influxdb.dto.BatchPoints;
import org.influxdb.dto.Point;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class OrderBookService {

    @Autowired
    InfluxDbConfig influxDbConfig;

    public void loadBtcPair(List<String> btcPairList) {
        InfluxDB connection = influxDbConfig.getInfluxDBInstance();
        BatchPoints batchPoints = BatchPoints
                .database("orderBook")
                .retentionPolicy("defaultPolicy")
                .build();
        int counter = 0;
        for (String btcPair : btcPairList) {
            Point point = Point.measurement("btc-pair")
                    .time(System.currentTimeMillis() + counter++, TimeUnit.MILLISECONDS)
                    .addField("symbol", btcPair)
                    .build();
            batchPoints.point(point);
        }
        connection.write(batchPoints);
    }
}
