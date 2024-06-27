package com.cleancode.real_estate_backend.services;

import com.cleancode.real_estate_backend.dtos.administrator.building.BuildingResponseDTO;
import com.cleancode.real_estate_backend.dtos.administrator.tenants.TenantResponseDTO;
import com.cleancode.real_estate_backend.entities.Tenant;
import com.cleancode.real_estate_backend.repositories.TenantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class TenantService {

    private final TenantRepository tenantRepository;
    private final BuildingService buildingService;

    public List<TenantResponseDTO> getTenants() {


        //todo find in functie de administrator
       return tenantRepository.findAll().stream().map(this::convertToDTO).toList();
    }

    private TenantResponseDTO convertToDTO(Tenant entity) {

        List <BuildingResponseDTO> buildingResponseDTOS =  entity.getBuildings().stream().map(buildingService::convertToDTO).toList();


        return new TenantResponseDTO(
                entity.getName(),
                entity.getSquareMeterPrice(),
                entity.getMaintenanceSquareMeterPrice(),
                buildingResponseDTOS);
    }
}
