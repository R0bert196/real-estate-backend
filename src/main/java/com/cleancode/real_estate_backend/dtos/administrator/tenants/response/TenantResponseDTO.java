package com.cleancode.real_estate_backend.dtos.administrator.tenants.response;

import com.cleancode.real_estate_backend.dtos.administrator.building.response.BuildingResponseDTOLite;

import java.util.List;

public record TenantResponseDTO(Long id,
                                String name,
                                List<RentedFloorResponseDTO> rentedFloorResponseDTOS) {
}
