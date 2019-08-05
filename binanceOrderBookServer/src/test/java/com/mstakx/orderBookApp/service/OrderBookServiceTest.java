package com.mstakx.orderBookApp.service;

import com.mstakx.orderBookApp.BinanceAppApplication;
import com.mstakx.orderBookApp.dto.CryptoPairDTO;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {BinanceAppApplication.class})
public class OrderBookServiceTest {

    @Autowired
    OrderBookService orderBookService;

    @Test
    public void loadBtcPair() {
        List<String> btcPairList = new ArrayList<>();
        btcPairList.add("ETHBTC");
        btcPairList.add("NEOBTC");
        orderBookService.loadBtcPair(btcPairList);
        List<CryptoPairDTO> cryptoPairDTOList = orderBookService.getAllCryptoPairs();
        Assert.assertEquals("Crypto Pair mismatch","ETHBTC", cryptoPairDTOList.get(cryptoPairDTOList.size()-2).getSymbol());
        Assert.assertEquals("Crypto Pair mismatch","NEOBTC", cryptoPairDTOList.get(cryptoPairDTOList.size()-1).getSymbol());
    }
}