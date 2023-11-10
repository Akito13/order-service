package com.example.bookshop.orderservice.model;

import jakarta.persistence.IdClass;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@EqualsAndHashCode
@Getter
@Setter
public class OrderDetailsPK implements Serializable {
    private Order donHang;
    private Long sachId;
}
