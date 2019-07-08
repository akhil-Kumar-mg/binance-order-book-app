package com.mstakx.orderBookApp.dao;

import com.mstakx.orderBookApp.config.InfluxDbConfig;
import com.mstakx.orderBookApp.model.CryptoPair;
import org.influxdb.InfluxDB;
import org.influxdb.dto.Query;
import org.influxdb.dto.QueryResult;
import org.influxdb.impl.InfluxDBResultMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CryptoPairDao {

    @Autowired
    InfluxDbConfig influxDbConfig;

    public List<CryptoPair> getAllCryptoPairs() {
        InfluxDB connection = influxDbConfig.getConnection();
        QueryResult queryResult = connection.query(new Query("Select * from \"btc-pair\"", "orderBook"));
        InfluxDBResultMapper resultMapper = new InfluxDBResultMapper();
        List<CryptoPair> cryptoPairList = resultMapper.toPOJO(queryResult, CryptoPair.class);
        return cryptoPairList;
    }
}
