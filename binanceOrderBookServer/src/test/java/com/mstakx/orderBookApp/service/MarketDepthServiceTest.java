package com.mstakx.orderBookApp.service;

import com.mstakx.orderBookApp.BinanceAppApplication;
import com.mstakx.orderBookApp.dto.MarketDepthResponse;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {BinanceAppApplication.class})
public class MarketDepthServiceTest {

    @Autowired
    MarketDepthService marketDepthService;

    /*
        Validate the service to get the initial snapshot of a particular symbol
     */
    @Test
    public void getMarketDepthForAPair() {
        MarketDepthResponse marketDepthResponse = marketDepthService.getMarketDepthForAPair("ETHBTC");
        Assert.assertEquals("SnapShot size mismatch for asks", 20, marketDepthResponse.getMarketDepthForAsks().size());
        Assert.assertEquals("SnapShot size mismatch for bids", 20, marketDepthResponse.getMarketDepthForBids().size());
    }
}