package com.mstakx.orderBookApp.exception;

public class DBConnectionFailureException extends RuntimeException {

    public DBConnectionFailureException(String errorMessage, Throwable throwable) {
        super(errorMessage, throwable);
    }
}
