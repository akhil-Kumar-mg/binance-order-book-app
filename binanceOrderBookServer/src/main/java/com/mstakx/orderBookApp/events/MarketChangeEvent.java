package com.mstakx.orderBookApp.events;

import org.springframework.context.ApplicationEvent;

public class MarketChangeEvent extends ApplicationEvent {
    private String symbol;

    public MarketChangeEvent(Object source, String symbol) {
        super(source);
        this.symbol = symbol;
    }

    public String getSymbol() {
        return symbol;
    }
}
