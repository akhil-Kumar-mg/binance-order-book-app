package com.mstakx.orderBookApp.transaction.buy.conditions;

import com.mstakx.orderBookApp.service.MarketDepthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class BuyConditionTwo implements BuyCondition {

    @Autowired
    MarketDepthService marketDepthService;

    @Override
    public Boolean condition(String symbol, Long time) {
        BigDecimal currentPrice = marketDepthService.getCurrentPrice(symbol, time* 1000000);
        if(currentPrice == null) return false;
        BigDecimal delta = marketDepthService.getDeltaByTimeRange(time * 1000000, ((time - 600 * 1000) * 1000000), symbol, currentPrice);
        if(delta == null) return false;
        if(delta.abs().divideToIntegralValue(currentPrice).multiply(new BigDecimal(100)).compareTo(new BigDecimal(1.5)) == -1) {
            return true;
        }
        return false;

    }
}
