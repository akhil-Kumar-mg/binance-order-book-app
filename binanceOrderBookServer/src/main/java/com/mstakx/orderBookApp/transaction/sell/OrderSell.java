package com.mstakx.orderBookApp.transaction.sell;

import com.mstakx.orderBookApp.dao.MarketDepthDao;
import com.mstakx.orderBookApp.measurements.Orders;
import com.mstakx.orderBookApp.service.MarketDepthService;
import org.influxdb.dto.BatchPoints;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class OrderSell {

    @Autowired
    MarketDepthService marketDepthService;

    @Autowired
    MarketDepthDao marketDepthDao;


    public void sell() {
        List<Orders> activeOrders = marketDepthService.getAllActiveOrders();
        activeOrders.forEach(order -> {
            BigDecimal currentPrice = marketDepthService.getCurrentPrice(order.getSymbol(), System.currentTimeMillis() * 1000000);

            BigDecimal stopLoss = new BigDecimal(order.getStopLoss());
            BigDecimal sellPrice = new BigDecimal(order.getSellPrice());
            BatchPoints batchPoints = marketDepthDao.initiateBatchPoint();

            if (sellPrice.compareTo(currentPrice) == -1) {
                marketDepthDao.setPointsForOrderHistory(order.getTime().getEpochSecond()*1000000000 + order.getTime().getNano(), order.getSymbol(), batchPoints, new BigDecimal(order.getBuyPrice()),
                        new BigDecimal(order.getSellPrice()), currentPrice.subtract(new BigDecimal(order.getBuyPrice())),
                        "EXECUTED", new BigDecimal(order.getStopLoss()));
            } else if (currentPrice.compareTo(stopLoss) == -1) {
                marketDepthDao.setPointsForOrderHistory(order.getTime().getEpochSecond()*1000000000 + order.getTime().getNano(), order.getSymbol(), batchPoints, new BigDecimal(order.getBuyPrice()),
                        new BigDecimal(order.getSellPrice()), currentPrice.subtract(new BigDecimal(order.getBuyPrice())),
                        "EXECUTED", new BigDecimal(order.getStopLoss()));
            } else {
                Long time = order.getTime().getEpochSecond()*1000000000 + order.getTime().getNano();
                if( time+ 10000000000l < System.currentTimeMillis() * 1000000 ) {
                    BigDecimal buyPrice = new BigDecimal(order.getBuyPrice());
                    sellPrice = buyPrice.multiply(new BigDecimal(0.01)).add(buyPrice);
                    marketDepthDao.setPointsForOrderHistory(order.getTime().getEpochSecond()*1000000000 + order.getTime().getNano(), order.getSymbol(), batchPoints, buyPrice,
                            sellPrice, currentPrice.subtract(buyPrice),
                            "ALIVE", new BigDecimal(order.getStopLoss()));
                } else {
                    marketDepthDao.setPointsForOrderHistory(order.getTime().getEpochSecond()*1000000000 + order.getTime().getNano(), order.getSymbol(), batchPoints, new BigDecimal(order.getBuyPrice()),
                            new BigDecimal(order.getSellPrice()), currentPrice.subtract(new BigDecimal(order.getBuyPrice())),
                            "ALIVE", new BigDecimal(order.getStopLoss()));
                }

            }
            marketDepthDao.executeBatch(batchPoints);
        });
    }


}
