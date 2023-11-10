package com.example.bookshop.orderservice.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "DonHangChiTiet")
@IdClass(OrderDetailsPK.class)
public class OrderDetails {
    @Id
    @Column(name = "donhang_id")
    @ManyToOne
    @JoinColumn(name = "donhang_id")
    private Order donHang;

    @Id
    private Long sachId;

    @Column(name = "ten_sach")
    private String tenSach;

    @Column(name = "so_luong")
    private Integer soLuong;

    @Column(name = "don_gia")
    private BigDecimal donGia;

    @Column(name = "tong_tien")
    private BigDecimal tongTien;

    @Column(name = "phan_tram_giam")
    private Double phanTramGiam;
        
}
