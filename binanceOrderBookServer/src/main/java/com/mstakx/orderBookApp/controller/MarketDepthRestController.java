package com.mstakx.orderBookApp.controller;

import com.mstakx.orderBookApp.dto.MarketDepthResponse;
import com.mstakx.orderBookApp.service.MarketDepthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin("*")
public class MarketDepthRestController {

    @Autowired
    MarketDepthService marketDepthService;

    @GetMapping(value = "marketDepth/{btcPair}")
    @MessageMapping("/all")
    public ResponseEntity getMarketDepthByPair(@PathVariable(value = "btcPair") String btcPair) {
        MarketDepthResponse marketDepthResponse = marketDepthService.getMarketDepthForAPair(btcPair);
        if (marketDepthResponse.getMarketDepthForAsks().size() != 0) {
            return new ResponseEntity(marketDepthResponse, HttpStatus.OK);
        }
        return new ResponseEntity("Error while fetching the data", HttpStatus.OK);
    }
}
