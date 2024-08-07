package com.cleancode.real_estate_backend.controllers;

import com.cleancode.real_estate_backend.dtos.administrator.building.request.BuildingRequestDTO;
import com.cleancode.real_estate_backend.dtos.administrator.building.response.BuildingResponseDTO;
import com.cleancode.real_estate_backend.dtos.administrator.building.response.BuildingResponseDTOLite;
import com.cleancode.real_estate_backend.dtos.administrator.tenants.request.TenantRequestDTO;
import com.cleancode.real_estate_backend.dtos.administrator.tenants.response.RentedFloorResponseDTO;
import com.cleancode.real_estate_backend.dtos.administrator.tenants.response.TenantResponseDTO;
import com.cleancode.real_estate_backend.dtos.administrator.tenants.response.TenantResponseDTOLite;
import com.cleancode.real_estate_backend.dtos.administrator.ticket.response.TicketResponseDTOView;
import com.cleancode.real_estate_backend.dtos.tenant.ticket.request.TicketMessageRequestDTO;
import com.cleancode.real_estate_backend.dtos.tenant.ticket.request.TicketRequestDTO;
import com.cleancode.real_estate_backend.dtos.tenant.ticket.request.TicketUpdateRequestDTO;
import com.cleancode.real_estate_backend.dtos.tenant.ticket.response.TicketResponseDTO;
import com.cleancode.real_estate_backend.dtos.tenant.ticket.response.TicketResponseDTOLite;
import com.cleancode.real_estate_backend.services.*;
import com.cleancode.real_estate_backend.utils.IAuthenticationFacade;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Set;

@RestController
@Log4j2
@RequestMapping("/api/administrator")
@RequiredArgsConstructor
public class AdministratorController {

    private final BuildingService buildingService;
    private final TenantService tenantService;
    private final TicketService ticketService;
    private final PhotoService photoService;
    private final IAuthenticationFacade authenticationFacade;
    private final RentedFloorService rentedFloorService;

    @GetMapping("/building")
    public ResponseEntity<?> getBuildings() {
        log.info("Request to fetch buildings received.");

        Authentication authentication = authenticationFacade.getAuthentication();
        log.debug("Authenticated user: {}", authentication.getName());

        List<BuildingResponseDTO> buildings = buildingService.getBuildings(authentication.getName());

        log.info("Returning {} buildings.", buildings.size());
        return ResponseEntity.ok(buildings);
    }

    @PostMapping("/building")
    public ResponseEntity<?> addBuilding(@RequestBody BuildingRequestDTO buildingRequestDTO) {
        log.info("Request to add a new building received. Building name: {}", buildingRequestDTO.buildingName());

        BuildingResponseDTOLite building = buildingService.addBuilding(buildingRequestDTO);

        log.info("Building added successfully. Building ID: {}", building.id());
        return ResponseEntity.ok(building);
    }

    @PutMapping("/building/{buildingId}")
    public ResponseEntity<?> updateBuilding(
            @PathVariable(value = "buildingId") Long buildingId,
            @RequestBody BuildingRequestDTO buildingRequestDTO) {

        log.info("Request to update building received. Building ID: {}", buildingId);

        BuildingResponseDTOLite building = buildingService.updateBuilding(buildingId, buildingRequestDTO);

        log.info("Building updated successfully. Building ID: {}", building.id());
        return ResponseEntity.ok(building);
    }

    @DeleteMapping("/building/{buildingId}")
    public ResponseEntity<?> deleteBuilding(@PathVariable(value = "buildingId") Long buildingId) {
        log.info("Request to delete building received. Building ID: {}", buildingId);

        buildingService.deleteBuilding(buildingId);

        log.info("Building deleted successfully. Building ID: {}", buildingId);
        return ResponseEntity.ok(null);
    }

    @GetMapping("/rented-floors")
    public ResponseEntity<?> getRentedFloors() {
        log.info("Request to fetch rented floors received.");

        List<RentedFloorResponseDTO> rentedFloors = rentedFloorService.getTenantRentedFloors();

        log.info("Returning {} rented floors.", rentedFloors.size());
        return ResponseEntity.ok(rentedFloors);
    }

    @GetMapping("/tenant")
    public ResponseEntity<?> getTenants() {
        log.info("Request to fetch tenants received.");

        List<TenantResponseDTO> tenants = tenantService.getTenants();

        log.info("Returning {} tenants.", tenants.size());
        return ResponseEntity.ok(tenants);
    }

    @PostMapping("/tenant")
    public ResponseEntity<?> addTenant(@RequestBody TenantRequestDTO tenantRequestDTO) {
        log.info("Request to add a new tenant received. Tenant name: {}", tenantRequestDTO.tenantName());

        TenantResponseDTOLite tenant = tenantService.addTenant(tenantRequestDTO);

        log.info("Tenant added successfully. Tenant name: {}", tenant.name());
        return ResponseEntity.ok(tenant);
    }

    @PutMapping("/tenant/{tenantId}")
    public ResponseEntity<?> updateTenant(@PathVariable(value = "tenantId") Long tenantId,
                                          @RequestBody TenantRequestDTO tenantRequestDTO) {
        log.info("Request to update tenant received. Tenant ID: {}", tenantId);

        TenantResponseDTOLite tenant = tenantService.updateTenant(tenantId, tenantRequestDTO);

        log.info("Tenant updated successfully. Tenant name: {}", tenant.name());
        return ResponseEntity.ok(tenant);
    }

    @DeleteMapping("/tenant/{tenantId}")
    public ResponseEntity<?> deleteTenant(@PathVariable(value = "tenantId") Long tenantId) {
        log.info("Request to delete tenant received. Tenant ID: {}", tenantId);

        tenantService.deleteTenant(tenantId);

        log.info("Tenant deleted successfully. Tenant ID: {}", tenantId);
        return ResponseEntity.ok(null);
    }

    @GetMapping("/ticket")
    public ResponseEntity<?> getTickets(
            @RequestParam(name = "pageNumber", required = true) Integer pageNumber,
            @RequestParam(name = "numberOfItems", required = true) Integer numberOfItems
    ) {
        log.info("Request to fetch tickets received. Page number: {}, Number of items: {}", pageNumber, numberOfItems);

        Pageable pageable = PageRequest.of(pageNumber, numberOfItems);
        List<TicketResponseDTOView> ticketResponseDTOViews = ticketService.getTicketsViewManager(pageable);

        log.info("Returning {} tickets.", ticketResponseDTOViews.size());
        return ResponseEntity.ok(ticketResponseDTOViews);
    }

    @PostMapping("/ticket")
    public ResponseEntity<?> addTicket(
            @RequestParam("subject") String subject,
            @RequestParam("message") String message,
            @RequestParam("severity") String severity,
            @RequestParam("department") String department,
            @RequestParam("rentedFloorId") Long rentedFloorId,
            @RequestParam(value = "images", required = false) MultipartFile[] images) {

        log.info("Request to add a new ticket received. Subject: {}", subject);

        TicketRequestDTO ticketRequestDTO = new TicketRequestDTO(subject, message, severity, department, rentedFloorId);

        TicketResponseDTOLite ticketDto = ticketService.addTicket(ticketRequestDTO);

        if (saveImages(images, ticketDto)) {
            log.error("Error occurred while saving images for the ticket.");
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        log.info("Ticket added successfully. Ticket ID: {}", ticketDto.id());
        return new ResponseEntity<>(ticketDto, HttpStatus.CREATED);
    }

    @GetMapping("/ticket/{ticketId}")
    public ResponseEntity<?> getTicket(@PathVariable(value = "ticketId") Long ticketId) {
        log.info("Request to fetch ticket details received. Ticket ID: {}", ticketId);

        TicketResponseDTO dto = ticketService.getTicket(ticketId);

        log.info("Returning details for ticket ID: {}", ticketId);
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/ticket/count")
    public ResponseEntity<?> countTickets() {
        log.info("Request to count tickets received.");

        long count = ticketService.countTickets();

        log.info("Returning total ticket count: {}", count);
        return ResponseEntity.ok(count);
    }

    @PostMapping("/ticket/{ticketId}/message")
    public ResponseEntity<?> addMessageToTicket(
            @PathVariable(value = "ticketId") Long ticketId,
            @RequestParam("message") String message,
            @RequestParam(value = "images", required = false) MultipartFile[] images
    ) {
        log.info("Request to add a message to ticket received. Ticket ID: {}", ticketId);

        TicketMessageRequestDTO requestDTO = new TicketMessageRequestDTO(message);

        TicketResponseDTOLite ticketDto = ticketService.addMessageToTicket(ticketId, requestDTO);

        if (saveImages(images, ticketDto)) {
            log.error("Error occurred while saving images for the ticket message.");
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        log.info("Message added successfully to ticket ID: {}", ticketId);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    private boolean saveImages(@RequestParam(value = "images", required = false) MultipartFile[] images, TicketResponseDTOLite ticketDto) {
        if (images == null) {
            return false;
        }

        try {
            log.info("Saving images for ticket message ID: {}", ticketDto.ticketMessageId());

            // Save the images to the disk
            Set<String> imageUrls = photoService.savePhotos(ticketDto.ticketMessageId(), images);

            // Save the path to the image into the ticket message
            ticketService.addPhotosUrlsToMessage(ticketDto.ticketMessageId(), imageUrls);

            log.info("Images saved successfully for ticket message ID: {}", ticketDto.ticketMessageId());
        } catch (IOException e) {
            log.error("Error while saving images for ticket message ID: {}. Error: {}", ticketDto.ticketMessageId(), e.getMessage());
            return true;
        }
        return false;
    }

    @PutMapping("/ticket/{ticketId}")
    public ResponseEntity<?> updateTicket(
            @PathVariable(value = "ticketId") Long ticketId,
            @RequestBody TicketUpdateRequestDTO ticketUpdateRequestDTO) {
        log.info("Request to update ticket received. Ticket ID: {}", ticketId);

        ticketService.updateTicket(ticketId, ticketUpdateRequestDTO);

        log.info("Ticket updated successfully. Ticket ID: {}", ticketId);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }
}
