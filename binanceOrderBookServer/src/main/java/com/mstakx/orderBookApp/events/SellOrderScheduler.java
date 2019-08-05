package com.mstakx.orderBookApp.events;

import org.springframework.scheduling.annotation.Scheduled;

public class SellOrderScheduler {

    @Scheduled(fixedRate = 1000)
    public void triggerSellEvent() {
        System.out.println("kshdkfshfkd");
    }


}
