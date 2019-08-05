package com.mstakx.orderBookApp.service;

import com.mstakx.orderBookApp.config.InfluxDbConfig;
import com.mstakx.orderBookApp.dao.CryptoPairDao;
import com.mstakx.orderBookApp.dto.CryptoPairDTO;
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
    CryptoPairDao cryptoPairDao;

    public void loadBtcPair(List<String> btcPairList) {
        InfluxDB connection = InfluxDbConfig.getConnection();
        BatchPoints batchPoints = BatchPoints
                .database("trading_DB")
                .retentionPolicy("defaultPolicy")
                .build();
        int counter = 0;
        for (String btcPair : btcPairList) {
            Point point = Point.measurement("crypto_pair")
                    .time(System.currentTimeMillis() + counter++, TimeUnit.MILLISECONDS)
                    .addField("symbol", btcPair)
                    .build();
            batchPoints.point(point);
        }
        connection.write(batchPoints);
    }

    public List<CryptoPairDTO> getAllCryptoPairs() {
        return cryptoPairDao.getAllCryptoPairs();
    }

    public String getLastAddedBtcPair() {
        return cryptoPairDao.getLastAddedBtcPair();
    }

}
