package com.example.bookshop.orderservice.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "DonHang")
public class Order {
    @Id
    private Long donhangId;
    @Column(name = "sdt")
    private String sdt;

    @Column(name = "dia_chi")
    private String diaChi;

    @Column(name = "thoi_gian_dat")
    private LocalDateTime thoiGianDat;

    @Column(name = "thoi_gian_xuat")
    private LocalDateTime thoiGianXuat;

    @Column(name = "nguoi_dung_id")
    private Long nguoiDungId;

    @ManyToOne
    @JoinColumn(name = "trang_thai_id")
    private OrderStatus trangThai;

    @Column(name = "tong_tien")
    private BigDecimal tongTien;

    @OneToMany(mappedBy = "donHang")
    private List<OrderDetails> orderDetails;
}
