package com.example.bookshop.orderservice.controller;

import com.example.bookshop.orderservice.dto.*;
import com.example.bookshop.orderservice.exception.InvalidBodyException;
import com.example.bookshop.orderservice.model.OrderStatus;
import com.example.bookshop.orderservice.service.OrderService;
import com.stripe.exception.StripeException;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
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

    @PostMapping("new")
//    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Long> createOrder(@RequestBody OrderWithPaymentDto orderWithPaymentDto, Authentication auth) throws StripeException {
        System.out.println(auth);
        return ResponseEntity.ok(orderService.createOrder(orderWithPaymentDto.getOrder(), orderWithPaymentDto.getPaymentId(),auth));
    }

    @GetMapping("status")
    public ResponseEntity<ResponseDto<OrderStatus>> getStatuses(WebRequest request){
        List<OrderStatus> statuses = orderService.getOrderStatuses();
        ResponsePayload<OrderStatus> payload = ResponsePayload.<OrderStatus>builder()
                .currentPage(0).currentPageSize(statuses.size()).pageSize(statuses.size())
                .recordCounts((long) statuses.size()).totalPages(1)
                .records(statuses).build();
        ResponseDto<OrderStatus> response = ResponseDto.<OrderStatus>builder()
                .apiPath(request.getDescription(false))
                .message("OK")
                .statusCode(HttpStatus.OK)
                .timestamp(LocalDateTime.now())
                .payload(payload).build();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("user/{accountId}")
//    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ResponseDto<OrderDto>> getOrders(@PathVariable Long accountId,
                                                           @RequestParam("trangThai") Optional<String> tt,
                                                           @RequestParam("page") Optional<Integer> os,
                                                           @RequestParam("pageSize") Optional<Integer> ps,
                                                           @RequestParam("sortBy") Optional<String> sb,
                                                           @RequestParam("direction") Optional<String> d,
                                                           WebRequest request) {
        int page = os.orElse(0);
        int pageSize = ps.orElse(5);
        String direction = d.orElse("asc");
        String sortBy = sb.orElse("id");
//        String trangThaiId = tt.orElse("A");
        Page<OrderDto> orderDtoPage = null;
        Sort sort = generateSortPolicy(sortBy, direction);
        if(tt.isPresent()) {
            orderDtoPage = orderService.getOrdersByStatus(accountId, tt.get(), page, pageSize, sort);
        } else {
            orderDtoPage = orderService.getOrders(accountId, page, pageSize, sort);
        }
        return getResponseDtoResponseEntity(request, orderDtoPage);
    }

    @GetMapping("user/{accountId}/{orderId}")
//    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ResponseDto<OrderDto>> getOrder(@PathVariable Long accountId,
                                                          @PathVariable Long orderId,
                                                          Authentication auth,
                                                          WebRequest request) {
        OrderDto orderDto = orderService.getOrder(accountId, orderId);
        ResponsePayload<OrderDto> payload = ResponsePayload.<OrderDto>builder()
                .currentPage(0).totalPages(1).currentPageSize(1).pageSize(1).recordCounts((long)orderDto.getOrderDetails().size())
                .records(List.of(orderDto))
                .build();
        return generateResponseEntity(request, "OK", HttpStatus.OK, payload);
    }

    @DeleteMapping("user/{accountId}/{orderId}")
//    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ResponseDto<OrderDto>> cancelOrder(@PathVariable Long accountId,
                                                             @PathVariable Long orderId,
                                                             WebRequest request,
                                                             Authentication auth) throws StripeException {
        orderService.cancelOrder(accountId, orderId, auth);
        return getResponseDtoResponseEntity(request, null);
    }

    @GetMapping("admin")
    public ResponseEntity<ResponseDto<OrderDto>> getAllOrders(@RequestParam(required = false) Optional<Long> accountId,
                                                           @RequestParam("trangThai") Optional<String> tt,
                                                           @RequestParam("tenNguoiNhan") Optional<String> tnn,
                                                           @RequestParam("page") Optional<Integer> os,
                                                           @RequestParam("pageSize") Optional<Integer> ps,
                                                           @RequestParam("sortBy") Optional<String> sb,
                                                           @RequestParam("direction") Optional<String> d,
                                                           WebRequest request) {
        if(accountId.isPresent()) {
            return getOrders(accountId.get(), tt, os, ps, sb, d, request);
        }

        int page = os.orElse(0);
        int pageSize = ps.orElse(6);
        String direction = d.orElse("desc");
        String sortBy = sb.orElse("id");
        String tenNguoiNhan = tnn.orElse("");
        Page<OrderDto> orderDtoPage = null;
        Sort sort = generateSortPolicy(sortBy, direction);
        if(tt.isPresent()) {
            orderDtoPage = orderService.getOrdersByStatusAndTen(tt.get(), "%" + tenNguoiNhan +"%", page, pageSize, sort);
        } else {
            System.out.println("Found");
            orderDtoPage = orderService.getOrdersByTen("%" + tenNguoiNhan +"%", page, pageSize, sort);
        }
        return getResponseDtoResponseEntity(request, orderDtoPage);
    }

    @GetMapping("admin/{orderId}")
    public ResponseEntity<ResponseDto<OrderDto>> getOrderAdmin(@PathVariable Long orderId,
                                                               WebRequest request) {
        OrderDto orderDto = orderService.getOrderAdmin(orderId);
        ResponsePayload<OrderDto> payload = ResponsePayload.<OrderDto>builder()
                .currentPage(0).totalPages(1).currentPageSize(1).pageSize(1).recordCounts((long)orderDto.getOrderDetails().size())
                .records(List.of(orderDto))
                .build();
        return generateResponseEntity(request, "OK", HttpStatus.OK, payload);
    }

    @PutMapping("admin/{orderId}")
    public ResponseEntity<ResponseDto<OrderDto>> updateOrderStatus(@RequestParam Optional<String> trangThaiId,
                                                                   @PathVariable Optional<Long> orderId,
                                                                   Authentication auth,
                                                                   WebRequest request){
        if(trangThaiId.isEmpty() || orderId.isEmpty()) {
            throw new InvalidBodyException("Thiếu trạng thái hoặc mã tài khoản hoặc mã đơn hàng");
        }
        var updatedOrder = orderService.updateOrderStatus(trangThaiId.get(), orderId.get(), auth);
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
        return switch (sortBy) {
            case "tt" -> Sort.by(direction, "trangThai_trangThaiId");
            case "tong" -> Sort.by(direction, "tongTien");
            default -> Sort.by(direction, "donhangId");
        };
    }
}
