package com.cleancode.real_estate_backend.dtos.manager.tenants.response;

public record TenantResponseDTOView(
        Long id,
        String name,
        Double totalRentedSize,
        Double averageSquareMeterPrice,
        Double averageMaintenanceSquareMeterPrice,
        Integer buildingsCount

) {
}
