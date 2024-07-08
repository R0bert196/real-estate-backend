package com.cleancode.real_estate_backend.dtos.administrator.building;

import java.util.List;

public record BuildingResponseDTO(String name,
                                  List<FloorResponseDTO> floors,
                                  Double squareMeter,
                                  Long id) {
}
