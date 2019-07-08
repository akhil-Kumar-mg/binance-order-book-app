package com.mstakx.orderBookApp.util;

import com.binance.api.client.BinanceApiClientFactory;
import com.binance.api.client.BinanceApiRestClient;
import com.binance.api.client.BinanceApiWebSocketClient;
import com.binance.api.client.domain.market.OrderBook;

public class BinanceApiUtil {

    private static BinanceApiClientFactory factory = BinanceApiClientFactory.newInstance();

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
}
