package com.example.bookshop.orderservice.event;

import com.example.bookshop.orderservice.CommonConstants;
import com.example.bookshop.orderservice.model.Order;
import org.apache.kafka.common.serialization.BytesSerializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class KafkaProducer {
//    private KafkaTemplate<String, Map<String, List<OrderPlacementEvent>>> kafkaTemplate;

    private KafkaTemplate<String, Object> kafkaTemplate;
    @Autowired
    public KafkaProducer(KafkaTemplate<String, Object> kafkaTemplate){
        this.kafkaTemplate = kafkaTemplate;
    }

    public void notifyOrderPlacement(String email, Order order) {
        Map<String, List<OrderPlacementEvent>> orderData = new HashMap<>();
        System.out.println(order.getNguoiDungId()+":"+order.getThoiGianDat()+":"+order.getThoiGianXuat()+":"+order.getDonhangId()+":"+order.getTrangThai().getTenTrangThai());
        List<OrderPlacementEvent> orderDetailData = order.getOrderDetails().stream()
                        .map(orderDetailDto -> OrderPlacementEvent.builder()
                                .phanTramGiam(orderDetailDto.getPhanTramGiam())
                                .nguoiDungId(order.getNguoiDungId())
                                .thoiGianDat(order.getThoiGianDat())
                                .thoiGianXuat(order.getThoiGianXuat())
                                .donhangId(order.getDonhangId())
                                .dhctId(orderDetailDto.getDhctId())
                                .trangThai(order.getTrangThai().getTenTrangThai())
                                .tenSach(orderDetailDto.getTenSach())
                                .sachId(orderDetailDto.getSachId())
                                .soLuong(orderDetailDto.getSoLuong())
                                .donGia(orderDetailDto.getDonGia())
                                .hoanTra(order.getHoanTra())
                                .tongTien(orderDetailDto.getTongTien()).build())
                        .toList();
        orderData.put(email, orderDetailData);
        kafkaTemplate.send(CommonConstants.KAFKA_TOPIC_ORDER_PLACEMENT, orderData);
        System.out.println("notifyOrderPlacement() sent orderData");
        System.out.println(orderData);
    }
    public void notifyOrderStatusChange(String email, Order order) {
        Map<String, List<OrderStatusChangeEvent>> orderData = new HashMap<>();
        List<OrderStatusChangeEvent> orderDetailData = order.getOrderDetails().stream()
                .map(orderDetailDto -> OrderStatusChangeEvent.builder()
                        .donhangId(order.getDonhangId())
                        .trangThai(order.getTrangThai().getTenTrangThai())
                        .hoanTra(order.getHoanTra())
                        .build())
                .toList();
        orderData.put(email, orderDetailData);
        kafkaTemplate.send(CommonConstants.KAFKA_TOPIC_ORDER_STATUS_CHANGED, orderData);
    }
}
