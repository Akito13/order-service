package com.example.bookshop.orderservice.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "trangthaidonhang")
public class OrderStatus {
    @Id
    @Column(name = "trang_thai_id")
    private String trangThaiId;

    @Column(name = "trang_thai_ten")
    private String tenTrangThai;

    @JsonIgnore
    @OneToMany(mappedBy = "trangThai")
    private List<Order> orders;
}
