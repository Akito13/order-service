package com.example.bookshop.orderservice;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class CommonConstants {
    public static String KAFKA_TOPIC_ORDER_PLACEMENT;
    public static String KAFKA_TOPIC_ORDER_STATUS_CHANGED;
    public static String JWT_KEY;

    @Value("${constant.kafka.order-status-changed}")
    public void setKafkaTopicOrderStatusChanged(String topic) {
        CommonConstants.KAFKA_TOPIC_ORDER_STATUS_CHANGED = topic;
    }

    @Value("${constant.kafka.order-placed}")
    public void setKafkaTopicOrderPlacement(String topic) {
        CommonConstants.KAFKA_TOPIC_ORDER_PLACEMENT = topic;
    }

    @Value("${constant.security.JWT_KEY}")
    public void setJwtKey(String key) {
        CommonConstants.JWT_KEY = key;
    }
}
