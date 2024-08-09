package com.cleancode.real_estate_backend.services;

import com.cleancode.real_estate_backend.dtos.manager.tenants.response.RentedFloorResponseDTO;
import com.cleancode.real_estate_backend.entities.AppUser;
import com.cleancode.real_estate_backend.entities.RentedFloor;
import com.cleancode.real_estate_backend.enums.Role;
import com.cleancode.real_estate_backend.repositories.AppUserRepository;
import com.cleancode.real_estate_backend.repositories.RentedFloorRepository;
import com.cleancode.real_estate_backend.utils.IAuthenticationFacade;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
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

    public List<RentedFloorResponseDTO> getTenantRentedFloorsRepresentative() {

        String userEmail = authenticationFacade.getAuthentication().getName();
        log.info("Fetching rented floors for user: {}", userEmail);

        AppUser loggedUser = appUserRepository.findByEmail(userEmail)
                .orElseThrow(() -> {
                    log.error("User not found: {}", userEmail);
                    return new EntityNotFoundException("User not found: " + userEmail);
                });

            log.info("User is a representative. Fetching rented floors by representative ID: {}", loggedUser.getId());

            return rentedFloorRepository.findAllWithFloorAndBuildingByRepresentativeId(loggedUser.getId())
                    .stream()
                    .map(this::convertToDTO)
                    .toList();
    }


    public List<RentedFloorResponseDTO> getTenantRentedFloorsManager(Long tenantId) {
        String userEmail = authenticationFacade.getAuthentication().getName();
        log.info("Fetching rented floors for user: {}", userEmail);

        AppUser loggedUser = appUserRepository.findByEmail(userEmail)
                .orElseThrow(() -> {
                    log.error("User not found: {}", userEmail);
                    return new EntityNotFoundException("User not found: " + userEmail);
                });

        log.info("User is a manager. Fetching rented floors by manager ID: {}", loggedUser.getId());
        return rentedFloorRepository.findAllWithFloorAndBuildingByManagerIdAndTenantId(loggedUser.getId(), tenantId)
                .stream()
                .map(this::convertToDTO)
                .toList();

    }
}
