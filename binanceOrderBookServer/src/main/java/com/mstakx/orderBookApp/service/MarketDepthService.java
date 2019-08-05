package com.mstakx.orderBookApp.service;

import com.binance.api.client.BinanceApiWebSocketClient;
import com.binance.api.client.domain.market.OrderBook;
import com.binance.api.client.domain.market.OrderBookEntry;
import com.mstakx.orderBookApp.config.InfluxDbConfig;
import com.mstakx.orderBookApp.dao.MarketDepthDao;
import com.mstakx.orderBookApp.dto.MarketDepthResponse;
import com.mstakx.orderBookApp.measurements.Orders;
import com.mstakx.orderBookApp.measurements.PriceHistory;
import com.mstakx.orderBookApp.util.BinanceApiUtil;
import com.mstakx.orderBookApp.util.MarketDepthCacheUtil;
import org.influxdb.InfluxDB;
import org.influxdb.dto.BatchPoints;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;

@Service
public class MarketDepthService {

    @Autowired
    SimpMessagingTemplate simpMessagingTemplate;

    @Autowired
    MarketDepthDao marketDepthDao;

    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;

    /*
        it will fetch the current market depth of the given pair and trigger depth listener for the same
        if the database persistence is disabled it will not store the changes to database
     */
    public MarketDepthResponse getMarketDepthForAPair(String symbol) {
        NavigableMap<BigDecimal, BigDecimal> asks = MarketDepthCacheUtil.getMarketDepthCacheForAsksBySymbol(symbol);
        NavigableMap<BigDecimal, BigDecimal> bids = MarketDepthCacheUtil.getMarketDepthCacheForBidsBySymbol(symbol);

        InfluxDB connection = InfluxDbConfig.getConnection();
        /*
            If the given pair is not called before, the initial cache will be empty,
            in such case we call the rest api to get the initial cache and store it in the map
            Also, trigger the web socket listener for the same
         */
        if (asks.size() == 0 && bids.size() == 0) {
            OrderBook orderBook = BinanceApiUtil.getOrderBookFromBinanceRestClient(symbol, 20);
            if (connection != null) {
                marketDepthDao.saveOrderBookToCacheAndDB(orderBook, symbol, asks, bids);
            } else {
                marketDepthDao.saveOrderBookToCache(orderBook, symbol, asks, bids);
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

    public void startDepthEventListenerForPair(String symbol) {

        BinanceApiWebSocketClient client = BinanceApiUtil.getBinanceWebSocketClient();
        client.onDepthEvent(symbol.toLowerCase(), res -> {
            Long currentUpdatedId = res.getUpdateId();
            Long lastUpdatedId = MarketDepthCacheUtil.getLastUpdatedId(symbol);
            if (lastUpdatedId == null || currentUpdatedId > lastUpdatedId) {
                BatchPoints batchPoints = marketDepthDao.initiateBatchPoint();
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
                    marketDepthDao.setPointsForAsksOrBids(symbol, batchPoints, orderBookDelta, "ASKS");
                }

                for (OrderBookEntry orderBookDelta : res.getBids()) {
                    BigDecimal price = new BigDecimal(orderBookDelta.getPrice());
                    BigDecimal qty = new BigDecimal(orderBookDelta.getQty());
                    if (qty.compareTo(BigDecimal.ZERO) == 0) {
                        bids.remove(price);
                    } else {
                        bids.put(price, qty);
                    }
                    marketDepthDao.setPointsForAsksOrBids(symbol, batchPoints, orderBookDelta, "BIDS");
                }
                if(bids.size() >0 && asks.size() >0) {
                    BigDecimal currentPrice = bids.firstKey().add(asks.firstKey()).divide(new BigDecimal(2));
                    marketDepthDao.setPointsForCurrentPrice(symbol, batchPoints, currentPrice);
                }
                marketDepthDao.executeBatch(batchPoints);
                MarketDepthCacheUtil.setCurrentUpdatedId(symbol, currentUpdatedId);
                simpMessagingTemplate.convertAndSend("/topic/all", getMarkerResponse(symbol));
            }
        });

    }

    public NavigableMap<BigDecimal, BigDecimal> getLiveBidsBySymbol(String symbol, Long time) {
        List<com.mstakx.orderBookApp.measurements.OrderBook> bidList = marketDepthDao.getLiveBidsBySymbol(symbol, time);
        NavigableMap<BigDecimal, BigDecimal> liveBidsList = new TreeMap<>(Comparator.reverseOrder());
        bidList.forEach(item -> {
            if (new BigDecimal(item.getQuantity()).compareTo(BigDecimal.ZERO) != 0)
                liveBidsList.put(new BigDecimal(item.getPrice()), new BigDecimal(item.getQuantity()));
        });
        return liveBidsList;
    }

    public NavigableMap<BigDecimal, BigDecimal> getLiveAsksBySymbol(String symbol, Long time) {
        List<com.mstakx.orderBookApp.measurements.OrderBook> askList = marketDepthDao.getLiveAsksBySymbol(symbol, time);
        NavigableMap<BigDecimal, BigDecimal> liveAsksList = new TreeMap<>(Comparator.naturalOrder());
        askList.forEach(item -> {
            if (new BigDecimal(item.getQuantity()).compareTo(BigDecimal.ZERO) != 0)
                liveAsksList.put(new BigDecimal(item.getPrice()), new BigDecimal(item.getQuantity()));
        });
        return liveAsksList;
    }

    public BigDecimal getDeltaByTimeRange(Long startTime, Long endTime, String symbol, BigDecimal currentPrice) {
        List<PriceHistory> priceHistoryList = marketDepthDao.getDeltaByTimeRange(startTime, endTime, symbol);
        if(priceHistoryList.size() ==0) return null;
        BigDecimal minPrice = new BigDecimal(10000000);
        for(PriceHistory priceHistory : priceHistoryList) {
           if(new BigDecimal(priceHistory.getPrice()).compareTo(minPrice) == -1 ) {
                minPrice = new BigDecimal(priceHistory.getPrice());
           }
        }
        BigDecimal delta = currentPrice.subtract(minPrice);
        return delta;
    }

    public BigDecimal getCurrentPrice(String symbol, Long time) {
        NavigableMap<BigDecimal, BigDecimal> liveAsks = getLiveAsksBySymbol(symbol, time);
        NavigableMap<BigDecimal, BigDecimal> liveBids = getLiveBidsBySymbol(symbol, time);
        BigDecimal currentPrice;
        if(liveAsks.size() >0 && liveBids.size() >0) {
            return currentPrice = liveBids.firstKey().add(liveAsks.firstKey()).divide(new BigDecimal(2));
        } else {
            return null;
        }
    }

    public List<Orders> getAllActiveOrders() {
        List<Orders> activeOrders = marketDepthDao.getAllActiveOrders();
        return activeOrders;
    }
}

