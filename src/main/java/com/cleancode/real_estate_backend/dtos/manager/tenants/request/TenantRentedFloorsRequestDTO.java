package com.cleancode.real_estate_backend.dtos.manager.tenants.request;

public record TenantRentedFloorsRequestDTO(Long selectedFloorId, // the id of the floor that was selected
                                           Double rentedSize,
                                           Double squareMeterPrice,
                                           Double maintenanceSquareMeterPrice,
                                           Long selectedBuildingId
) {
}
