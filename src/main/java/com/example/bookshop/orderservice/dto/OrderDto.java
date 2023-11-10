package com.example.bookshop.orderservice.dto;

import com.example.bookshop.orderservice.model.OrderDetails;
import com.example.bookshop.orderservice.model.OrderStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.Column;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;


@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderDto {
    private Long donhangId;
    @NotBlank(message = "Số điện thoại chưa có")
    private String sdt;

    @NotBlank(message = "Địa chỉ chưa có")
    private String diaChi;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDateTime thoiGianDat;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDateTime thoiGianXuat;

    @NotNull(message = "Chưa có id người dùng")
    private Long nguoiDungId;

    @NotBlank(message = "Trạng thái trống")
    private String trangThai;

    @NotNull(message = "Tổng tiền trống")
    @Min(value = 0, message = "Giá không dưới 1đ")
    private BigDecimal tongTien;


    private List<OrderDetailDto> orderDetails;
}
