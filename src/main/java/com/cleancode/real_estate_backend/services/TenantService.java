package com.cleancode.real_estate_backend.services;

import com.cleancode.real_estate_backend.dtos.administrator.tenants.request.TenantRequestDTO;
import com.cleancode.real_estate_backend.dtos.administrator.tenants.response.TenantResponseDTO;
import com.cleancode.real_estate_backend.dtos.administrator.tenants.response.TenantResponseDTOLite;
import com.cleancode.real_estate_backend.entities.Building;
import com.cleancode.real_estate_backend.entities.Floor;
import com.cleancode.real_estate_backend.entities.RentedFloor;
import com.cleancode.real_estate_backend.entities.Tenant;
import com.cleancode.real_estate_backend.repositories.BuildingRepository;
import com.cleancode.real_estate_backend.repositories.FloorRepository;
import com.cleancode.real_estate_backend.repositories.RentedFloorRepository;
import com.cleancode.real_estate_backend.repositories.TenantRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;


@Service
@Slf4j
@RequiredArgsConstructor
public class TenantService {

    private final TenantRepository tenantRepository;
    private final BuildingService buildingService;
    private final BuildingRepository buildingRepository;
    private final FloorRepository floorRepository;
    private final RentedFloorRepository rentedFloorRepository;
    private final RentedFloorService rentedFloorService;

    public List<TenantResponseDTO> getTenants() {


        //todo find in functie de administrator
        List<Tenant> tenants = tenantRepository.findAllWithRentedFloorsAndFloors();
        return tenants.stream().map(this::convertToDTO).toList();
    }

    private TenantResponseDTO convertToDTO(Tenant entity) {

        return new TenantResponseDTO(
                entity.getId(),
                entity.getName(),
                entity.getRentedFloors().stream().map(rentedFloorService::convertToDTO).toList());
    }


    @Transactional
    public TenantResponseDTOLite addTenant(TenantRequestDTO tenantCreationRequest) {

        Tenant tenant = new Tenant();

        tenant.setName(tenantCreationRequest.tenantName());


        tenantCreationRequest.rentedFloors().forEach(rentedFloorsRequestDTO -> {

            Building building = buildingRepository.findById(rentedFloorsRequestDTO.selectedBuildingId())
                    .orElseThrow(() -> new RuntimeException("Building id not found"));

            Floor floor = floorRepository.findById(rentedFloorsRequestDTO.selectedFloorId())
                    .orElseThrow(() -> new RuntimeException("Floor id not found"));

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


        });

        Tenant savedTenant = tenantRepository.save(tenant);

        return new TenantResponseDTOLite(savedTenant.getName());
    }

    public void deleteTenant(Long tenantId) {
        try {

            tenantRepository.deleteById(tenantId);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Tenant selectedFloorId is null");
        }
    }

    public TenantResponseDTOLite updateTenant(Long tenantId, TenantRequestDTO tenantRequestDTO) {

        Tenant tenant = tenantRepository.findWithRentedFloorsAndFloorsById(tenantId)
                .orElseThrow(EntityNotFoundException::new);

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

                    //remove floor key if its values has been only updated
                    existingRentedSizes.remove(rentedFloorRequest.selectedFloorId());

                    rentedFloor.setFloor(floorEntity);

                    return rentedFloor;
                })
                .collect(Collectors.toSet());

        tenant.getRentedFloors().addAll(rentedFloors);

        // give back the available sizes to the deleted floors
        existingRentedSizes.forEach((key, value) -> {
            Floor floor = floorRepository.findById(key).orElseThrow(EntityNotFoundException::new);
            floor.rentFloor(value, 0.0);

        });

        tenantRepository.save(tenant);

        return new TenantResponseDTOLite(tenant.getName());
    }


    public void deleteBuilding(Long buildingId) {
        try {
            buildingRepository.deleteById(buildingId);

        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Building selectedFloorId is null");
        }
    }
}
