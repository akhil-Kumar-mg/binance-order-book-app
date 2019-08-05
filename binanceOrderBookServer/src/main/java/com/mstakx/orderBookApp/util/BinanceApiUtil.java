package com.mstakx.orderBookApp.util;

import com.binance.api.client.BinanceApiAsyncRestClient;
import com.binance.api.client.BinanceApiClientFactory;
import com.binance.api.client.BinanceApiRestClient;
import com.binance.api.client.BinanceApiWebSocketClient;
import com.binance.api.client.domain.market.OrderBook;

public class BinanceApiUtil {

    private static BinanceApiClientFactory factory = BinanceApiClientFactory.newInstance("i2Nv7U1HC0rjnxUvJPuF1QTxoR4AFfG54sjs3VEDal9ixLY83uqt1L0pvv9wGY6R","KN6TSCn82fuonnF3mvFs63EvWPAcezAJHBPxbQtkvMBm8AWjRKya2xlpIPLKlgG9");

    public static OrderBook getOrderBookFromBinanceRestClient(String symbol, Integer size) {
        BinanceApiRestClient client = factory.newRestClient();
        return client.getOrderBook(symbol, size);
    }

    public static BinanceApiWebSocketClient getBinanceWebSocketClient() {
        return factory.newWebSocketClient();
    }

    public static String getLastPriceBySymbol(String symbol) {
        BinanceApiRestClient client = factory.newRestClient();
        return client.get24HrPriceStatistics(symbol).getLastPrice();
    }

    public static BinanceApiAsyncRestClient getBinanceAsyncRestClient() {
        return factory.newAsyncRestClient();
    }
}
