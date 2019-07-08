package com.mstakx.orderBookApp.model;

import org.influxdb.annotation.Column;
import org.influxdb.annotation.Measurement;

@Measurement(name = "btc-pair")
public class CryptoPair {

    @Column(name = "symbol")
    private String symbol;
}
