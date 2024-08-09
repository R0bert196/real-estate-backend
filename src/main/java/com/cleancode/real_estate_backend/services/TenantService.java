package com.cleancode.real_estate_backend.services;

import com.cleancode.real_estate_backend.dtos.manager.tenants.request.TenantRequestDTO;
import com.cleancode.real_estate_backend.dtos.manager.tenants.response.TenantResponseDTO;
import com.cleancode.real_estate_backend.dtos.manager.tenants.response.TenantResponseDTOLite;
import com.cleancode.real_estate_backend.dtos.manager.tenants.response.TenantResponseDTOView;
import com.cleancode.real_estate_backend.entities.*;
import com.cleancode.real_estate_backend.repositories.*;
import com.cleancode.real_estate_backend.utils.IAuthenticationFacade;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;


@Service
@Log4j2
@RequiredArgsConstructor
public class TenantService {

    private final AppUserRepository appUserRepository;
    private final TenantRepository tenantRepository;
    private final BuildingRepository buildingRepository;
    private final FloorRepository floorRepository;
    private final RentedFloorRepository rentedFloorRepository;
    private final RentedFloorService rentedFloorService;
    private final IAuthenticationFacade authenticationFacade;

    public List<TenantResponseDTOView> getTenantsView() {
        log.info("Fetching the authenticated user.");
        AppUser manager = appUserRepository.findByEmail(authenticationFacade.getAuthentication().getName())
                .orElseThrow(() -> {
                    log.error("User not found.");
                    return new EntityNotFoundException("User not found.");
                });

        log.debug("Authenticated user: {}", manager.getEmail());

        List<Tenant> tenants = tenantRepository.findAllWithRentedFloorsAndFloorsAndBuildingByManagerId(manager.getId());

        log.info("Fetched {} tenants for manager with ID: {}", tenants.size(), manager.getId());

        return tenants.stream().map(tenant -> {
            // Calculate the total rented size
            double totalRentedSize = tenant.getRentedFloors().stream()
                    .mapToDouble(RentedFloor::getRentedSize)
                    .sum();

            // Calculate the average square meter price
            double averageSquareMeterPrice = tenant.getRentedFloors().stream()
                    .mapToDouble(RentedFloor::getSquareMeterPrice)
                    .average()
                    .orElse(0.0);  // Provide a default value in case the average is not present

            // Calculate the average maintenance square meter price
            double averageMaintenancePrice = tenant.getRentedFloors().stream()
                    .mapToDouble(RentedFloor::getMaintenanceSquareMeterPrice)
                    .average()
                    .orElse(0.0);  // Provide a default value in case the average is not present

            // Get the unique buildings
            Integer uniqueBuildingsCount = Math.toIntExact(tenant.getRentedFloors().stream()
                    .map(rentedFloor -> rentedFloor.getFloor().getBuilding())
                    .distinct()
                    .count());

            return new TenantResponseDTOView(
                    tenant.getId(),
                    tenant.getName(),
                    totalRentedSize,
                    averageSquareMeterPrice,
                    averageMaintenancePrice,
                    uniqueBuildingsCount
            );
        }).toList();
    }

    public TenantResponseDTO getTenantDetails(Long tenantId) {

        log.info("Fetching the authenticated user.");
        AppUser manager = appUserRepository.findByEmail(authenticationFacade.getAuthentication().getName())
                .orElseThrow(EntityNotFoundException::new);

        log.debug("Authenticated user: {}", manager.getEmail());

        Tenant tenant = tenantRepository.findWithRentedFloorsAndFloorsAndBuildingByManagerIdAndTenantId(manager.getId(), tenantId);

        log.info("Fetched tenant with ID: {}", tenant.getId());
        return convertToDTO(tenant);

    }

    private TenantResponseDTO convertToDTO(Tenant entity) {
        log.debug("Converting tenant entity to DTO: {}", entity.getName());
        return new TenantResponseDTO(
                entity.getId(),
                entity.getName(),
                entity.getRentedFloors().stream().map(rentedFloorService::convertToDTO).toList());
    }


    @Transactional
    public TenantResponseDTOLite addTenant(TenantRequestDTO tenantCreationRequest) {
        log.info("Adding new tenant with name: {}", tenantCreationRequest.tenantName());
        Tenant tenant = new Tenant();

        AppUser appUser = appUserRepository.findByEmail(authenticationFacade.getAuthentication().getName())
                .orElseThrow(EntityNotFoundException::new);

        tenant.setManager(appUser);
        tenant.setName(tenantCreationRequest.tenantName());

        tenantCreationRequest.rentedFloors().forEach(rentedFloorsRequestDTO -> {
            log.debug("Processing rented floor for building ID: {}, floor ID: {}",
                    rentedFloorsRequestDTO.selectedBuildingId(),
                    rentedFloorsRequestDTO.selectedFloorId());

            Building building = buildingRepository.findById(rentedFloorsRequestDTO.selectedBuildingId())
                    .orElseThrow(() -> new RuntimeException("Building ID not found"));

            Floor floor = floorRepository.findById(rentedFloorsRequestDTO.selectedFloorId())
                    .orElseThrow(() -> new RuntimeException("Floor ID not found"));

            RentedFloor rentedFloor = RentedFloor.builder()
                    .floor(floor)
                    .tenant(tenant)
                    .rentedSize(rentedFloorsRequestDTO.rentedSize())
                    .squareMeterPrice(rentedFloorsRequestDTO.squareMeterPrice())
                    .maintenanceSquareMeterPrice(rentedFloorsRequestDTO.maintenanceSquareMeterPrice())
                    .build();

            tenant.getRentedFloors().add(rentedFloor);
            floor.rentFloor(0.0, rentedFloorsRequestDTO.rentedSize());
            rentedFloorRepository.save(rentedFloor);

            floorRepository.save(floor);
            log.debug("Rented floor saved for tenant: {}", tenant.getName());
        });

        Tenant savedTenant = tenantRepository.save(tenant);
        log.info("Tenant added successfully: {}", savedTenant.getName());

        return new TenantResponseDTOLite(savedTenant.getName());
    }

    public void deleteTenant(Long tenantId) {
        try {
            log.info("Deleting tenant with ID: {}", tenantId);
            AppUser manager = appUserRepository.findByEmail(authenticationFacade.getAuthentication().getName())
                    .orElseThrow(EntityNotFoundException::new);

            Tenant tenant = tenantRepository.findById(tenantId)
                    .orElseThrow(EntityNotFoundException::new);

            if (!tenant.getManager().equals(manager)) {
                log.error("Manager mismatch: Logged in manager does not manage this tenant.");
                throw new IllegalArgumentException("Only the tenant's manager can delete the tenant");
            }

            tenantRepository.deleteById(tenantId);
            log.info("Tenant deleted successfully with ID: {}", tenantId);
        } catch (IllegalArgumentException e) {
            log.error("Failed to delete tenant with ID: {}. Error: {}", tenantId, e.getMessage());
            throw new RuntimeException("Tenant selectedFloorId is null");
        }
    }

    public TenantResponseDTOLite updateTenant(Long tenantId, TenantRequestDTO tenantRequestDTO) {
        log.info("Updating tenant with ID: {}", tenantId);

        Tenant tenant = tenantRepository.findWithRentedFloorsAndFloorsById(tenantId)
                .orElseThrow(EntityNotFoundException::new);

        AppUser manager = appUserRepository.findByEmail(authenticationFacade.getAuthentication().getName())
                .orElseThrow(EntityNotFoundException::new);

        if (!tenant.getManager().equals(manager)) {
            log.error("Manager mismatch: Logged in manager does not manage this tenant.");
            throw new RuntimeException("Only the tenant's manager can update the tenant");
        }

        tenant.setName(tenantRequestDTO.tenantName());

        // Retrieve existing rentedFloors to manage available space correctly
        Map<Long, Double> existingRentedSizes = tenant.getRentedFloors().stream()
                .collect(Collectors.toMap(
                        rentedFloor -> rentedFloor.getFloor().getId(),
                        RentedFloor::getRentedSize
                ));

        // Clear existing rentedFloors to avoid orphaned entities
        tenant.getRentedFloors().clear();

        // Map and save new rentedFloors
        Set<RentedFloor> rentedFloors = tenantRequestDTO.rentedFloors().stream()
                .map(rentedFloorRequest -> {
                    RentedFloor rentedFloor = new RentedFloor();
                    rentedFloor.setRentedSize(rentedFloorRequest.rentedSize());
                    rentedFloor.setSquareMeterPrice(rentedFloorRequest.squareMeterPrice());
                    rentedFloor.setMaintenanceSquareMeterPrice(rentedFloorRequest.maintenanceSquareMeterPrice());

                    rentedFloor.setId(rentedFloorRequest.selectedFloorId());
                    rentedFloor.setTenant(tenant); // Set the bidirectional relationship

                    // Set the floor association
                    Floor floorEntity = floorRepository.findById(rentedFloorRequest.selectedFloorId())
                            .orElseThrow(EntityNotFoundException::new);

                    // Adjust floor remaining size
                    Double previousRentedSize = existingRentedSizes.getOrDefault(rentedFloorRequest.selectedFloorId(), 0.0);
                    floorEntity.rentFloor(previousRentedSize, rentedFloorRequest.rentedSize());

                    // Remove floor key if its values have only been updated
                    existingRentedSizes.remove(rentedFloorRequest.selectedFloorId());

                    rentedFloor.setFloor(floorEntity);
                    log.debug("Updated rented floor for tenant: {}", tenant.getName());

                    return rentedFloor;
                })
                .collect(Collectors.toSet());

        tenant.getRentedFloors().addAll(rentedFloors);

        // Give back the available sizes to the deleted floors
        existingRentedSizes.forEach((key, value) -> {
            Floor floor = floorRepository.findById(key).orElseThrow(EntityNotFoundException::new);
            floor.rentFloor(value, 0.0);
            log.debug("Updated floor size for floor ID: {}", key);
        });

        tenantRepository.save(tenant);
        log.info("Tenant updated successfully: {}", tenant.getName());

        return new TenantResponseDTOLite(tenant.getName());
    }

}
