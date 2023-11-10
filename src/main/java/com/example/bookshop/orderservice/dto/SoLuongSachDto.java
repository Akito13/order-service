package com.example.bookshop.orderservice.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SoLuongSachDto implements Comparable<Long>{
    private Long sachId;
    private String tenSach;
    private Integer soLuong;
    private Boolean trangThai;
    private Double phanTramGiam;
    private BigDecimal donGia;

    @Override
    public int compareTo(Long o) {
        return this.sachId.compareTo(o);
    }
}
