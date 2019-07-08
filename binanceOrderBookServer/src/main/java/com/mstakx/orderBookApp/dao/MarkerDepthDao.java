package com.mstakx.orderBookApp.dao;

import com.binance.api.client.domain.market.OrderBook;
import com.binance.api.client.domain.market.OrderBookEntry;
import com.mstakx.orderBookApp.config.InfluxDbConfig;
import org.influxdb.InfluxDB;
import org.influxdb.dto.BatchPoints;
import org.influxdb.dto.Point;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.NavigableMap;
import java.util.concurrent.TimeUnit;

@Component
public class MarkerDepthDao {
    @Autowired
    InfluxDbConfig influxDbConfig;

    public void saveOrderBookToCacheAndDB(OrderBook orderBook, String symbol, NavigableMap<BigDecimal, BigDecimal> asks, NavigableMap<BigDecimal, BigDecimal> bids) {
        InfluxDB connection = influxDbConfig.getConnection();
        BatchPoints batchPoints = initiateBatchPoint();

        for (OrderBookEntry ask : orderBook.getAsks()) {
            asks.put(new BigDecimal(ask.getPrice()), new BigDecimal(ask.getQty()));
            setPoints(symbol, batchPoints, ask, "ASKS");
        }

        for (OrderBookEntry bid : orderBook.getBids()) {
            bids.put(new BigDecimal(bid.getPrice()), new BigDecimal(bid.getQty()));
            if (connection != null)
                setPoints(symbol, batchPoints, bid, "BIDS");
        }
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
        InfluxDB connection = influxDbConfig.getConnection();
        BatchPoints batchPoints = null;
        if (connection != null) {
            batchPoints = BatchPoints
                    .database("orderBook")
                    .retentionPolicy("defaultPolicy")
                    .build();
        }
        return batchPoints;
    }

    public void executeBatch(BatchPoints batchPoints) {
        InfluxDB connection = influxDbConfig.getConnection();
        connection.write(batchPoints);
        connection.disableBatch();
    }

    public void setPoints(String symbol, BatchPoints batchPoints, OrderBookEntry ask, String orderNature) {
        String key = "quantity_" + symbol.substring(0, symbol.length() - 3);
        String measurement;
        if(orderNature.equalsIgnoreCase("ASKS")) {
            measurement = "order_book_asks";
        }else {
            measurement = "order_book_bids";
        }
        Point point = Point.measurement(measurement)
                .time(System.currentTimeMillis(), TimeUnit.MILLISECONDS)
                .tag("symbol", symbol)
                .tag("order_nature", orderNature)
                .addField("price_BTC", ask.getPrice())
                .addField(key, ask.getQty())
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
}
