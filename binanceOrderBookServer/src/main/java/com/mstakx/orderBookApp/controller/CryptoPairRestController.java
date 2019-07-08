package com.mstakx.orderBookApp.controller;

import com.mstakx.orderBookApp.model.CryptoPair;
import com.mstakx.orderBookApp.service.OrderBookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class CryptoPairRestController {

    @Autowired
    OrderBookService orderBookService;

    @GetMapping(value = "cryptoPair")
    public ResponseEntity getAllCryptoPair() {
        List<CryptoPair> cryptoPairs = orderBookService.getAllCryptoPairs();
        if (cryptoPairs.size() != 0) {
            return new ResponseEntity(cryptoPairs, HttpStatus.OK);
        }
        return new ResponseEntity("Error while fetching the crypto pair list", HttpStatus.OK);
    }
}
