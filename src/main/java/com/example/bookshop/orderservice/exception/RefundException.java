package com.example.bookshop.orderservice.exception;

public class RefundException extends RuntimeException{
    public RefundException(String message) {
        super(message);
    }
}
