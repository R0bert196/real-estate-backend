package com.cleancode.real_estate_backend.dtos.manager.tenants.request;

import java.util.List;

public record TenantRequestDTO(String tenantName,
                               List<TenantRentedFloorsRequestDTO> rentedFloors) {
}
