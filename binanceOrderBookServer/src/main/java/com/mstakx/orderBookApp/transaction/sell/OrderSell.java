package com.mstakx.orderBookApp.transaction.sell;

import com.mstakx.orderBookApp.dao.MarketDepthDao;
import com.mstakx.orderBookApp.measurements.Orders;
import com.mstakx.orderBookApp.service.MarketDepthService;
import com.mstakx.orderBookApp.util.MarketDepthCacheUtil;
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
            BigDecimal currentPrice = MarketDepthCacheUtil.getCurrentPriceBySymbol(order.getSymbol());
            if (currentPrice == null) {
                currentPrice = marketDepthService.getCurrentPrice(order.getSymbol(), System.currentTimeMillis() * 1000000);
                MarketDepthCacheUtil.setCurrentPriceBySymbol(order.getSymbol(), currentPrice);
            }
            BigDecimal stopLoss = new BigDecimal(order.getStopLoss());
            BigDecimal sellPrice = new BigDecimal(order.getSellPrice());
            BatchPoints batchPoints = marketDepthDao.initiateBatchPoint();

            if (sellPrice.compareTo(currentPrice) == -1) {
                marketDepthDao.setPointsForOrderHistory(order.getSymbol(), batchPoints, new BigDecimal(order.getBuyPrice()),
                        new BigDecimal(order.getSellPrice()), currentPrice.subtract(new BigDecimal(order.getBuyPrice())),
                        "EXECUTED", new BigDecimal(order.getStopLoss()));
            } else if (currentPrice.compareTo(stopLoss) == -1) {
                marketDepthDao.setPointsForOrderHistory(order.getSymbol(), batchPoints, new BigDecimal(order.getBuyPrice()),
                        new BigDecimal(order.getSellPrice()), currentPrice.subtract(new BigDecimal(order.getBuyPrice())),
                        "EXECUTED", new BigDecimal(order.getStopLoss()));
            } else {
                marketDepthDao.setPointsForOrderHistory(order.getSymbol(), batchPoints, new BigDecimal(order.getBuyPrice()),
                        new BigDecimal(order.getSellPrice()), currentPrice.subtract(new BigDecimal(order.getBuyPrice())),
                        "ALIVE", new BigDecimal(order.getStopLoss()));
            }
            marketDepthDao.executeBatch(batchPoints);
        });
    }


}
