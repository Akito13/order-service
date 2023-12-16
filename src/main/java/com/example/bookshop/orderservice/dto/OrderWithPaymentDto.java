package com.example.bookshop.orderservice.dto;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderWithPaymentDto {
    private OrderDto order;
    private String paymentId;
    private String cardHolder;
//    private String receiptEmail;
}
