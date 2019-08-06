package com.mstakx.orderBookApp.events;

import com.mstakx.orderBookApp.transaction.sell.OrderSell;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class SellOrderScheduler {

    @Autowired
    OrderSell orderSell;

    @Scheduled(fixedDelay = 100)
    public void triggerSellEvent() {
        orderSell.sell();
    }


}
