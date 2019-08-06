package com.mstakx.orderBookApp.transaction.buy;

import com.mstakx.orderBookApp.dao.MarketDepthDao;
import com.mstakx.orderBookApp.service.MarketDepthService;
import com.mstakx.orderBookApp.transaction.buy.conditions.BuyConditionFour;
import com.mstakx.orderBookApp.transaction.buy.conditions.BuyConditionOne;
import com.mstakx.orderBookApp.transaction.buy.conditions.BuyConditionThree;
import com.mstakx.orderBookApp.transaction.buy.conditions.BuyConditionTwo;
import org.influxdb.dto.BatchPoints;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.BigInteger;

@Component
public class OrderBuy {

    @Autowired
    MarketDepthService marketDepthService;

    @Autowired
    MarketDepthDao marketDepthDao;

    @Autowired
    BuyConditionOne buyConditionOne;

    @Autowired
    BuyConditionTwo buyConditionTwo;

    @Autowired
    BuyConditionThree buyConditionThree;

    @Autowired
    BuyConditionFour buyConditionFour;

    public void buy(String symbol, Long time) {
        if (areBuyConditionsMet(symbol, time)) {
            BigDecimal currentPrice = marketDepthService.getCurrentPrice(symbol, time*1000000 + 10000);
            BigDecimal buyPrice = currentPrice.divide(new BigDecimal(200)).add(currentPrice);
            BigDecimal sellPrice = buyPrice.multiply(new BigDecimal(0.05)).add(buyPrice);
            BigDecimal stopLoss = buyPrice.subtract(buyPrice.multiply(new BigDecimal(0.07)));
            BatchPoints batchPoints = marketDepthDao.initiateBatchPoint();
            marketDepthDao.setPointsForOrderHistory(null, symbol, batchPoints, buyPrice, sellPrice, new BigDecimal(BigInteger.ZERO), "ALIVE", stopLoss);
            marketDepthDao.executeBatch(batchPoints);
        }
    }

    private Boolean areBuyConditionsMet(String symbol, Long time) {
        if (buyConditionOne.condition(symbol, time) && buyConditionTwo.condition(symbol, time)
                && buyConditionThree.condition(symbol, time) && buyConditionFour.condition(symbol, time)) {
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            buyConditionFour.setMinBtcVolume(new BigDecimal(0.5).multiply(buyConditionFour.getBtcVolume()));
            if (buyConditionThree.condition(symbol, time + 10000) && buyConditionFour.condition(symbol, time + 10000)) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }
}
