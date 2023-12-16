package com.example.bookshop.orderservice.exception;

import com.stripe.exception.StripeException;

public class PaymentException extends StripeException {
    public PaymentException(String message, String requestId, String code, Integer statusCode) {
        super(message, requestId, code, statusCode);
    }
}
