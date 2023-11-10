package com.example.bookshop.orderservice.exception;

public class InvalidBodyException extends RuntimeException{
    public InvalidBodyException(String message) {
        super(message);
    }
}
