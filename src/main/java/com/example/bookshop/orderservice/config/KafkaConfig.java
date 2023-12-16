package com.example.bookshop.orderservice.config;

import com.example.bookshop.orderservice.CommonConstants;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {
    @Bean
    public NewTopic newOrderTopic() {
        return TopicBuilder.name(CommonConstants.KAFKA_TOPIC_ORDER_PLACEMENT).build();
    }

    @Bean
    public NewTopic orderStatusChangedTopic() {
        return TopicBuilder.name(CommonConstants.KAFKA_TOPIC_ORDER_STATUS_CHANGED).build();
    }
}
