package com.example.bookshop.orderservice.dto;

import lombok.*;

import java.util.List;
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ResponsePayload<T> {
    private Long recordCounts;
    private Integer currentPage;
    private Integer pageSize;
    private Integer totalPages;
    private Integer currentPageSize;
    private List<T> records;
}
