package com.example.bookshop.orderservice.dto;

import com.example.bookshop.orderservice.model.Order;
import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderDetailDto {

    @NotNull(message = "Id sách trống")
    @Min(value = 1,message = "id không nhỏ hơn 1")
    private Long sachId;

    @NotBlank(message = "Tên sách chưa có")
    private String tenSach;

    @NotNull(message = "Số lượng trống")
    @Min(value = 1, message = "Số lượng không nhỏ hơn 1")
    private Integer soLuong;

    @NotNull(message = "Đơn giá trống")
    @Min(value = 0, message = "Đơn giá không thể âm")
    private BigDecimal donGia;

    @NotNull(message = "Tổng tiền trống")
    @Min(value = 1, message = "Tổng tiền không nhỏ hơn 1")
    private BigDecimal tongTien;

    private Double phanTramGiam;
}
