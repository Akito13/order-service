package com.example.bookshop.orderservice.event;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderPlacementEvent {
    private Long donhangId;
    private Long nguoiDungId;
    private Long dhctId;
    private Long sachId;
    private String tenSach;
    private Integer soLuong;
    private BigDecimal donGia;
    private BigDecimal tongTien;
    private Double phanTramGiam;
    private LocalDateTime thoiGianDat;
    private LocalDateTime thoiGianXuat;
    private String trangThai;
    private Boolean hoanTra;
}
