package com.mstakx.orderBookApp.transaction.buy.conditions;

public interface BuyCondition {

    Boolean condition(String symbol, Long time);
}
