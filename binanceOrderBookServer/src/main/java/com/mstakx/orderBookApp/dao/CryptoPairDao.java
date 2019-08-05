package com.mstakx.orderBookApp.dao;

import com.mstakx.orderBookApp.config.InfluxDbConfig;
import com.mstakx.orderBookApp.measurements.CryptoPair;
import com.mstakx.orderBookApp.dto.CryptoPairDTO;
import org.influxdb.InfluxDB;
import org.influxdb.dto.Query;
import org.influxdb.dto.QueryResult;
import org.influxdb.impl.InfluxDBResultMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class CryptoPairDao {

    @Autowired
    InfluxDbConfig influxDbConfig;

    public List<CryptoPairDTO> getAllCryptoPairs() {
        InfluxDB connection = influxDbConfig.createConnection();
        QueryResult queryResult = connection.query(new Query("Select * from \"crypto_pair\"", "trading_DB"));
        InfluxDBResultMapper resultMapper = new InfluxDBResultMapper();
        List<CryptoPair> cryptoPairList = resultMapper.toPOJO(queryResult, CryptoPair.class);
        List<CryptoPairDTO> cryptoPairResponse = new ArrayList<>();
        cryptoPairList.forEach(cryptoPair -> {
            CryptoPairDTO cryptoPairDTO = new CryptoPairDTO();
            cryptoPairDTO.setSymbol(cryptoPair.getSymbol());
            cryptoPairResponse.add(cryptoPairDTO);
        });
        return cryptoPairResponse;
    }

    public String getLastAddedBtcPair() {
        InfluxDB connection = influxDbConfig.getConnection();
        QueryResult queryResult = connection.query(new Query("SELECT * FROM crypto_pair GROUP BY * ORDER BY DESC LIMIT 1", "trading_DB"));
        InfluxDBResultMapper resultMapper = new InfluxDBResultMapper();
        List<CryptoPair> cryptoPair = resultMapper.toPOJO(queryResult, CryptoPair.class);
        if (cryptoPair.size() > 0) {
            return cryptoPair.get(0).getSymbol();
        }
        return null;
    }
}
