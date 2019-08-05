package com.mstakx.orderBookApp.transaction.buy.conditions;

import com.mstakx.orderBookApp.service.MarketDepthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Map;
import java.util.NavigableMap;

@Service
public class BuyConditionFour implements BuyCondition {

    @Autowired
    MarketDepthService marketDepthService;

    private BigDecimal btcVolume = new BigDecimal(BigInteger.ZERO);

    private BigDecimal minBtcVolume = new BigDecimal(3);

    @Override
    public Boolean condition(String symbol, Long time) {
        NavigableMap<BigDecimal, BigDecimal> liveAsks = marketDepthService.getLiveAsksBySymbol(symbol, time * 1000000);
        if (liveAsks.size() == 0) return false;
        BigDecimal upperLimit = liveAsks.firstKey().multiply(new BigDecimal(103)).divide(new BigDecimal(100));
        BigDecimal totalBTC = new BigDecimal(BigInteger.ZERO);
        for (Map.Entry<BigDecimal, BigDecimal> entry : liveAsks.entrySet()) {
            BigDecimal item = entry.getKey();
            BigDecimal value = entry.getValue();
            if (upperLimit.compareTo(item) == -1) break;
            totalBTC = totalBTC.add((item.multiply(value)));
        }
        if (totalBTC.compareTo(minBtcVolume) >= 0) {
            btcVolume = totalBTC;
            return true;
        } else {
            return false;
        }
    }

    public BigDecimal getBtcVolume() {
        return btcVolume;
    }

    public void setMinBtcVolume(BigDecimal minBtcVolume) {
        this.minBtcVolume = minBtcVolume;
    }
}
