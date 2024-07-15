package com.cleancode.real_estate_backend.dtos.administrator.tenants.response;

import com.cleancode.real_estate_backend.dtos.administrator.building.response.FloorResponseDTO;

public record RentedFloorResponseDTO(Long id,
                                     Double rentedSize,
                                     Double squareMeterPrice,
                                     Double maintenanceSquareMeterPrice,
                                     FloorResponseDTO floorResponseDTO) {
}
