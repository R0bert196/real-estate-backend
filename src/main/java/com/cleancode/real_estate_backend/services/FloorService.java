package com.cleancode.real_estate_backend.services;

import com.cleancode.real_estate_backend.dtos.administrator.building.response.BuildingResponseDTOLite;
import com.cleancode.real_estate_backend.dtos.administrator.building.response.FloorResponseDTO;
import com.cleancode.real_estate_backend.dtos.administrator.building.response.FloorResponseDTOLite;
import com.cleancode.real_estate_backend.entities.Building;
import com.cleancode.real_estate_backend.entities.Floor;
import com.cleancode.real_estate_backend.repositories.FloorRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class FloorService {

    private final FloorRepository floorRepository;


    public FloorResponseDTOLite convertToDTOLite(Floor entity) {
        return new FloorResponseDTOLite(entity.getSize(), entity.getAvailableSize(), entity.getFloorNumber(), entity.getId());
    }

    public FloorResponseDTO convertToDTO(Floor entity) {

        Building building = entity.getBuilding();

        BuildingResponseDTOLite buildingResponseDTOLite = new BuildingResponseDTOLite(
                building.getName(),
                null,
                null,
                building.getId());

        return new FloorResponseDTO(
                entity.getSize(),
                entity.getFloorNumber(),
                entity.getId(),
                buildingResponseDTOLite

        );


    }
}
