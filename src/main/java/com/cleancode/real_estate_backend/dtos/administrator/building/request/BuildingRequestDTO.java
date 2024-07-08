package com.cleancode.real_estate_backend.dtos.administrator.building.request;

import com.cleancode.real_estate_backend.dtos.administrator.building.request.FloorRequestDTO;

import java.util.Set;

public record BuildingRequestDTO(
        String buildingName,
        String address,
        Set<FloorRequestDTO> floors
) {
}
