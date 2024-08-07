package com.cleancode.real_estate_backend.dtos.manager.building.request;

import java.util.Set;

public record BuildingRequestDTO(
        String buildingName,
        String address,
        Set<FloorRequestDTO> floors
) {
}
