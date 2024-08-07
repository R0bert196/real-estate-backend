package com.cleancode.real_estate_backend.dtos.manager.tenants.response;

import java.util.List;

public record TenantResponseDTO(Long id,
                                String name,
                                List<RentedFloorResponseDTO> rentedFloorResponseDTOS) {
}
