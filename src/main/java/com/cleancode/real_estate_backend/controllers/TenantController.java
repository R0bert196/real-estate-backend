package com.cleancode.real_estate_backend.controllers;

import com.cleancode.real_estate_backend.dtos.manager.tenants.response.RentedFloorResponseDTO;
import com.cleancode.real_estate_backend.dtos.manager.ticket.response.TicketResponseDTOView;
import com.cleancode.real_estate_backend.dtos.tenant.ticket.request.TicketMessageRequestDTO;
import com.cleancode.real_estate_backend.dtos.tenant.ticket.request.TicketRequestDTO;
import com.cleancode.real_estate_backend.dtos.tenant.ticket.response.TicketResponseDTO;
import com.cleancode.real_estate_backend.dtos.tenant.ticket.response.TicketResponseDTOLite;
import com.cleancode.real_estate_backend.services.PhotoService;
import com.cleancode.real_estate_backend.services.RentedFloorService;
import com.cleancode.real_estate_backend.services.TenantService;
import com.cleancode.real_estate_backend.services.TicketService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Set;

@RestController
@RequiredArgsConstructor
@Log4j2
@RequestMapping("/api/tenant")
public class TenantController {

    private final TenantService tenantService;
    private final RentedFloorService rentedFloorService;
    private final TicketService ticketService;
    private final PhotoService photoService;

    @GetMapping("/rented-floors")
    public ResponseEntity<?> getRentedFloors() {
        log.info("Fetching rented floors for tenant");
        List<RentedFloorResponseDTO> rentedFloors = rentedFloorService.getTenantRentedFloorsRepresentative();
        return ResponseEntity.ok(rentedFloors);
    }

    @GetMapping("/ticket")
    public ResponseEntity<?> getTickets(
            @RequestParam(name = "pageNumber") Integer pageNumber,
            @RequestParam(name = "numberOfItems") Integer numberOfItems) {
        log.info("Fetching tickets for tenant with pageNumber={}, numberOfItems={}", pageNumber, numberOfItems);
        Pageable pageable = PageRequest.of(pageNumber, numberOfItems);
        List<TicketResponseDTOView> ticketResponseDTOViews = ticketService.getTicketsViewTenant(pageable);
        return ResponseEntity.ok(ticketResponseDTOViews);
    }

    @GetMapping("/ticket/{ticketId}")
    public ResponseEntity<?> getTicket(@PathVariable(value = "ticketId") Long ticketId) {
        log.info("Fetching ticket with ID={}", ticketId);
        TicketResponseDTO dto = ticketService.getTicket(ticketId);
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/ticket/count")
    public ResponseEntity<?> countTickets() {
        log.info("Counting tickets for tenant");
        return ResponseEntity.ok(ticketService.countTickets());
    }

    @PostMapping(path = "/ticket", consumes = "multipart/form-data")
    public ResponseEntity<TicketResponseDTOLite> createTicket(
            @RequestParam("subject") String subject,
            @RequestParam("message") String message,
            @RequestParam("severity") String severity,
            @RequestParam("department") String department,
            @RequestParam("rentedFloorId") Long rentedFloorId,
            @RequestParam(value = "images", required = false) MultipartFile[] images) {

        log.info("Creating a new ticket for subject={}, department={}, severity={}", subject, department, severity);

        TicketRequestDTO ticketRequestDTO = new TicketRequestDTO(subject, message, severity, department, rentedFloorId);
        TicketResponseDTOLite ticketDto = ticketService.addTicket(ticketRequestDTO);

        if (saveImages(images, ticketDto)) {
            log.error("Error saving images for ticket with ID={}", ticketDto.id());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        log.info("Ticket created successfully with ID={}", ticketDto.id());
        return new ResponseEntity<>(ticketDto, HttpStatus.CREATED);
    }

    @PostMapping("/ticket/{ticketId}/message")
    public ResponseEntity<?> addMessageToTicket(
            @PathVariable(value = "ticketId") Long ticketId,
            @RequestParam("message") String message,
            @RequestParam(value = "images", required = false) MultipartFile[] images) {

        log.info("Adding message to ticket with ID={}", ticketId);
        TicketMessageRequestDTO requestDTO = new TicketMessageRequestDTO(message);
        TicketResponseDTOLite ticketDto = ticketService.addMessageToTicket(ticketId, requestDTO);

        if (saveImages(images, ticketDto)) {
            log.error("Error saving images for ticket message with ID={}", ticketDto.ticketMessageId());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        log.info("Message added to ticket with ID={}", ticketId);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    private boolean saveImages(@RequestParam(value = "images", required = false) MultipartFile[] images, TicketResponseDTOLite ticketDto) {
        if (images == null) {
            return false;
        }

        try {
            Set<String> imageUrls = photoService.savePhotos(ticketDto.ticketMessageId(), images);
            ticketService.addPhotosUrlsToMessage(ticketDto.ticketMessageId(), imageUrls);
        } catch (IOException e) {
            log.error("Error saving images for ticket message with ID={}: {}", ticketDto.ticketMessageId(), e.getMessage());
            return true;
        }
        return false;
    }
}
