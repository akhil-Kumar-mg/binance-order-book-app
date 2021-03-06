package com.mstakx.orderBookApp;

import com.mstakx.orderBookApp.transaction.buy.OrderBuy;
import com.mstakx.orderBookApp.transaction.sell.OrderSell;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {BinanceAppApplication.class})
public class testBuyOrderStrategy {

    @Autowired
    OrderBuy orderBuy;

    @Autowired
    OrderSell orderSell;

    @Test
    public void testBuyOrderStrategy() {
        orderBuy.buy("ETHBTC", System.currentTimeMillis());
    }

    @Test
    public void testSellOrderStrategy() {
        orderSell.sell();
    }
}
