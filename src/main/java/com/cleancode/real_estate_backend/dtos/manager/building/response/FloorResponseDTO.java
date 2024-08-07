package com.cleancode.real_estate_backend.dtos.manager.building.response;

public record FloorResponseDTO(Double size,
                               Integer number,
                               Long id,
                               Double availableSize,
                               BuildingResponseDTOLite buildingResponseDTOLite) {
}
