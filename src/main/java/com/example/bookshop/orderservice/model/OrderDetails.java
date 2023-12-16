package com.example.bookshop.orderservice.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "donhangchitiet")
@Table(name = "donhangchitiet")
//@IdClass(OrderDetailsPK.class)
public class OrderDetails {
//    @Id
    @ManyToOne
    @JoinColumn(name = "donhang_id")
    private Order donhangId;

//    @Id
    @Column(name = "sach_id")
    private Long sachId;

    @Id
    @Column(name = "dhct_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long dhctId;

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
