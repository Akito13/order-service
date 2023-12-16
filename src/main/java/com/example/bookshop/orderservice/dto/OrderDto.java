package com.example.bookshop.orderservice.dto;
import com.example.bookshop.orderservice.model.OrderStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.validator.constraints.Length;

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

    @NotBlank(message = "Tối đa 2 ký tự")
    @Length(min = 2, message = "Tối đa 2 ký tự")
    private String tenNguoiNhan;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    private LocalDateTime thoiGianDat;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    private LocalDateTime thoiGianXuat;

    @NotNull(message = "Chưa có id người dùng")
    private Long nguoiDungId;

//    @NotBlank(message = "Trạng thái trống")
    private OrderStatus trangThai;

    @NotNull(message = "Tổng tiền trống")
    @Min(value = 0, message = "Giá không dưới 1đ")
    private BigDecimal tongTien;

    @NotEmpty(message = "Giỏ hàng rỗng")
    private List<OrderDetailDto> orderDetails;
}
