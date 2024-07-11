package com.cleancode.real_estate_backend.dtos.administrator.tenants.request;

public record TenantSelectedFloorsRequestDTO(Integer floorNumber,
                                             Integer size,
                                             Double selectedSize,
                                             Long id) {
}
