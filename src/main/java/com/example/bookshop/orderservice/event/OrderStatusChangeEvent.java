package com.example.bookshop.orderservice.event;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderStatusChangeEvent {
    private Long donhangId;
    private String trangThai;
    private Boolean hoanTra;
}
