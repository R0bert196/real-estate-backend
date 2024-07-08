package com.cleancode.real_estate_backend.dtos.administrator.tenants.request;

import java.util.List;

public record TenantSelectedBuildingsRequestDTO(Long selectedBuildingId,
                                                List<TenantSelectedFloorsRequestDTO> selectedFloors,
                                                Double squareMeterPrice,
                                                Double maintenanceSquareMeterPrice) {
}
