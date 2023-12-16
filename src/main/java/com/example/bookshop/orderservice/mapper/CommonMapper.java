package com.example.bookshop.orderservice.mapper;

import com.example.bookshop.orderservice.dto.OrderDetailDto;
import com.example.bookshop.orderservice.dto.OrderDto;
import com.example.bookshop.orderservice.dto.SoLuongSachDto;
import com.example.bookshop.orderservice.exception.ErrorResponseDto;
import com.example.bookshop.orderservice.model.Order;
import com.example.bookshop.orderservice.model.OrderDetails;
import com.example.bookshop.orderservice.model.OrderStatus;
import org.springframework.http.HttpStatus;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public class CommonMapper {
    public static Order mapToOrder(OrderStatus orderStatus, OrderDto orderDto) {
        return Order.builder()
                .nguoiDungId(orderDto.getNguoiDungId())
                .thoiGianDat(orderDto.getThoiGianDat() == null ? LocalDateTime.now() : orderDto.getThoiGianDat())
                .trangThai(orderStatus)
                .diaChi(orderDto.getDiaChi())
                .tenNguoiNhan(orderDto.getTenNguoiNhan())
                .sdt(orderDto.getSdt()).build();
    }

    public static OrderDetails mapToOrderDetails(Order order, SoLuongSachDto foundSoLuongSachDto, OrderDetailDto details) {
        return OrderDetails.builder()
                .sachId(details.getSachId())
                .tenSach(foundSoLuongSachDto.getTenSach())
                .soLuong(details.getSoLuong())
                .phanTramGiam(foundSoLuongSachDto.getPhanTramGiam())
                .donGia(foundSoLuongSachDto.getDonGia())
                .donhangId(order)
                .tongTien(details.getTongTien()).build();
    }

    public static OrderDto mapToOrderDto(Order order) {
        return OrderDto.builder()
                .diaChi(order.getDiaChi())
                .donhangId(order.getDonhangId())
                .nguoiDungId(order.getNguoiDungId())
                .tenNguoiNhan(order.getTenNguoiNhan())
                .sdt(order.getSdt())
                .thoiGianXuat(order.getThoiGianXuat())
                .tongTien(order.getTongTien())
                .thoiGianDat(order.getThoiGianDat())
                .trangThai(order.getTrangThai()).build();
    }

    public static List<OrderDetailDto> mapToListOrderDetailDto(List<OrderDetails> orderDetails) {
        return orderDetails.stream().map(CommonMapper::mapToOrderDetailDto).toList();
    }

    public static OrderDetailDto mapToOrderDetailDto(OrderDetails orderDetails) {
        return OrderDetailDto.builder()
                .tenSach(orderDetails.getTenSach())
                .sachId(orderDetails.getSachId())
                .donGia(orderDetails.getDonGia())
                .phanTramGiam(orderDetails.getPhanTramGiam())
                .soLuong(orderDetails.getSoLuong())
                .tongTien(orderDetails.getTongTien()).build();
    }

    public static ErrorResponseDto buildErrorResponse(Throwable exception, WebRequest request, Map<String, String> errors, HttpStatus httpStatus){
        return ErrorResponseDto.builder()
                .apiPath(request.getDescription(false))
                .message(exception.getMessage())
                .timestamp(LocalDateTime.now())
                .statusCode(httpStatus)
                .errors(errors)
                .build();
    }
}
