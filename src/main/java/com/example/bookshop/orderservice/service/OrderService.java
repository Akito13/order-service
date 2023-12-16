package com.example.bookshop.orderservice.service;

import com.example.bookshop.orderservice.dto.OrderDetailDto;
import com.example.bookshop.orderservice.dto.OrderDto;
import com.example.bookshop.orderservice.dto.SoLuongSachDto;
import com.example.bookshop.orderservice.event.KafkaProducer;
import com.example.bookshop.orderservice.exception.CannotCancelOrder;
import com.example.bookshop.orderservice.exception.InvalidBodyException;
import com.example.bookshop.orderservice.exception.OrderNotFoundException;
import com.example.bookshop.orderservice.exception.SachNotAvailableException;
import com.example.bookshop.orderservice.mapper.CommonMapper;
import com.example.bookshop.orderservice.model.Order;
import com.example.bookshop.orderservice.model.OrderDetails;
import com.example.bookshop.orderservice.model.OrderStatus;
import com.example.bookshop.orderservice.repository.OrderReponsitory;
import com.example.bookshop.orderservice.repository.OrderStatusRepository;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
public class OrderService {

    private OrderReponsitory orderReponsitory;
    private final WebClient.Builder webClientBuilder;
    private final KafkaProducer kafkaProducer;
    private final OrderStatusRepository statusRepository;
    private final PaymentService paymentService;

    @Autowired
    public OrderService(OrderReponsitory orderReponsitory,
                        WebClient.Builder webClientBuilder,
                        KafkaProducer kafkaProducer,
                        OrderStatusRepository statusRepository,
                        PaymentService paymentService) {
        this.orderReponsitory = orderReponsitory;
        this.webClientBuilder = webClientBuilder;
        this.kafkaProducer = kafkaProducer;
        this.statusRepository = statusRepository;
        this.paymentService = paymentService;
    }

    @Transactional(rollbackFor = {Exception.class, RuntimeException.class})
    public Long createOrder(@Valid OrderDto orderDto, String paymentId, Authentication auth) throws StripeException {
        System.out.println("Id thanh toán: " + paymentId);
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
        PaymentIntent paymentIntent = null;
        try {
            OrderStatus newStatus = statusRepository.findById("A").get();
            Order order = CommonMapper.mapToOrder(newStatus, orderDto);
            order.setThoiGianDat(order.getThoiGianDat().plusHours(7L));
            List<OrderDetails> orderDetailsList = new ArrayList<>();
            orderDetails.forEach(details -> {
                int index = Arrays.binarySearch(soLuongSachDtos, details.getSachId());
                if(index >= 0) {
                    SoLuongSachDto foundSoLuongSachDto = soLuongSachDtos[index];
                    if (!foundSoLuongSachDto.getTrangThai()) {
                        throw new SachNotAvailableException("Sách hiện không có");
                    }
                    if(details.getSoLuong() > foundSoLuongSachDto.getSoLuong()) {
                        throw new InvalidBodyException("Sách tạm hết hàng");
                    }
                    OrderDetails orderDetails1 = CommonMapper.mapToOrderDetails(order, foundSoLuongSachDto, details);
                    System.out.println("----------------- Sách " + details.getTenSach() + " -----------------");
                    System.out.println(orderDetails1.getDonGia());
                    System.out.println(orderDetails1.getSoLuong());
                    System.out.println(orderDetails1.getPhanTramGiam());
                    System.out.println(orderDetails1.getTongTien());
                    System.out.println("---------------------------------------------------");
                    orderDetailsList.add(orderDetails1);
                }
            });
            System.out.println(order.getDiaChi());
            System.out.println(order.getSdt());
            System.out.println(order.getNguoiDungId());
            System.out.println(order.getTenNguoiNhan());
            System.out.println(order.getTrangThai().getTrangThaiId());
            order.setTongTien(orderDetailsList.stream()
                    .reduce(BigDecimal.valueOf(0), (bigDecimal, orderDetails1) -> bigDecimal.add(orderDetails1.getTongTien()), BigDecimal::add));
            order.setOrderDetails(orderDetailsList);
            paymentIntent = paymentService.createPaymentIntent(order.getTongTien().longValue(), paymentId, auth.getName());
            order.setThanhToanId(paymentIntent.getId());
            order.setHoanTra(false);

            System.out.println("============= Tổng thanh toán: " + order.getTongTien() + " =============");

            Order savedOrder = orderReponsitory.save(order);
            kafkaProducer.notifyOrderPlacement(auth.getName(), savedOrder);

//            OrderDto savedOrderDto = CommonMapper.mapToOrderDto(savedOrder);
//            savedOrderDto.setOrderDetails(CommonMapper.mapToListOrderDetailDto(savedOrder.getOrderDetails()));
//            return 1L;
            return savedOrder.getDonhangId();
        } catch (Exception e) {
            e.printStackTrace();
            if(paymentIntent != null) {
                paymentIntent.cancel();
            }
            throw new RuntimeException(e);
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

    public Page<OrderDto> getOrdersByTen(String tenNguoiNhan, int page, int pageSize, Sort sort) {
        return orderReponsitory.findAllByTenNguoiNhanLikeIgnoreCase(tenNguoiNhan, PageRequest.of(page, pageSize, sort))
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

    public Page<OrderDto> getOrdersByStatusAndTen(String trangThaiId, String tenNguoiNhan, int page, int pageSize, Sort sort) {
        return orderReponsitory.findAllByTrangThai_TrangThaiIdAndTenNguoiNhanLikeIgnoreCase(trangThaiId, tenNguoiNhan,
                PageRequest.of(page, pageSize, sort)).map(CommonMapper::mapToOrderDto);
    }

    public OrderDto getOrder(Long accountId, Long orderId) {
        Optional<Order> result =orderReponsitory.findById(orderId);
        if(result.isEmpty()) {
            throw new OrderNotFoundException("Không tìm thấy đơn hàng");
        }
        Order order = result.get();
        if(!order.getNguoiDungId().equals(accountId)) {
            throw new InvalidBodyException("Không có quyền xem");
        }
        OrderDto orderDto = CommonMapper.mapToOrderDto(order);
        orderDto.setOrderDetails(order.getOrderDetails().stream()
                .map(CommonMapper::mapToOrderDetailDto).toList());
        return orderDto;
    }

    public OrderDto getOrderAdmin(Long orderId) {
        Optional<Order> result =orderReponsitory.findById(orderId);
        if(result.isEmpty()) {
            throw new OrderNotFoundException("Không tìm thấy đơn hàng");
        }
        Order order = result.get();
        OrderDto orderDto = CommonMapper.mapToOrderDto(order);
        orderDto.setOrderDetails(order.getOrderDetails().stream()
                .map(CommonMapper::mapToOrderDetailDto).toList());
        return orderDto;
    }

    public List<OrderStatus> getOrderStatuses() {
        return statusRepository.findAll();
    }

    public void cancelOrder(Long accountId, Long orderId, Authentication auth) throws StripeException {
        var result = orderReponsitory.findByNguoiDungIdAndDonhangId(accountId, orderId);
        var cancelStatusResult = statusRepository.findById("G");
        if(result.isEmpty() || cancelStatusResult.isEmpty()) {
            throw new OrderNotFoundException("Không tìm thấy đơn hàng");
        }
        var foundOrder = result.get();
        if(foundOrder.getTrangThai().getTrangThaiId().compareTo("D") >= 0) {
            throw new CannotCancelOrder("Không thể hủy đơn sau khi đã giao cho bên vận chuyển");
        }
        paymentService.refundPaymentIntent(foundOrder.getThanhToanId());
        foundOrder.setTrangThai(cancelStatusResult.get());
        foundOrder.setHoanTra(true);
        Order savedOrder = orderReponsitory.save(foundOrder);
        kafkaProducer.notifyOrderStatusChange(auth.getName(), savedOrder);
    }

    public OrderDto updateOrderStatus(String trangThaiId, Long orderId, Authentication auth){
        var result = orderReponsitory.findById(orderId);
        var ttResult = statusRepository.findById(trangThaiId);
        if(result.isEmpty() || ttResult.isEmpty()) {
            throw new OrderNotFoundException("Không tìm thấy đơn hàng");
        }
        var foundOrder = result.get();
        if(foundOrder.getTrangThai().getTrangThaiId().compareTo(trangThaiId) >= 0) {
            throw new InvalidBodyException("Trạng thái không hợp lệ");
        }
        if(foundOrder.getTrangThai().getTrangThaiId().compareTo("D") < 0 && trangThaiId.compareTo("D") >= 0) {
            foundOrder.setThoiGianXuat(LocalDateTime.now());
        }
        foundOrder.setTrangThai(ttResult.get());

        Order savedOrder = orderReponsitory.save(foundOrder);
        kafkaProducer.notifyOrderStatusChange(auth.getName(), savedOrder);
        System.out.println(savedOrder.getTrangThai().getTenTrangThai());
        return CommonMapper.mapToOrderDto(savedOrder);
//        return CommonMapper.mapToOrderDto(foundOrder);
    }
}
