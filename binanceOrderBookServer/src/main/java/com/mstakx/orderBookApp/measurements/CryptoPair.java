package com.mstakx.orderBookApp.measurements;

import org.influxdb.annotation.Column;
import org.influxdb.annotation.Measurement;

import java.time.Instant;

@Measurement(name = "crypto_pair")
public class CryptoPair {

    @Column(name = "time")
    private Instant time;

    @Column(name = "symbol")
    private String symbol;

    public Instant getTime() {
        return time;
    }

    public void setTime(Instant time) {
        this.time = time;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }
}
