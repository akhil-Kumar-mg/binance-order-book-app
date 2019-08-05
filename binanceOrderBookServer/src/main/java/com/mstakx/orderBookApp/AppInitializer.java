package com.mstakx.orderBookApp;

import com.mstakx.orderBookApp.config.InfluxDbConfig;
import com.mstakx.orderBookApp.dto.CryptoPairDTO;
import com.mstakx.orderBookApp.exception.BtcPairFileNotFound;
import com.mstakx.orderBookApp.service.MarketDepthService;
import com.mstakx.orderBookApp.service.OrderBookService;
import org.influxdb.InfluxDB;
import org.influxdb.dto.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
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

    @Autowired
    MarketDepthService marketDepthService;

    @Value("${database.persistance.enable}")
    private Boolean dbPersistanceEnable;

    @Autowired
    ApplicationEventPublisher applicationEventPublisher;

    @EventListener
    public void onApplicationStartup(ContextRefreshedEvent event) {
        if (dbPersistanceEnable) {
            createAndInitDatabase();
            initiateOnDepthEventListenerForAllSymbols();
        }
    }

    private void createAndInitDatabase() {
        InfluxDB influxDB = influxDbConfig.createConnection();
        if (!influxDB.databaseExists("trading_DB")) {
            influxDB.query(new Query("CREATE DATABASE trading_DB", "trade_DB"));
            influxDB.createRetentionPolicy(
                    "defaultPolicy", "trading_DB", "3d", 1, true);
        }
        loadBtcPairs();
    }

    private void loadBtcPairs() {
        try {
            /*
                Assuming we will not remove the existing pairs
             */
            String lastBtcPairValue = orderBookService.getLastAddedBtcPair();
            Boolean loadPairs = false;
            InputStream resource = resourceLoader.getResource("classpath:data/btc-pairs.dat").getInputStream();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(resource));
            String line = bufferedReader.readLine();
            List<String> btcPairList = new ArrayList<>();
            while (line != null) {
                if (loadPairs) {
                    btcPairList.add(line);
                }
                if (line.equalsIgnoreCase(lastBtcPairValue) || lastBtcPairValue == null) {
                    loadPairs = true;
                }
                line = bufferedReader.readLine();
            }
            orderBookService.loadBtcPair(btcPairList);
            bufferedReader.close();
        } catch (IOException e) {
            throw new BtcPairFileNotFound("Btc Pair File not found", e);
        }
    }

    private void initiateOnDepthEventListenerForAllSymbols() {
        List<CryptoPairDTO> cryptoPairDTOList = orderBookService.getAllCryptoPairs();
        cryptoPairDTOList.forEach(cryptoPair -> {
            marketDepthService.startDepthEventListenerForPair(cryptoPair.getSymbol());
        });
    }
}
