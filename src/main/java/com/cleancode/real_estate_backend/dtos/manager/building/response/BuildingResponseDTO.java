package com.cleancode.real_estate_backend.dtos.manager.building.response;

import java.util.List;

public record BuildingResponseDTO(String name,
                                  List<FloorResponseDTOLite> floors,
                                  Double squareMeter,
                                  String address,
                                  Long id) {
}
