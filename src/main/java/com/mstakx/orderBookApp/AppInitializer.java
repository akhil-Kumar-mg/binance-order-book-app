package com.mstakx.orderBookApp;

import com.mstakx.orderBookApp.config.InfluxDbConfig;
import com.mstakx.orderBookApp.exception.BtcPairFileNotFound;
import com.mstakx.orderBookApp.service.OrderBookService;
import org.influxdb.InfluxDB;
import org.influxdb.dto.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

@Component
public class AppInitializer {

    @Autowired
    InfluxDbConfig influxDbConfig;

    @Autowired
    ResourceLoader resourceLoader;

    @Autowired
    OrderBookService orderBookService;

    @Value("${database.persistance.enable}")
    private Boolean dbPersistanceEnable;

    @EventListener
    public void onApplicationStartup(ContextRefreshedEvent event) {
        if (dbPersistanceEnable) {
            InfluxDB influxDB = influxDbConfig.getConnection();
            createAndInitDatabase(influxDB);
        }
    }

    private void createAndInitDatabase(InfluxDB influxDB) {
        if(influxDB.databaseExists("orderBook")) {
            influxDB.query(new Query("DROP DATABASE orderBook", "orderBook"));
        }
        influxDB.query(new Query("CREATE DATABASE orderBook", "orderBook"));

        influxDB.createRetentionPolicy(
                "defaultPolicy", "orderBook", "3d", 1, true);
        loadBtcPairs();
    }

    private void loadBtcPairs() {
        try {
            InputStream resource = resourceLoader.getResource("classpath:data/btc-pairs.dat").getInputStream();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(resource));
            String line = bufferedReader.readLine();
            List<String> btcPairList = new ArrayList<>();
            while (line != null) {
                btcPairList.add(line);
                line = bufferedReader.readLine();
            }
            orderBookService.loadBtcPair(btcPairList);
            bufferedReader.close();
        } catch (IOException e) {
            throw new BtcPairFileNotFound("Btc Pair File not found", e);
        }
    }
}
