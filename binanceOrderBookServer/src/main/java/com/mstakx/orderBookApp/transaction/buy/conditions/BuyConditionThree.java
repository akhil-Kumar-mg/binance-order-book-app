package com.mstakx.orderBookApp.transaction.buy.conditions;

import com.mstakx.orderBookApp.service.MarketDepthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Map;
import java.util.NavigableMap;

@Service
public class BuyConditionThree implements BuyCondition {

    @Autowired
    MarketDepthService marketDepthService;

    private BigDecimal btcVolume = new BigDecimal(BigInteger.ZERO);

    @Override
    public Boolean condition(String symbol, Long time) {
        NavigableMap<BigDecimal, BigDecimal> liveBids = marketDepthService.getLiveBidsBySymbol(symbol, time*1000000);
        if(liveBids.size() ==0) return false;
        BigDecimal lowerLimit = liveBids.firstKey().multiply(new BigDecimal(97)).divide(new BigDecimal(100));
        BigDecimal totalBTC = new BigDecimal(BigInteger.ZERO);
        for (Map.Entry<BigDecimal, BigDecimal> entry : liveBids.entrySet()) {
            BigDecimal item = entry.getKey();
            BigDecimal value = entry.getValue();
            if(item.compareTo(lowerLimit) == -1) break;
            totalBTC = totalBTC.add((item.multiply(value)));
        }
        if (totalBTC.compareTo(new BigDecimal(3)) >= 0) {
            btcVolume = totalBTC;
            return true;
        } else {
            return false;
        }
    }

    public BigDecimal getBtcVolume() {
        return btcVolume;
    }
}
