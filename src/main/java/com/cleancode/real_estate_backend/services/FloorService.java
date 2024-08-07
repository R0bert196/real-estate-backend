package com.cleancode.real_estate_backend.services;

import com.cleancode.real_estate_backend.dtos.manager.building.response.BuildingResponseDTOLite;
import com.cleancode.real_estate_backend.dtos.manager.building.response.FloorResponseDTO;
import com.cleancode.real_estate_backend.dtos.manager.building.response.FloorResponseDTOLite;
import com.cleancode.real_estate_backend.entities.Building;
import com.cleancode.real_estate_backend.entities.Floor;
import com.cleancode.real_estate_backend.repositories.FloorRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

@Service
@Log4j2
@RequiredArgsConstructor
public class FloorService {

    private final FloorRepository floorRepository;

    public FloorResponseDTOLite convertToDTOLite(Floor entity) {
        log.info("Converting Floor entity to FloorResponseDTOLite: Floor ID = {}", entity.getId());

        FloorResponseDTOLite dtoLite = new FloorResponseDTOLite(
                entity.getSize(),
                entity.getAvailableSize(),
                entity.getFloorNumber(),
                entity.getId()
        );

        log.info("Converted FloorResponseDTOLite: {}", dtoLite);
        return dtoLite;
    }

    public FloorResponseDTO convertToDTO(Floor entity) {
        log.info("Converting Floor entity to FloorResponseDTO: Floor ID = {}", entity.getId());

        Building building = entity.getBuilding();

        BuildingResponseDTOLite buildingResponseDTOLite = new BuildingResponseDTOLite(
                building.getName(),
                null,
                null,
                building.getId()
        );

        FloorResponseDTO dto = new FloorResponseDTO(
                entity.getSize(),
                entity.getFloorNumber(),
                entity.getId(),
                entity.getAvailableSize(),
                buildingResponseDTOLite
        );

        log.info("Converted FloorResponseDTO: {}", dto);
        return dto;
    }
}
