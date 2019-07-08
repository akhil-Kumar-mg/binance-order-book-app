package com.mstakx.orderBookApp.util;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.HashMap;
import java.util.NavigableMap;
import java.util.TreeMap;

public class MarketDepthCacheUtil {

    private static HashMap<String,NavigableMap<BigDecimal,BigDecimal>> marketDepthCacheForAsks = new HashMap<>();
    private static HashMap<String,NavigableMap<BigDecimal,BigDecimal>> marketDepthCacheForBids = new HashMap<>();
    private static HashMap<String,Long> updatedIdMap= new HashMap<>();

    private MarketDepthCacheUtil(){}

    public static NavigableMap<BigDecimal, BigDecimal> getMarketDepthCacheForAsksBySymbol(String symbol) {
        if(marketDepthCacheForAsks.get(symbol) == null) {
            marketDepthCacheForAsks.put(symbol, new TreeMap<>(Comparator.naturalOrder()));
            return marketDepthCacheForAsks.get(symbol);
        }
        return marketDepthCacheForAsks.get(symbol);
    }

    public static NavigableMap<BigDecimal, BigDecimal> getMarketDepthCacheForBidsBySymbol(String symbol) {
        if(marketDepthCacheForBids.get(symbol) == null) {
            marketDepthCacheForBids.put(symbol, new TreeMap<>(Comparator.reverseOrder()));
            return marketDepthCacheForBids.get(symbol);
        }
        return marketDepthCacheForBids.get(symbol);
    }

    public static Long getLastUpdatedId(String symbol) {
        return updatedIdMap.get(symbol);
    }


    public static void setCurrentUpdatedId(String symbol, Long updatedId) {
        updatedIdMap.put(symbol,updatedId);
    }

}
