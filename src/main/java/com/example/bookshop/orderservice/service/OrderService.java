package com.example.bookshop.orderservice.service;

import com.example.bookshop.orderservice.dto.OrderDetailDto;
import com.example.bookshop.orderservice.dto.OrderDto;
import com.example.bookshop.orderservice.dto.SoLuongSachDto;
import com.example.bookshop.orderservice.exception.CannotCancelOrder;
import com.example.bookshop.orderservice.exception.InvalidBodyException;
import com.example.bookshop.orderservice.exception.OrderNotFoundException;
import com.example.bookshop.orderservice.exception.SachNotAvailableException;
import com.example.bookshop.orderservice.mapper.CommonMapper;
import com.example.bookshop.orderservice.model.Order;
import com.example.bookshop.orderservice.model.OrderDetails;
import com.example.bookshop.orderservice.model.OrderStatus;
import com.example.bookshop.orderservice.repository.OrderReponsitory;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class OrderService {

    private OrderReponsitory orderReponsitory;

    private final WebClient.Builder webClientBuilder;

    @Autowired
    public OrderService(OrderReponsitory orderReponsitory, WebClient.Builder webClientBuilder) {
        this.orderReponsitory = orderReponsitory;
        this.webClientBuilder = webClientBuilder;
    }

    public OrderDto createOrder(@Valid OrderDto orderDto) {
        List<OrderDetailDto> orderDetails = orderDto.getOrderDetails();
        List<Long> sachIds = orderDetails
                        .stream()
                        .map(OrderDetailDto::getSachId)
                        .toList();

        SoLuongSachDto[] soLuongSachDtos = webClientBuilder.build()
                .get()
                .uri("http://sach/api/sach/soLuongById",
                        uriBuilder -> uriBuilder.queryParam("sachIds", sachIds).build())
                .retrieve()
                .bodyToMono(SoLuongSachDto[].class)
                .block();
        try {
            OrderStatus orderStatus = new OrderStatus();
            orderStatus.setTrangThaiId("A");
            Order order = CommonMapper.mapToOrder(orderStatus, orderDto);
            List<OrderDetails> orderDetailsList = new ArrayList<>();
            orderDetails.forEach(details -> {
                int index = Arrays.binarySearch(soLuongSachDtos, details.getSachId());
                if(index >= 0) {
                    SoLuongSachDto foundSoLuongSachDto = soLuongSachDtos[index];
                    if (!foundSoLuongSachDto.getTrangThai()) {
                        throw new SachNotAvailableException("Sách " + foundSoLuongSachDto.getTenSach() +" hiện không có");
                    }
                    if(details.getSoLuong() > foundSoLuongSachDto.getSoLuong()) {
                        throw new InvalidBodyException("Sách " + foundSoLuongSachDto.getTenSach() + " không đủ hàng tồn kho");
                    }
                    OrderDetails orderDetails1 = CommonMapper.mapToOrderDetails(order, foundSoLuongSachDto, details);
                    orderDetails1.setTongTien(
                                    foundSoLuongSachDto.getDonGia()
                                            .subtract(
                                                    foundSoLuongSachDto.getDonGia()
                                                            .multiply(BigDecimal.valueOf(foundSoLuongSachDto.getPhanTramGiam()))
                                            )
                                            .multiply(BigDecimal.valueOf(foundSoLuongSachDto.getSoLuong()))
                    );
                    System.out.println(orderDetails1.getDonGia());
                    System.out.println(orderDetails1.getSoLuong());
                    System.out.println(orderDetails1.getPhanTramGiam());
                    System.out.println(orderDetails1.getTongTien());
                    orderDetailsList.add(orderDetails1);
                }
            });
            order.setTongTien(orderDetailsList.stream()
                    .reduce(BigDecimal.valueOf(0), (bigDecimal, orderDetails1) -> bigDecimal.add(orderDetails1.getTongTien()), BigDecimal::add));
            System.out.println(order.getTongTien());
            Order savedOrder = orderReponsitory.save(order);
            OrderDto savedOrderDto = CommonMapper.mapToOrderDto(savedOrder);
            savedOrderDto.setOrderDetails(CommonMapper.mapToListOrderDetailDto(savedOrder.getOrderDetails()));
            return savedOrderDto;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Xảy ra lỗi, vui lòng thử lại sau.");
        }
    }

    public Page<OrderDto> getOrders(Long accountId, int page, int pageSize, Sort sort) {
        return orderReponsitory.findAllByNguoiDungId(accountId, PageRequest.of(page, pageSize, sort))
                .map(CommonMapper::mapToOrderDto);
    }

    public Page<OrderDto> getOrders(int page, int pageSize, Sort sort) {
        return orderReponsitory.findAll(PageRequest.of(page, pageSize, sort))
                .map(CommonMapper::mapToOrderDto);
    }

    public Page<OrderDto> getOrdersByStatus(Long accountId, String trangThaiId,int page, int pageSize, Sort sort) {
        return orderReponsitory.findAllByNguoiDungIdAndTrangThai_TrangThaiId(accountId, trangThaiId,
                PageRequest.of(page, pageSize, sort)).map(CommonMapper::mapToOrderDto);
    }

    public Page<OrderDto> getOrdersByStatus(String trangThaiId,int page, int pageSize, Sort sort) {
        return orderReponsitory.findAllByTrangThai_TrangThaiId(trangThaiId,
                PageRequest.of(page, pageSize, sort)).map(CommonMapper::mapToOrderDto);
    }

    public void cancelOrder(Long accountId, Long orderId) {
        var result = orderReponsitory.findByNguoiDungIdAndDonhangId(accountId, orderId);
        if(result.isEmpty()) {
            throw new OrderNotFoundException("Không tìm thấy đơn hàng");
        }
        var foundOrder = result.get();
        if(foundOrder.getTrangThai().getTrangThaiId().compareTo("D") >= 0) {
            throw new CannotCancelOrder("Không thể hủy đơn sau khi đã giao cho bên vận chuyển");
        }
        foundOrder.getTrangThai().setTrangThaiId("G");
        orderReponsitory.save(foundOrder);
    }

    public OrderDto updateOrderStatus(String trangThaiId, Long accountId, Long orderId){
        var result = orderReponsitory.findByNguoiDungIdAndDonhangId(accountId, orderId);
        if(result.isEmpty()) {
            throw new OrderNotFoundException("Không tìm thấy đơn hàng");
        }
        var foundOrder = result.get();
        if(foundOrder.getTrangThai().getTrangThaiId().compareTo(trangThaiId) >= 0) {
            throw new InvalidBodyException("Trạng thái không hợp lệ");
        }
        foundOrder.getTrangThai().setTrangThaiId(trangThaiId);
        return CommonMapper.mapToOrderDto(orderReponsitory.save(foundOrder));
    }
}
