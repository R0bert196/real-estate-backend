package com.cleancode.real_estate_backend.services;

import com.cleancode.real_estate_backend.dtos.administrator.building.BuildingResponseDTOLite;
import com.cleancode.real_estate_backend.dtos.administrator.tenants.TenantResponseDTO;
import com.cleancode.real_estate_backend.entities.Tenant;
import com.cleancode.real_estate_backend.repositories.TenantRepository;
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

    public List<TenantResponseDTO> getTenants() {


        //todo find in functie de administrator
       return tenantRepository.findAll().stream().map(this::convertToDTO).toList();
    }

    private TenantResponseDTO convertToDTO(Tenant entity) {

        List <BuildingResponseDTOLite> buildingResponseDTOLites =  entity.getBuildings().stream().map(buildingService::convertToDTOLite).toList();


        return new TenantResponseDTO(
                entity.getName(),
                entity.getSquareMeterPrice(),
                entity.getMaintenanceSquareMeterPrice(),
                buildingResponseDTOLites);
    }
}
