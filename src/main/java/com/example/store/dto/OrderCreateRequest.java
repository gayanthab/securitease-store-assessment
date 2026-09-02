package com.example.store.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import lombok.Data;

import java.util.List;

@Data
public class OrderCreateRequest {
    @NotBlank
    private String description;

    @NotNull private Long customerId;

    @NotEmpty
    private List<Long> productIds;
}
