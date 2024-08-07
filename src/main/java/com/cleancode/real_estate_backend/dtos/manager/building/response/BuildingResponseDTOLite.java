package com.cleancode.real_estate_backend.dtos.manager.building.response;

public record BuildingResponseDTOLite(String name,
                                      Integer floors,
                                      Double squareMeter,
                                      Long id) {

    // Additional constructor that only takes name and selectedFloorId
    public BuildingResponseDTOLite(String name, Long id) {
        this(name, null, null, id);
    }
}
