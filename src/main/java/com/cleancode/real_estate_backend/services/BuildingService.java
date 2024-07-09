package com.cleancode.real_estate_backend.services;

import com.cleancode.real_estate_backend.dtos.administrator.building.request.BuildingRequestDTO;
import com.cleancode.real_estate_backend.dtos.administrator.building.response.BuildingResponseDTOLite;
import com.cleancode.real_estate_backend.dtos.administrator.building.response.BuildingResponseDTO;
import com.cleancode.real_estate_backend.dtos.administrator.building.request.FloorRequestDTO;
import com.cleancode.real_estate_backend.entities.Building;
import com.cleancode.real_estate_backend.entities.Floor;
import com.cleancode.real_estate_backend.repositories.BuildingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class BuildingService {

    private final BuildingRepository buildingRepository;
    private final FloorService floorService;

    public BuildingResponseDTOLite addBuilding(BuildingRequestDTO buildingRequestDTO) {

        try {

            Building building = convertToEntity(buildingRequestDTO);

            Building savedBuilding = buildingRepository.save(building);
            Set<Floor> savedFloors = savedBuilding.getFloors();

            return new BuildingResponseDTOLite(building.getName(), savedFloors.size(), savedFloors.stream().mapToDouble(Floor::getSize).sum(), building.getId());
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

    public BuildingResponseDTOLite convertToDTOLite(Building entity) {

        return new BuildingResponseDTOLite(
                entity.getName(),
                entity.getFloors().size(),
                entity.getFloors().stream().mapToDouble(Floor::getSize).sum(),
                entity.getId());
    }

    public BuildingResponseDTO convertToDTO(Building entity) {

        return new BuildingResponseDTO(
                entity.getName(),
                entity.getFloors().stream().map(floorService::convertToDTOLite).toList(),
                entity.getFloors().stream().mapToDouble(Floor::getSize).sum(),
                entity.getId());
    }

    private Floor convertToEntity(FloorRequestDTO dto) {

        return Floor.builder()
                .floorNumber(Integer.valueOf(dto.floorNumber()))
                .size(Double.valueOf(dto.floorSize()))
                .build();
    }

    public List<BuildingResponseDTO> getBuildings() {

        return buildingRepository.findAll().stream().map(this::convertToDTO).toList();
    }
}
