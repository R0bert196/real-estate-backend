package com.cleancode.real_estate_backend.dtos.administrator.tenants;

import com.cleancode.real_estate_backend.dtos.administrator.building.BuildingResponseDTO;
import com.cleancode.real_estate_backend.entities.Building;

import java.util.List;
import java.util.Set;

public record TenantResponseDTO(String name,
                                Double squareMeterPrice,
                                Double maintenanceSquareMeterPrice,
                                List<BuildingResponseDTO> buildings) {
}
