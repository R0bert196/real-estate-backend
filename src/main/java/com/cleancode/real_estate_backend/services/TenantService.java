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

//    public List<TenantResponseDTO> addTenant(TenantRequestDTO tenantRequestDTO) {
//
//        Tenant tenant = Tenant.builder()
//                .name(tenantRequestDTO.tenantName())
//                .rentedFloors()
//    }

    @Transactional
    public TenantResponseDTOLite addTenant(TenantRequestDTO tenantCreationRequest) {

        Tenant tenant = new Tenant();

        tenant.setName(tenantCreationRequest.tenantName());

        tenantCreationRequest.buildings().forEach(buildingRequest -> {

            Building building = buildingRepository.findById(buildingRequest.selectedBuildingId())
                    .orElseThrow(() -> new RuntimeException("Building not found with id: " + buildingRequest.selectedBuildingId()));

            buildingRequest.selectedFloors().forEach(floorRequest -> {
                Floor floor = floorRepository.findById(floorRequest.id())
                        .orElseThrow(() -> new RuntimeException("Floor not found with id: " + floorRequest.id()));

                RentedFloor rentedFloor = RentedFloor.builder()
                        .floor(floor)
                        .tenant(tenant)
                        .rentedSize(floorRequest.selectedSize())
                        .squareMeterPrice(buildingRequest.squareMeterPrice())
                        .maintenanceSquareMeterPrice(buildingRequest.maintenanceSquareMeterPrice())
                        .build();

                tenant.getRentedFloors().add(rentedFloor);
                floor.rentFloor(0.0, floorRequest.selectedSize());
                rentedFloorRepository.save(rentedFloor);

                floorRepository.save(floor);


            });

        });

        Tenant savedTenant = tenantRepository.save(tenant);

        return new TenantResponseDTOLite(savedTenant.getName());
    }

    public void deleteTenant(Long tenantId) {
        try {

            tenantRepository.deleteById(tenantId);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Tenant id is null");
        }
    }

    public TenantResponseDTOLite editTenant(Long tenantId, TenantRequestDTO tenantRequestDTO) {
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
        Set<RentedFloor> rentedFloors = tenantRequestDTO.buildings().stream()
                .flatMap(building -> building.selectedFloors().stream()
                        .map(floorDTO -> {
                            RentedFloor rentedFloor = new RentedFloor();
                            rentedFloor.setRentedSize(floorDTO.selectedSize());
                            rentedFloor.setSquareMeterPrice(building.squareMeterPrice());
                            rentedFloor.setMaintenanceSquareMeterPrice(building.maintenanceSquareMeterPrice());

                            rentedFloor.setId(floorDTO.id());

                            rentedFloor.setTenant(tenant); // Set the bidirectional relationship

                            // Set the floor association
                            Floor floorEntity = floorRepository.findById(floorDTO.id())
                                    .orElseThrow(EntityNotFoundException::new);

                            // Adjust floor remaining size
                            Double previousRentedSize = existingRentedSizes.getOrDefault(floorDTO.id(), 0.0);
                            floorEntity.rentFloor(previousRentedSize, floorDTO.selectedSize());

                            rentedFloor.setFloor(floorEntity);

                            return rentedFloor;
                        }))
                .collect(Collectors.toSet());

        tenant.getRentedFloors().addAll(rentedFloors);

        tenantRepository.save(tenant);

        return new TenantResponseDTOLite(tenant.getName());
    }


    public void deleteBuilding(Long buildingId) {
        try {
            buildingRepository.deleteById(buildingId);

        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Building id is null");
        }
    }
}
