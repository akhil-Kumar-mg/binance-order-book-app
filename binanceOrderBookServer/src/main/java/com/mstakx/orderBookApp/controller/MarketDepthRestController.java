package com.mstakx.orderBookApp.controller;

import com.mstakx.orderBookApp.model.MarketDepthResponse;
import com.mstakx.orderBookApp.service.MarketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MarketDepthRestController {

    @Autowired
    MarketService marketService;

    @GetMapping(value = "marketDepth/{btcPair}")
    @MessageMapping("/all")
    public ResponseEntity getMarketDepth(@PathVariable(value = "btcPair") String btcPair) {
        MarketDepthResponse marketDepthResponse = marketService.getMarketDepthForAPair(btcPair);
        if (marketDepthResponse.getMarketDepthForAsks().size() != 0) {
            return new ResponseEntity(marketDepthResponse, HttpStatus.OK);
        }
        return new ResponseEntity("Error while fetching the data", HttpStatus.OK);
    }
}
