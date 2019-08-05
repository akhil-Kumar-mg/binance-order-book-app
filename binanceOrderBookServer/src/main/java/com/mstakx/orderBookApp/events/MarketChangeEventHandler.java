package com.mstakx.orderBookApp.events;

import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class MarketChangeEventHandler {



    @Async
    @EventListener
    public void handleMarketChangeEvent(MarketChangeEvent event) {

//        orderBuy.buy(event.getSymbol());
    }
}
