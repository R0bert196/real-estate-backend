package com.cleancode.real_estate_backend.dtos.administrator.building.request;

public record FloorRequestDTO(
        String size,
        String number,
        Long id
) {
}
