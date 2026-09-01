package com.example.store.dto;

import lombok.Data;

import java.util.List;

@Data
public class OrderCreateRequest {
    private String description;
    private Long customerId;
    private List<Long> productIds;
}
