package com.cleancode.real_estate_backend.dtos.administrator.building;

import java.util.Set;

public record BuildingRequestDTO(
        String buildingName,
        String address,
        Set<FloorRequestDTO> floors
) {
}
