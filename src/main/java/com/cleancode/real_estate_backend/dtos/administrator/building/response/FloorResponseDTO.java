package com.cleancode.real_estate_backend.dtos.administrator.building.response;

public record FloorResponseDTO(Double size,
                               Integer floorNumber,
                               Long id,
                               BuildingResponseDTOLite buildingResponseDTOLite) {
}
