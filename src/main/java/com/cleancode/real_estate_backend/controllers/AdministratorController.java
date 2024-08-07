package com.cleancode.real_estate_backend.controllers;

import
        com.cleancode.real_estate_backend.dtos.administrator.building.request.BuildingRequestDTO;
import com.cleancode.real_estate_backend.dtos.administrator.building.response.BuildingResponseDTO;
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
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
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

        Authentication authentication = authenticationFacade.getAuthentication();
        List<BuildingResponseDTO> buildings = buildingService.getBuildings(authentication.getName());

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


        return ResponseEntity.ok(buildingService.updateBuilding(buildingId, buildingRequestDTO));
    }


    @DeleteMapping("/building/{buildingId}")
    public ResponseEntity<?> deleteBuilding(@PathVariable(value = "buildingId") Long buildingId) {

        buildingService.deleteBuilding(buildingId);
        return ResponseEntity.ok(null);
    }

    @GetMapping("/rented-floors")
    public ResponseEntity<?> getRentedFloors() {


        List<RentedFloorResponseDTO> rentedFloors = rentedFloorService.getTenantRentedFloors();
        return ResponseEntity.ok(rentedFloors);
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
            @RequestParam(name = "pageNumber", required = true) Integer pageNumber,
            @RequestParam(name = "numberOfItems", required = true) Integer numberOfItems
    ) {
        Pageable pageable = PageRequest.of(pageNumber, numberOfItems);
        List<TicketResponseDTOView> ticketResponseDTOViews = ticketService.getTicketsViewManager(pageable);
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


        TicketRequestDTO ticketRequestDTO = new TicketRequestDTO(subject, message, severity, department, rentedFloorId);

        TicketResponseDTOLite ticketDto = ticketService.addTicket(ticketRequestDTO);

        if (saveImages(images, ticketDto)) return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        return new ResponseEntity<>(ticketDto, HttpStatus.CREATED);

    }

    @GetMapping("/ticket/{ticketId}")
    public ResponseEntity<?> getTicket(@PathVariable(value = "ticketId") Long ticketId) {

        TicketResponseDTO dto = ticketService.getTicket(ticketId);
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/ticket/count")
    public ResponseEntity<?> countTickets() {
        return ResponseEntity.ok(ticketService.countTickets());
    }

    //TODO: refactor, same code on tenant controller
    @PostMapping("/ticket/{ticketId}/message")
    public ResponseEntity<?> addMessageToTicket(
            @PathVariable(value = "ticketId") Long ticketId,
            @RequestParam("message") String message,
            @RequestParam(value = "images", required = false) MultipartFile[] images
    ) {

        TicketMessageRequestDTO requestDTO = new TicketMessageRequestDTO(message);

        TicketResponseDTOLite ticketDto = ticketService.addMessageToTicket(ticketId, requestDTO);

        if (saveImages(images, ticketDto)) return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    //TODO: refactor, same code on tenant controller
    private boolean saveImages(@RequestParam(value = "images", required = false) MultipartFile[] images, TicketResponseDTOLite ticketDto) {
        if (images == null) {
            return false;
        }

        try {

            // save the images to the disk
            Set<String> imageUrls = photoService.savePhotos(ticketDto.ticketMessageId(), images);

            // save the path to the image into the ticket message
            ticketService.addPhotosUrlsToMessage(ticketDto.ticketMessageId(), imageUrls);
        } catch (IOException e) {
            log.error(e.toString());
            return true;
        }
        return false;
    }

    @PutMapping("/ticket/{ticketId}")
    public ResponseEntity<?> updateTicket(
            @PathVariable(value = "ticketId") Long ticketId,
            @RequestBody TicketUpdateRequestDTO ticketUpdateRequestDTO) {

        ticketService.updateTicket(ticketId, ticketUpdateRequestDTO);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }


}
