package com.mstakx.orderBookApp.exception;

public class BtcPairFileNotFound extends RuntimeException {

    public BtcPairFileNotFound(String errorMessage, Throwable throwable) {
        super(errorMessage, throwable);
    }

}
