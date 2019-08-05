package com.mstakx.orderBookApp.measurements;

import org.influxdb.annotation.Column;
import org.influxdb.annotation.Measurement;

import java.time.Instant;

@Measurement(name = "price_history")
public class PriceHistory {

    @Column(name = "time")
    private Instant time;

    @Column(name = "price")
    private String price;

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
}
