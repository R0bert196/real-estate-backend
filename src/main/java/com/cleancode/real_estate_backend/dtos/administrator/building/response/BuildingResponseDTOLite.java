package com.cleancode.real_estate_backend.dtos.administrator.building.response;

public record BuildingResponseDTOLite(String name,
                                      Integer floors,
                                      Double squareMeter,
                                      Long id) {

    // Additional constructor that only takes name and id
    public BuildingResponseDTOLite(String name, Long id) {
        this(name, null, null, id);
    }
}
