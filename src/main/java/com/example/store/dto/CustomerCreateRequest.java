package com.example.store.dto;

import jakarta.validation.constraints.NotBlank;

import lombok.Data;

@Data
public class CustomerCreateRequest {
    @NotBlank
    private String name;
}
