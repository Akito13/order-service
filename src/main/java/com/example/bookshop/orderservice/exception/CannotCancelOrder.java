package com.example.bookshop.orderservice.exception;

public class CannotCancelOrder extends RuntimeException{
    public CannotCancelOrder(String message) {
        super(message);
    }
}
