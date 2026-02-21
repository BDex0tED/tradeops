package com.tradeops.exceptions;

public class TraderNotActiveException extends RuntimeException {
    public TraderNotActiveException(String message) {
        super(message);
    }
}
