package com.mstakx.orderBookApp.measurements;

import org.influxdb.annotation.Column;
import org.influxdb.annotation.Measurement;

import java.time.Instant;

@Measurement(name = "order_book")
public class OrderBook {

    @Column(name = "time")
    private Instant time;

    @Column(name = "price(BTC)")
    private String price;

    @Column(name = "quantity")
    private String quantity;

    public Instant getTime() {
        return time;
    }

    public void setTime(Instant time) {
        this.time = time;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getQuantity() {
        return quantity;
    }

    public void setQuantity(String quantity) {
        this.quantity = quantity;
    }
}
