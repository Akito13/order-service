package com.example.bookshop.orderservice.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Cascade;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "donhang")
@Table(name = "donhang")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long donhangId;

    @Column(name = "ten_nguoi_nhan")
    private String tenNguoiNhan;

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

    @Column(name = "thanh_toan_id")
    private String thanhToanId;

    @Column(name = "hoan_tra")
    private Boolean hoanTra;

    @OneToMany(mappedBy = "donhangId")
    @Cascade(org.hibernate.annotations.CascadeType.PERSIST)
    private List<OrderDetails> orderDetails;
}
