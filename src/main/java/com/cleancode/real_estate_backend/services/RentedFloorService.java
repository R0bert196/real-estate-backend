package com.cleancode.real_estate_backend.services;

import com.cleancode.real_estate_backend.dtos.administrator.tenants.response.RentedFloorResponseDTO;
import com.cleancode.real_estate_backend.entities.AppUser;
import com.cleancode.real_estate_backend.entities.RentedFloor;
import com.cleancode.real_estate_backend.enums.Role;
import com.cleancode.real_estate_backend.repositories.AppUserRepository;
import com.cleancode.real_estate_backend.repositories.RentedFloorRepository;
import com.cleancode.real_estate_backend.utils.IAuthenticationFacade;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
@Log4j2
public class RentedFloorService {

    private final RentedFloorRepository rentedFloorRepository;
    private final FloorService floorService;
    private final IAuthenticationFacade authenticationFacade;
    private final AppUserRepository appUserRepository;

    RentedFloorResponseDTO convertToDTO(RentedFloor entity) {
        log.info("Converting RentedFloor entity to RentedFloorResponseDTO: RentedFloor ID = {}", entity.getId());

        RentedFloorResponseDTO dto = new RentedFloorResponseDTO(
                entity.getId(),
                entity.getRentedSize(),
                entity.getSquareMeterPrice(),
                entity.getMaintenanceSquareMeterPrice(),
                floorService.convertToDTO(entity.getFloor())
        );

        log.info("Converted RentedFloorResponseDTO: {}", dto);
        return dto;
    }

    public List<RentedFloorResponseDTO> getTenantRentedFloors() {
        String userEmail = authenticationFacade.getAuthentication().getName();
        log.info("Fetching rented floors for user: {}", userEmail);

        AppUser loggedUser = appUserRepository.findByEmail(userEmail)
                .orElseThrow(() -> {
                    log.error("User not found: {}", userEmail);
                    return new EntityNotFoundException("User not found: " + userEmail);
                });

        if (loggedUser.getRole().stream().anyMatch(role -> role == Role.ROLE_MANAGER)) {
            log.info("User is a manager. Fetching rented floors by manager ID: {}", loggedUser.getId());
            return rentedFloorRepository.findAllWithFloorAndBuildingByManagerId(loggedUser.getId())
                    .stream()
                    .map(this::convertToDTO)
                    .toList();
        } else if (loggedUser.getRole().stream().anyMatch(role -> role == Role.ROLE_REPRESENTANT)) {
            log.info("User is a representant. Fetching rented floors by representant ID: {}", loggedUser.getId());
            return rentedFloorRepository.findAllWithFloorAndBuildingByRepresentantId(loggedUser.getId())
                    .stream()
                    .map(this::convertToDTO)
                    .toList();
        }

        log.error("User role not found for user: {}", userEmail);
        throw new IllegalArgumentException("User role not found");
    }
}
