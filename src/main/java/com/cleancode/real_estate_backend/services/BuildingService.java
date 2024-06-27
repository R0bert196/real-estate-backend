package com.cleancode.real_estate_backend.services;

import com.cleancode.real_estate_backend.dtos.administrator.building.BuildingRequestDTO;
import com.cleancode.real_estate_backend.dtos.administrator.building.BuildingResponseDTO;
import com.cleancode.real_estate_backend.dtos.administrator.building.FloorRequestDTO;
import com.cleancode.real_estate_backend.entities.Building;
import com.cleancode.real_estate_backend.entities.Floor;
import com.cleancode.real_estate_backend.repositories.BuildingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class BuildingService {

    private final BuildingRepository buildingRepository;

    public BuildingResponseDTO addBuilding(BuildingRequestDTO buildingRequestDTO) {

        try {

            Building building = convertToEntity(buildingRequestDTO);

            Building savedBuilding = buildingRepository.save(building);
            Set<Floor> savedFloors = savedBuilding.getFloors();

            return new BuildingResponseDTO(building.getName(), savedFloors.size(), savedFloors.stream().mapToDouble(Floor::getSquareMeter).sum());
        } catch (IllegalArgumentException e) {

            log.error("Error while adding building: {}", e.getMessage());

            throw new IllegalArgumentException("Error while adding building", e);
        }
    }

    private Building convertToEntity(BuildingRequestDTO dto) {

        Building building = Building.builder()
                .name(dto.buildingName())
                .address(dto.address())
                .build();
        Set<Floor> floors =  dto.floors().stream().map(this::convertToEntity).collect(Collectors.toSet());

        // set floors to building
        building.setFloors(floors);

        //set building to floors
        floors.forEach(floor -> floor.setBuilding(building));

        return building;
    }

    public BuildingResponseDTO convertToDTO(Building entity) {

        return new BuildingResponseDTO(
                entity.getName(),
                entity.getFloors().size(),
                entity.getFloors().stream().mapToDouble(Floor::getSquareMeter).sum());
    }

    private Floor convertToEntity(FloorRequestDTO dto) {

        return Floor.builder()
                .orderNumber(Integer.valueOf(dto.floorNumber()))
                .squareMeter(Double.valueOf(dto.floorSize()))
                .build();
    }
}
