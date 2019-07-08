package com.mstakx.orderBookApp.service;

import com.binance.api.client.BinanceApiWebSocketClient;
import com.binance.api.client.domain.market.OrderBook;
import com.binance.api.client.domain.market.OrderBookEntry;
import com.mstakx.orderBookApp.config.InfluxDbConfig;
import com.mstakx.orderBookApp.dao.MarkerDepthDao;
import com.mstakx.orderBookApp.model.MarketDepthResponse;
import com.mstakx.orderBookApp.util.BinanceApiUtil;
import com.mstakx.orderBookApp.util.MarketDepthCacheUtil;
import org.influxdb.InfluxDB;
import org.influxdb.dto.BatchPoints;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.NavigableMap;

@Service
public class MarketDepthService {

    @Autowired
    InfluxDbConfig influxDbConfig;

    @Autowired
    SimpMessagingTemplate simpMessagingTemplate;

    @Autowired
    MarkerDepthDao markerDepthDao;

    @Value("${orderbook.cache.size}")
    private Integer cacheLimit;

    /*
        it will fetch the current market depth of the given pair and trigger depth listener for the same
        if the database persistence is disabled it will not store the changes to database
     */
    public MarketDepthResponse getMarketDepthForAPair(String symbol) {
        NavigableMap<BigDecimal, BigDecimal> asks = MarketDepthCacheUtil.getMarketDepthCacheForAsksBySymbol(symbol);
        NavigableMap<BigDecimal, BigDecimal> bids = MarketDepthCacheUtil.getMarketDepthCacheForBidsBySymbol(symbol);

        InfluxDB connection = influxDbConfig.getInfluxDBInstance();
        /*
            If the given pair is not called before, the initial cache will be empty,
            in such case we call the rest api to get the initial cache and store it in the map
            Also, trigger the web socket listener for the same
         */
        if (asks.size() == 0 && bids.size() == 0) {
            OrderBook orderBook = BinanceApiUtil.getOrderBookFromBinanceRestClient(symbol, 20);
            if (connection != null) {
                markerDepthDao.saveOrderBookToCacheAndDB(orderBook, symbol, asks, bids);
            } else {
                markerDepthDao.saveOrderBookToCache(orderBook, symbol, asks, bids);
            }
            MarketDepthCacheUtil.setCurrentUpdatedId(symbol, orderBook.getLastUpdateId());
        }
        startDepthEventListenerForPair(symbol);
        return getMarkerResponse(symbol);
    }

    private MarketDepthResponse getMarkerResponse(String symbol) {
        MarketDepthResponse marketDepthResponse = new MarketDepthResponse();
        marketDepthResponse.setMarketDepthForAsks(MarketDepthCacheUtil.getMarketDepthCacheForAsksBySymbol(symbol));
        marketDepthResponse.setMarketDepthForBids(MarketDepthCacheUtil.getMarketDepthCacheForBidsBySymbol(symbol));
        marketDepthResponse.setSymbol(symbol);
        return marketDepthResponse;
    }

    private void startDepthEventListenerForPair(String symbol) {

        BinanceApiWebSocketClient client = BinanceApiUtil.getBinanceWebSocketClient();
        client.onDepthEvent(symbol.toLowerCase(), res -> {
            Long currentUpdatedId = res.getUpdateId();
            Long lastUpdatedId = MarketDepthCacheUtil.getLastUpdatedId(symbol);
            if (currentUpdatedId > lastUpdatedId) {
                BatchPoints batchPoints = markerDepthDao.initiateBatchPoint();
                NavigableMap<BigDecimal, BigDecimal> asks = MarketDepthCacheUtil.getMarketDepthCacheForAsksBySymbol(symbol);
                NavigableMap<BigDecimal, BigDecimal> bids = MarketDepthCacheUtil.getMarketDepthCacheForBidsBySymbol(symbol);
                for (OrderBookEntry orderBookDelta : res.getAsks()) {
                    BigDecimal price = new BigDecimal(orderBookDelta.getPrice());
                    BigDecimal qty = new BigDecimal(orderBookDelta.getQty());
                    if (qty.compareTo(BigDecimal.ZERO) == 0) {
                        asks.remove(price);
                    } else {
                        asks.put(price, qty);
                    }
                    markerDepthDao.setPoints(symbol, batchPoints, orderBookDelta,"ASKS");
                }

                for (OrderBookEntry orderBookDelta : res.getBids()) {
                    BigDecimal price = new BigDecimal(orderBookDelta.getPrice());
                    BigDecimal qty = new BigDecimal(orderBookDelta.getQty());
                    if (qty.compareTo(BigDecimal.ZERO) == 0) {
                        bids.remove(price);
                    } else {
                        bids.put(price, qty);
                    }
                    markerDepthDao.setPoints(symbol, batchPoints, orderBookDelta,"BIDS");
                }
                markerDepthDao.executeBatch(batchPoints);
                simpMessagingTemplate.convertAndSend("/topic/all", getMarkerResponse(symbol));
            }
        });

    }


}

