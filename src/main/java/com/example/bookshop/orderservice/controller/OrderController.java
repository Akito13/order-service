package com.example.bookshop.orderservice.controller;

import com.example.bookshop.orderservice.dto.OrderDetailDto;
import com.example.bookshop.orderservice.dto.OrderDto;
import com.example.bookshop.orderservice.dto.ResponseDto;
import com.example.bookshop.orderservice.dto.ResponsePayload;
import com.example.bookshop.orderservice.exception.InvalidBodyException;
import com.example.bookshop.orderservice.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.WebRequest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping(path = "api/order")
public class OrderController {

    private OrderService orderService;

    @Autowired
    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public ResponseEntity<ResponseDto<OrderDetailDto>> createOrder(@Valid @RequestBody OrderDto orderDto,
                                                                   WebRequest request) {
        List<OrderDetailDto> savedOrderDetailDto = orderService.createOrder(orderDto).getOrderDetails();
        ResponsePayload<OrderDetailDto> payload = ResponsePayload.<OrderDetailDto>builder()
                .currentPage(0).currentPageSize(savedOrderDetailDto.size()).pageSize(savedOrderDetailDto.size())
                .recordCounts((long) savedOrderDetailDto.size()).totalPages(1)
                .records(savedOrderDetailDto).build();
        ResponseDto<OrderDetailDto> responseDto = ResponseDto.<OrderDetailDto>builder()
                .apiPath(request.getDescription(false))
                .message("Đã lưu đơn hàng thành công")
                .statusCode(HttpStatus.CREATED)
                .timestamp(LocalDateTime.now())
                .payload(payload).build();
        return new ResponseEntity<>(responseDto, HttpStatus.CREATED);
    }

    @GetMapping("user/{accountId}/{tt}")
    public ResponseEntity<ResponseDto<OrderDto>> getOrders(@PathVariable Long accountId,
                                                           @PathVariable(required = false) Optional<String> tt,
                                                           @RequestParam("page") Optional<Integer> os,
                                                           @RequestParam("pageSize") Optional<Integer> ps,
                                                           @RequestParam("sortBy") Optional<String> sb,
                                                           @RequestParam("direction") Optional<String> d,
                                                           WebRequest request) {
        int page = os.orElse(0);
        int pageSize = ps.orElse(3);
        String direction = d.orElse("desc");
        String sortBy = sb.orElse("id");
        Page<OrderDto> orderDtoPage = null;
        Sort sort = generateSortPolicy(sortBy, direction);
        if(tt.isPresent()) {
            orderDtoPage = orderService.getOrdersByStatus(accountId, tt.get(), page, pageSize, sort);
        } else {
            orderDtoPage = orderService.getOrders(accountId, page, pageSize, sort);
        }
        return getResponseDtoResponseEntity(request, orderDtoPage);
    }

    @DeleteMapping("user/{accountId}/{donHangId}")
    public ResponseEntity<ResponseDto<OrderDto>> cancelOrder(@PathVariable Long accountId,
                                                             @PathVariable Long donHangId,
                                                             WebRequest request) {
        orderService.cancelOrder(accountId, donHangId);
//        Refund logic here if possible (PAYPAL)
        return getResponseDtoResponseEntity(request, null);
    }

    @GetMapping("admin/{accountId}/{tt}")
    public ResponseEntity<ResponseDto<OrderDto>> getAllOrders(@PathVariable(required = false) Optional<Long> accountId,
                                                           @PathVariable(required = false) Optional<String> tt,
                                                           @RequestParam("page") Optional<Integer> os,
                                                           @RequestParam("pageSize") Optional<Integer> ps,
                                                           @RequestParam("sortBy") Optional<String> sb,
                                                           @RequestParam("direction") Optional<String> d,
                                                           WebRequest request) {
        if(accountId.isPresent()) {
            return getOrders(accountId.get(), tt, os, ps, sb, d, request);
        }
        int page = os.orElse(0);
        int pageSize = ps.orElse(3);
        String direction = d.orElse("desc");
        String sortBy = sb.orElse("id");
        Page<OrderDto> orderDtoPage = null;
        Sort sort = generateSortPolicy(sortBy, direction);
        if(tt.isPresent()) {
            orderDtoPage = orderService.getOrdersByStatus(tt.get(), page, pageSize, sort);
        } else {
            orderDtoPage = orderService.getOrders(page, pageSize, sort);
        }
        return getResponseDtoResponseEntity(request, orderDtoPage);
    }

    @PutMapping("admin/{accountId}/{orderId}")
    public ResponseEntity<ResponseDto<OrderDto>> updateOrderStatus(@RequestParam Optional<String> trangThaiId,
                                                                   @PathVariable Optional<Long> accountId,
                                                                   @PathVariable Optional<Long> orderId,
                                                                   WebRequest request){
        if(trangThaiId.isEmpty() || accountId.isEmpty() || orderId.isEmpty()) {
            throw new InvalidBodyException("Thiếu trạng thái hoặc mã tài khoản hoặc mã đơn hàng");
        }
        var updatedOrder = orderService.updateOrderStatus(trangThaiId.get(), accountId.get(), orderId.get());
        ResponsePayload<OrderDto> payload = ResponsePayload.<OrderDto>builder()
                .records(List.of(updatedOrder))
                .recordCounts(1L)
                .currentPage(0)
                .currentPageSize(1)
                .totalPages(1)
                .pageSize(1).build();
        return generateResponseEntity(request, "Request processed successfully", HttpStatus.OK, payload);
    }

    private ResponseEntity<ResponseDto<OrderDto>> getResponseDtoResponseEntity(WebRequest request, Page<OrderDto> orderDtoPage) {
        ResponsePayload<OrderDto> payload = orderDtoPage == null ? null : ResponsePayload.<OrderDto>builder()
                .records(orderDtoPage.getContent())
                .recordCounts(orderDtoPage.getTotalElements())
                .currentPage(orderDtoPage.getNumber())
                .currentPageSize(orderDtoPage.getNumberOfElements())
                .totalPages(orderDtoPage.getTotalPages())
                .pageSize(orderDtoPage.getSize()).build();
        return generateResponseEntity(request, "Request processed successfully", HttpStatus.OK, payload);
    }

    public <T> ResponseEntity<ResponseDto<T>> generateResponseEntity(WebRequest request, String message, HttpStatus status, ResponsePayload<T> payload) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ResponseDto.<T>builder()
                        .apiPath(request.getDescription(false))
                        .message(message)
                        .timestamp(LocalDateTime.now())
                        .statusCode(status)
                        .payload(payload)
                        .build()
                );
    }

    private Sort generateSortPolicy(String sortBy, String directionString) {
        directionString = directionString.equals("desc") ? "desc" : "asc";
        Sort.Direction direction = Sort.Direction.fromString(directionString);
        switch (sortBy) {
            case "tt":
                return Sort.by(direction, "trangThai");
            default:
                return Sort.by(direction, "donhangId");
        }
    }
}
