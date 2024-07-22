package com.cleancode.real_estate_backend.controllers;

import com.cleancode.real_estate_backend.dtos.administrator.building.request.BuildingRequestDTO;
import com.cleancode.real_estate_backend.dtos.administrator.building.response.BuildingResponseDTO;
import com.cleancode.real_estate_backend.dtos.administrator.tenants.request.TenantRequestDTO;
import com.cleancode.real_estate_backend.dtos.administrator.tenants.response.TenantResponseDTO;
import com.cleancode.real_estate_backend.dtos.administrator.tenants.response.TenantResponseDTOLite;
import com.cleancode.real_estate_backend.dtos.administrator.ticket.response.TicketResponseDTOView;
import com.cleancode.real_estate_backend.dtos.tenant.ticket.response.TicketResponseDTO;
import com.cleancode.real_estate_backend.services.BuildingService;
import com.cleancode.real_estate_backend.services.TenantService;
import com.cleancode.real_estate_backend.services.TicketService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
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
    private final TicketService ticketService;


    @GetMapping("/building")
    public ResponseEntity<?> getBuildings() {

        List<BuildingResponseDTO> buildings = buildingService.getBuildings();

        return ResponseEntity.ok(buildings);
    }

    @PostMapping("/building")
    public ResponseEntity<?> addBuilding(@RequestBody BuildingRequestDTO buildingRequestDTO) {

        return ResponseEntity.ok(buildingService.addBuilding(buildingRequestDTO));
    }

    @PutMapping("/building/{buildingId}")
    public ResponseEntity<?> updateBuilding(
            @PathVariable(value = "buildingId") Long buildingId,
            @RequestBody BuildingRequestDTO buildingRequestDTO) {

        System.out.println(buildingId);
        System.out.println(buildingRequestDTO);
        return ResponseEntity.ok(buildingService.updateBuilding(buildingId, buildingRequestDTO));
    }


    @DeleteMapping("/building/{buildingId}")
    public ResponseEntity<?> deleteBuilding(@PathVariable(value = "buildingId") Long buildingId) {

        tenantService.deleteBuilding(buildingId);
        return ResponseEntity.ok(null);
    }

    @GetMapping("/tenant")
    public ResponseEntity<?> getTenants() {

        List<TenantResponseDTO> tenants = tenantService.getTenants();
        return ResponseEntity.ok(tenants);
    }

    @PostMapping("/tenant")
    public ResponseEntity<?> addTenant(@RequestBody TenantRequestDTO tenantRequestDTO) {

        TenantResponseDTOLite tenant = tenantService.addTenant(tenantRequestDTO);
        return ResponseEntity.ok(tenant);

    }

    @PutMapping("/tenant/{tenantId}")
    public ResponseEntity<?> updateTenant(@PathVariable(value = "tenantId") Long tenantId,
                                          @RequestBody TenantRequestDTO tenantRequestDTO) {

        TenantResponseDTOLite tenant = tenantService.updateTenant(tenantId, tenantRequestDTO);
        return ResponseEntity.ok(tenant);
    }

    @DeleteMapping("/tenant/{tenantId}")
    public ResponseEntity<?> deleteTenant(@PathVariable(value = "tenantId") Long tenantId) {

        tenantService.deleteTenant(tenantId);
        return ResponseEntity.ok(null);
    }

    @GetMapping("/ticket")
    public ResponseEntity<?> getTickets(
            @RequestParam (name = "pageNumber", required = true) Integer pageNumber,
            @RequestParam (name = "numberOfItems", required = true) Integer numberOfItems
    ) {
        System.out.println(pageNumber + " " + numberOfItems);
            Pageable pageable = PageRequest.of(pageNumber, numberOfItems);
            List<TicketResponseDTOView> ticketResponseDTOViews = ticketService.getTicketsViewAdministrator(1L, pageable);
            return ResponseEntity.ok(ticketResponseDTOViews);
    }

    @GetMapping("/ticket/{ticketId}")
    public ResponseEntity<?> getTicket(@PathVariable(value = "ticketId") Long ticketId) {

        TicketResponseDTO dto = ticketService.getTicket(ticketId);
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/ticket/count")
    public ResponseEntity<?> countTickets(){
        return ResponseEntity.ok(ticketService.countTickets()) ;
    }
}
