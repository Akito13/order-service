package com.example.bookshop.orderservice.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "TrangThaiDonHang")
public class OrderStatus {
    @Id
    @Column(name = "trang_thai_id")
    private String trangThaiId;

    @Column(name = "ten_trang_thai")
    private String tenTrangThai;

    @OneToMany(mappedBy = "trangThai")
    private List<Order> orders;
}
