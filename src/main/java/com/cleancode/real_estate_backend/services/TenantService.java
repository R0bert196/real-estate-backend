package com.cleancode.real_estate_backend.services;

import com.cleancode.real_estate_backend.dtos.administrator.building.response.BuildingResponseDTOLite;
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
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

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
        List<Tenant> tenants =  tenantRepository.findAllWithRentedFloorsAndFloors();
       return tenants.stream().map(this::convertToDTO).toList();
    }

    private TenantResponseDTO convertToDTO(Tenant entity) {

//        List <BuildingResponseDTOLite> buildingResponseDTOLites =  entity.getBuildings().stream().map(buildingService::convertToDTOLite).toList();


        return new TenantResponseDTO(
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
                Floor floor = floorRepository.findById(floorRequest.selectedFloorId())
                        .orElseThrow(() -> new RuntimeException("Floor not found with id: " + floorRequest.selectedFloorId()));

                RentedFloor rentedFloor = RentedFloor.builder()
                        .floor(floor)
                        .tenant(tenant)
                        .rentedSize(floorRequest.selectedSize())
                        .squareMeterPrice(buildingRequest.squareMeterPrice())
                        .maintenanceSquareMeterPrice(buildingRequest.maintenanceSquareMeterPrice())
                        .build();

                tenant.getRentedFloors().add(rentedFloor);
                floor.rentFloor(floorRequest.selectedSize());
                rentedFloorRepository.save(rentedFloor);

                floorRepository.save(floor);


            });

        });

        Tenant savedTenant =  tenantRepository.save(tenant);
        return new TenantResponseDTOLite(savedTenant.getName());
    }
}
