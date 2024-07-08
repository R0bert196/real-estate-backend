package com.cleancode.real_estate_backend.controllers;

import com.cleancode.real_estate_backend.dtos.administrator.building.request.BuildingRequestDTO;
import com.cleancode.real_estate_backend.dtos.administrator.building.response.BuildingResponseDTO;
import com.cleancode.real_estate_backend.dtos.administrator.tenants.request.TenantRequestDTO;
import com.cleancode.real_estate_backend.dtos.administrator.tenants.response.TenantResponseDTO;
import com.cleancode.real_estate_backend.services.BuildingService;
import com.cleancode.real_estate_backend.services.TenantService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Slf4j
@RequestMapping("/api/administrator")
@RequiredArgsConstructor
public class AdministratorController {

    private final BuildingService buildingService;
    private final TenantService tenantService;

    @PostMapping("/building")
    public ResponseEntity<?> addBuilding(@RequestBody BuildingRequestDTO buildingRequestDTO) {

        return ResponseEntity.ok(buildingService.addBuilding(buildingRequestDTO));
    }

    @GetMapping("/building")
    public ResponseEntity<?> getBuildings() {

        List<BuildingResponseDTO> buildings = buildingService.getBuildings();

        return ResponseEntity.ok(buildings);
    }

    @GetMapping("/tenant")
    public ResponseEntity<?> getTenants() {

        List<TenantResponseDTO> tenants = tenantService.getTenants();
        return ResponseEntity.ok(tenants);
    }

    @PostMapping("/tenant")
    public ResponseEntity<?> addTenant(@RequestBody TenantRequestDTO tenantRequestDTO) {

        TenantResponseDTO tenant = tenantService.addTenant(tenantRequestDTO);
        return ResponseEntity.ok(tenant);

    }
}
