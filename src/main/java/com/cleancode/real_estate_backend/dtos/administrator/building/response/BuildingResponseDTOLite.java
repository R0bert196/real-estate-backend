package com.cleancode.real_estate_backend.dtos.administrator.building.response;

public record BuildingResponseDTOLite(String name,
                                      Integer floors,
                                      Double squareMeter,
                                      Long id) {
}
