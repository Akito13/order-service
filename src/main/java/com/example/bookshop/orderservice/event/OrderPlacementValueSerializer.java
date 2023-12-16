package com.example.bookshop.orderservice.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Serializer;

import java.util.List;
import java.util.Map;

public class OrderPlacementValueSerializer
        implements Serializer<Object>
        {
    private final ObjectMapper objectMapper = new ObjectMapper();
    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {
        Serializer.super.configure(configs, isKey);
    }

    @Override
    public byte[] serialize(String s, Object orderPlacementEvent) {
        try {
            if(orderPlacementEvent == null) {
                return null;
            }
            objectMapper.registerModule(new JavaTimeModule());
            System.out.println("Serializing...");
            return objectMapper.writeValueAsBytes(orderPlacementEvent);
        } catch (Exception e) {
            e.printStackTrace();
            throw new SerializationException("Error when serializing order to byte array", e);
        }
    }

    @Override
    public void close() {
        Serializer.super.close();
    }
}
