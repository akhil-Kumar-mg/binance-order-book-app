package com.mstakx.orderBookApp.dao;

import com.binance.api.client.domain.market.OrderBook;
import com.binance.api.client.domain.market.OrderBookEntry;
import com.mstakx.orderBookApp.config.InfluxDbConfig;
import com.mstakx.orderBookApp.measurements.Orders;
import com.mstakx.orderBookApp.measurements.PriceHistory;
import com.mstakx.orderBookApp.util.AnnotationUtil;
import org.influxdb.InfluxDB;
import org.influxdb.dto.BatchPoints;
import org.influxdb.dto.Point;
import org.influxdb.dto.Query;
import org.influxdb.dto.QueryResult;
import org.influxdb.impl.InfluxDBResultMapper;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.NavigableMap;
import java.util.concurrent.TimeUnit;

@Component
public class MarketDepthDao {

    public void saveOrderBookToCacheAndDB(OrderBook orderBook, String symbol, NavigableMap<BigDecimal, BigDecimal> asks, NavigableMap<BigDecimal, BigDecimal> bids) {
        InfluxDB connection = InfluxDbConfig.getConnection();
        BatchPoints batchPoints = initiateBatchPoint();

        for (OrderBookEntry ask : orderBook.getAsks()) {
            asks.put(new BigDecimal(ask.getPrice()), new BigDecimal(ask.getQty()));
            setPointsForAsksOrBids(symbol, batchPoints, ask, "ASKS");
        }

        for (OrderBookEntry bid : orderBook.getBids()) {
            bids.put(new BigDecimal(bid.getPrice()), new BigDecimal(bid.getQty()));
            if (connection != null)
                setPointsForAsksOrBids(symbol, batchPoints, bid, "BIDS");
        }
        BigDecimal currentPrice = bids.firstKey().add(asks.firstKey()).divide(new BigDecimal(2));
        setPointsForCurrentPrice(symbol, batchPoints, currentPrice);
        connection.write(batchPoints);
        connection.disableBatch();
    }

    public void saveOrderBookToCache(OrderBook orderBook, String symbol, NavigableMap<BigDecimal, BigDecimal> asks, NavigableMap<BigDecimal, BigDecimal> bids) {
        for (OrderBookEntry ask : orderBook.getAsks()) {
            asks.put(new BigDecimal(ask.getPrice()), new BigDecimal(ask.getQty()));
        }
        for (OrderBookEntry bid : orderBook.getBids()) {
            bids.put(new BigDecimal(bid.getPrice()), new BigDecimal(bid.getQty()));
        }
    }

    public BatchPoints initiateBatchPoint() {
        InfluxDB connection = InfluxDbConfig.getConnection();
        BatchPoints batchPoints = null;
        if (connection != null) {
            batchPoints = BatchPoints
                    .database("trading_DB")
                    .retentionPolicy("defaultPolicy")
                    .build();
        }
        return batchPoints;
    }

    public void executeBatch(BatchPoints batchPoints) {
        InfluxDB connection = InfluxDbConfig.getConnection();
        connection.write(batchPoints);
        connection.disableBatch();
    }

    public void setPointsForAsksOrBids(String symbol, BatchPoints batchPoints, OrderBookEntry orderBookEntry, String orderNature) {
        String measurement;
        if (orderNature.equalsIgnoreCase("ASKS")) {
            measurement = "order_book_" + symbol + "_asks";
        } else {
            measurement = "order_book_" + symbol + "_bids";
        }
        Point point = Point.measurement(measurement)
                .time(System.currentTimeMillis(), TimeUnit.MILLISECONDS)
                .addField("symbol", symbol)
                .addField("order_nature", orderNature)
                .addField("price(BTC)", new BigDecimal(orderBookEntry.getPrice()))
                .addField("quantity", new BigDecimal(orderBookEntry.getQty()))
                .build();
        batchPoints.point(point);
        /*
            work around to make a change in the timestamp
            Currently we can't specify a nano time through point. Need to modify when the update comes
         */
        try {
            Thread.sleep(1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void setPointsForCurrentPrice(String symbol, BatchPoints batchPoints, BigDecimal currentPrice) {

        Point point = Point.measurement("price_history_" + symbol)
                .time(System.currentTimeMillis(), TimeUnit.MILLISECONDS)
                .addField("price", currentPrice)
                .build();
        batchPoints.point(point);
        /*
            work around to make a change in the timestamp
            Currently we can't specify a nano time through point. Need to modify when the update comes
         */
        try {
            Thread.sleep(1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void setPointsForOrderHistory(String symbol, BatchPoints batchPoints, BigDecimal buyPrice,
                                         BigDecimal sellPrice, BigDecimal profit, String orderStatus, BigDecimal stopLoss) {

        Point point = Point.measurement("order_history")
                .time(System.currentTimeMillis(), TimeUnit.MILLISECONDS)
                .addField("symbol", symbol)
                .addField("order_status", orderStatus)
                .addField("buy_price", buyPrice)
                .addField("sell_price", sellPrice)
                .addField("stop_loss", stopLoss)
                .addField("profit", profit)
                .build();
        batchPoints.point(point);
        /*
            work around to make a change in the timestamp
            Currently we can't specify a nano time through point. Need to modify when the update comes
         */
        try {
            Thread.sleep(1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public List<com.mstakx.orderBookApp.measurements.OrderBook> getLiveBidsBySymbol(String symbol, Long time) {
        InfluxDB connection = InfluxDbConfig.getConnection();
        QueryResult queryResult;
        if (time != null) {
            queryResult = connection.query(new Query("SELECT * FROM order_book_" + symbol + "_bids where time<" + time, "trading_DB"));
        } else {
            queryResult = connection.query(new Query("SELECT * FROM order_book_" + symbol + "_bids", "trading_DB"));
        }
        AnnotationUtil.alterAnnotationValue("order_book_" + symbol + "_bids", com.mstakx.orderBookApp.measurements.OrderBook.class);
        InfluxDBResultMapper resultMapper = new InfluxDBResultMapper();
        return resultMapper.toPOJO(queryResult, com.mstakx.orderBookApp.measurements.OrderBook.class);
    }

    public List<com.mstakx.orderBookApp.measurements.OrderBook> getLiveAsksBySymbol(String symbol, Long time) {
        InfluxDB connection = InfluxDbConfig.getConnection();
        QueryResult queryResult;
        if (time != null) {
            queryResult = connection.query(new Query("SELECT * FROM order_book_" + symbol + "_asks where time<" + time, "trading_DB"));
        } else {
            queryResult = connection.query(new Query("SELECT * FROM order_book_" + symbol + "_asks", "trading_DB"));
        }
        AnnotationUtil.alterAnnotationValue("order_book_" + symbol + "_asks", com.mstakx.orderBookApp.measurements.OrderBook.class);
        InfluxDBResultMapper resultMapper = new InfluxDBResultMapper();
        return resultMapper.toPOJO(queryResult, com.mstakx.orderBookApp.measurements.OrderBook.class);
    }

    public List<PriceHistory> getDeltaByTimeRange(Long startTime, Long endTime, String symbol) {
        InfluxDB connection = InfluxDbConfig.getConnection();
        QueryResult queryResult = connection.query(new Query("SELECT * FROM price_history_" + symbol + " where time>" + endTime + " and time<" + startTime, "trading_DB"));
        AnnotationUtil.alterAnnotationValue("price_history_" + symbol, PriceHistory.class);
        InfluxDBResultMapper resultMapper = new InfluxDBResultMapper();
        return resultMapper.toPOJO(queryResult, com.mstakx.orderBookApp.measurements.PriceHistory.class);
    }

    public List<Orders> getAllActiveOrders() {
        InfluxDB connection = InfluxDbConfig.getConnection();
        QueryResult queryResult = connection.query(new Query("SELECT * FROM order_history where order_status=ALIVE", "trading_DB"));
        InfluxDBResultMapper resultMapper = new InfluxDBResultMapper();
        return resultMapper.toPOJO(queryResult, com.mstakx.orderBookApp.measurements.Orders.class);

    }
}
