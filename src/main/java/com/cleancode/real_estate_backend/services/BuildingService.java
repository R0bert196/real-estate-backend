package com.cleancode.real_estate_backend.services;

import com.cleancode.real_estate_backend.dtos.administrator.building.request.BuildingRequestDTO;
import com.cleancode.real_estate_backend.dtos.administrator.building.response.BuildingResponseDTOLite;
import com.cleancode.real_estate_backend.dtos.administrator.building.response.BuildingResponseDTO;
import com.cleancode.real_estate_backend.dtos.administrator.building.request.FloorRequestDTO;
import com.cleancode.real_estate_backend.entities.AppUser;
import com.cleancode.real_estate_backend.entities.Building;
import com.cleancode.real_estate_backend.entities.Floor;
import com.cleancode.real_estate_backend.repositories.AppUserRepository;
import com.cleancode.real_estate_backend.repositories.BuildingRepository;
import com.cleancode.real_estate_backend.utils.IAuthenticationFacade;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class BuildingService {
    private final AppUserRepository appUserRepository;
    private final BuildingRepository buildingRepository;
    private final FloorService floorService;
    private final IAuthenticationFacade authenticationFacade;

    public BuildingResponseDTOLite addBuilding(BuildingRequestDTO buildingRequestDTO) {
        try {
            log.info("Adding a new building: {}", buildingRequestDTO.buildingName());

            AppUser user = appUserRepository.findByEmail(authenticationFacade.getAuthentication().getName())
                    .orElseThrow(() -> {
                        log.error("User not found for email: {}", authenticationFacade.getAuthentication().getName());
                        return new EntityNotFoundException("User not found");
                    });

            Building building = convertToEntity(buildingRequestDTO, user);
            Building savedBuilding = buildingRepository.save(building);
            Set<Floor> savedFloors = savedBuilding.getFloors();

            log.info("Building added successfully: {}", savedBuilding.getId());

            return new BuildingResponseDTOLite(
                    savedBuilding.getName(),
                    savedFloors.size(),
                    savedFloors.stream().mapToDouble(Floor::getSize).sum(),
                    savedBuilding.getId()
            );
        } catch (IllegalArgumentException e) {
            log.error("Error while adding building: {}", e.getMessage());
            throw new IllegalArgumentException("Error while adding building", e);
        }
    }

    public BuildingResponseDTOLite updateBuilding(Long buildingId, BuildingRequestDTO buildingRequestDTO) {
        try {
            log.info("Updating building with ID: {}", buildingId);

            Optional<Building> buildingOpt = buildingRepository.findWithManagerAndFloorsById(buildingId);
            if (buildingOpt.isEmpty()) {
                log.error("Building not found with ID: {}", buildingId);
                throw new IllegalArgumentException("Building not found");
            }

            AppUser loggedInUser = appUserRepository.findByEmail(authenticationFacade.getAuthentication().getName())
                    .orElseThrow(() -> {
                        log.error("User not found for email: {}", authenticationFacade.getAuthentication().getName());
                        return new EntityNotFoundException("User not found");
                    });

            Building building = buildingOpt.get();
            if (!building.getManager().equals(loggedInUser)) {
                log.error("Unauthorized update attempt by user: {}", loggedInUser.getEmail());
                throw new IllegalArgumentException("Only the building manager can make changes");
            }

            // Update building details
            building.setName(buildingRequestDTO.buildingName());
            building.setAddress(buildingRequestDTO.address());

            log.debug("Updating floors for building with ID: {}", buildingId);
            Set<Long> requestFloorIds = buildingRequestDTO.floors().stream()
                    .map(FloorRequestDTO::id)
                    .collect(Collectors.toSet());

            Set<Floor> existingFloors = building.getFloors();
            Set<Floor> floorsToRemove = new HashSet<>();

            for (Floor existingFloor : existingFloors) {
                if (!requestFloorIds.contains(existingFloor.getId())) {
                    floorsToRemove.add(existingFloor);
                }
            }

            existingFloors.removeAll(floorsToRemove);

            for (FloorRequestDTO floorRequestDTO : buildingRequestDTO.floors()) {
                Optional<Floor> existingFloorOpt = existingFloors.stream()
                        .filter(floor -> floor.getId().equals(floorRequestDTO.id()))
                        .findFirst();

                if (existingFloorOpt.isPresent()) {
                    Floor floor = existingFloorOpt.get();
                    floor.setSize(Double.parseDouble(floorRequestDTO.size()));
                    floor.setFloorNumber(Integer.parseInt(floorRequestDTO.number()));
                } else {
                    Floor newFloor = new Floor();
                    newFloor.setSize(Double.parseDouble(floorRequestDTO.size()));
                    newFloor.setFloorNumber(Integer.parseInt(floorRequestDTO.number()));
                    newFloor.setBuilding(building);
                    existingFloors.add(newFloor);
                }
            }

            building.setFloors(existingFloors);
            Building savedBuilding = buildingRepository.save(building);
            Set<Floor> savedFloors = savedBuilding.getFloors();

            log.info("Building updated successfully: {}", savedBuilding.getId());

            return new BuildingResponseDTOLite(
                    savedBuilding.getName(),
                    savedFloors.size(),
                    savedFloors.stream().mapToDouble(Floor::getSize).sum(),
                    savedBuilding.getId()
            );
        } catch (IllegalArgumentException e) {
            log.error("Error while updating building: {}", e.getMessage());
            throw new IllegalArgumentException("Error while updating building", e);
        }
    }

    private Building convertToEntity(BuildingRequestDTO dto, AppUser user) {
        log.debug("Converting BuildingRequestDTO to Building entity for building name: {}", dto.buildingName());

        Building building = Building.builder()
                .name(dto.buildingName())
                .address(dto.address())
                .manager(user)
                .build();

        Set<Floor> floors = dto.floors().stream()
                .map(this::convertToEntity)
                .collect(Collectors.toSet());

        building.setFloors(floors);
        floors.forEach(floor -> floor.setBuilding(building));

        log.debug("Converted BuildingRequestDTO to Building entity with {} floors", floors.size());

        return building;
    }

    public BuildingResponseDTOLite convertToDTOLite(Building entity) {
        log.debug("Converting Building entity to BuildingResponseDTOLite for building ID: {}", entity.getId());

        return new BuildingResponseDTOLite(
                entity.getName(),
                entity.getFloors().size(),
                entity.getFloors().stream().mapToDouble(Floor::getSize).sum(),
                entity.getId()
        );
    }

    public BuildingResponseDTO convertToDTO(Building entity) {
        log.debug("Converting Building entity to BuildingResponseDTO for building ID: {}", entity.getId());

        return new BuildingResponseDTO(
                entity.getName(),
                entity.getFloors().stream().map(floorService::convertToDTOLite).toList(),
                entity.getFloors().stream().mapToDouble(Floor::getSize).sum(),
                entity.getAddress(),
                entity.getId()
        );
    }

    private Floor convertToEntity(FloorRequestDTO dto) {
        log.debug("Converting FloorRequestDTO to Floor entity for floor number: {}", dto.number());

        return Floor.builder()
                .floorNumber(Integer.valueOf(dto.number()))
                .size(Double.valueOf(dto.size()))
                .build();
    }

    public List<BuildingResponseDTO> getBuildings(String email) {
        log.info("Fetching buildings for user email: {}", email);

        AppUser user = appUserRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.error("User not found for email: {}", email);
                    return new EntityNotFoundException("User not found");
                });

        List<BuildingResponseDTO> buildings = buildingRepository.findAllWithFloorsByManagerId(user.getId()).stream()
                .map(this::convertToDTO)
                .toList();

        log.info("Fetched {} buildings for user email: {}", buildings.size(), email);

        return buildings;
    }

    public void deleteBuilding(Long buildingId) {
        try {
            log.info("Deleting building with ID: {}", buildingId);

            AppUser appUser = appUserRepository.findByEmail(authenticationFacade.getAuthentication().getName())
                    .orElseThrow(() -> {
                        log.error("User not found for email: {}", authenticationFacade.getAuthentication().getName());
                        return new EntityNotFoundException("User not found");
                    });

            Building building = buildingRepository.findByIdWithManager(buildingId)
                    .orElseThrow(() -> {
                        log.error("Building not found with ID: {}", buildingId);
                        return new EntityNotFoundException("Building not found");
                    });

            if (!building.getManager().equals(appUser)) {
                log.error("Unauthorized deletion attempt by user: {}", appUser.getEmail());
                throw new IllegalArgumentException("Only the building manager can delete the building");
            }

            buildingRepository.deleteById(buildingId);

            log.info("Building deleted successfully with ID: {}", buildingId);
        } catch (IllegalArgumentException e) {
            log.error("Error while deleting building: {}", e.getMessage());
            throw new RuntimeException("Building id is null", e);
        }
    }
}