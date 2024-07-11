package com.cleancode.real_estate_backend.dtos.administrator.building.response;

public record FloorResponseDTOLite(
        Double size,
        Double availableSize,
        Integer number,
        Long id
) {
}
