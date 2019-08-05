package com.mstakx.orderBookApp.dto;

import java.math.BigDecimal;
import java.util.NavigableMap;

public class MarketDepthResponse {

    private String symbol;

    private NavigableMap<BigDecimal,BigDecimal> marketDepthForAsks;

    private NavigableMap<BigDecimal,BigDecimal> marketDepthForBids;

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public NavigableMap<BigDecimal, BigDecimal> getMarketDepthForAsks() {
        return marketDepthForAsks;
    }

    public void setMarketDepthForAsks(NavigableMap<BigDecimal, BigDecimal> marketDepthForAsks) {
        this.marketDepthForAsks = marketDepthForAsks;
    }

    public NavigableMap<BigDecimal, BigDecimal> getMarketDepthForBids() {
        return marketDepthForBids;
    }

    public void setMarketDepthForBids(NavigableMap<BigDecimal, BigDecimal> marketDepthForBids) {
        this.marketDepthForBids = marketDepthForBids;
    }
}
