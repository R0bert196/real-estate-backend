package com.cleancode.real_estate_backend.dtos.administrator.tenants;

import com.cleancode.real_estate_backend.dtos.administrator.building.BuildingResponseDTOLite;

import java.util.List;

public record TenantResponseDTO(String name,
                                Double squareMeterPrice,
                                Double maintenanceSquareMeterPrice,
                                List<BuildingResponseDTOLite> buildings) {
}
