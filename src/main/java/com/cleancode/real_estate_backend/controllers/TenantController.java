package com.cleancode.real_estate_backend.controllers;

import com.cleancode.real_estate_backend.dtos.administrator.tenants.response.RentedFloorResponseDTO;
import com.cleancode.real_estate_backend.dtos.administrator.ticket.response.TicketResponseDTOView;
import com.cleancode.real_estate_backend.dtos.tenant.ticket.request.TicketMessageRequestDTO;
import com.cleancode.real_estate_backend.dtos.tenant.ticket.request.TicketRequestDTO;
import com.cleancode.real_estate_backend.dtos.tenant.ticket.response.TicketResponseDTO;
import com.cleancode.real_estate_backend.dtos.tenant.ticket.response.TicketResponseDTOLite;
import com.cleancode.real_estate_backend.enums.ticket.TicketSeverity;
import com.cleancode.real_estate_backend.services.PhotoService;
import com.cleancode.real_estate_backend.services.RentedFloorService;
import com.cleancode.real_estate_backend.services.TenantService;
import com.cleancode.real_estate_backend.services.TicketService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/tenant")
public class TenantController {

    private final TenantService tenantService;
    private final RentedFloorService rentedFloorService;
    private final TicketService ticketService;
    private final PhotoService photoService;

    @GetMapping("/rented-floors")
    public ResponseEntity<?> getRentedFloors() {

        //todo get rented floors by tenant id
        List<RentedFloorResponseDTO> rentedFloors = rentedFloorService.getRentedFloors();
        return ResponseEntity.ok(rentedFloors);
    }

    @GetMapping("/ticket")
    public ResponseEntity<?> getTickets() {

        List<TicketResponseDTOView> ticketResponseDTOViews = ticketService.getTicketsViewTenant(1L);
        return ResponseEntity.ok(ticketResponseDTOViews);
    }

    @GetMapping("/ticket/{ticketId}")
    public ResponseEntity<?> getTicket(@PathVariable(value = "ticketId") Long ticketId) {

        TicketResponseDTO dto = ticketService.getTicket(ticketId);
        return ResponseEntity.ok(dto);
    }

    @PostMapping(path = "/ticket", consumes = "multipart/form-data")
    public ResponseEntity<TicketResponseDTOLite> createTicket(
            @RequestParam("subject") String subject,
            @RequestParam("message") String message,
            @RequestParam("severity") String severity,
            @RequestParam("department") String department,
            @RequestParam("rentedFloorId") Long rentedFloorId,
            @RequestParam(value = "images", required = false) MultipartFile[] images) {

        //TODO replace with actual user
        Long creatorId = 1L;

        TicketRequestDTO ticketRequestDTO = new TicketRequestDTO(subject, message, severity, department, rentedFloorId);

        TicketResponseDTOLite ticketDto = ticketService.addTicket(ticketRequestDTO);

        if (saveImages(images, ticketDto, creatorId)) return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        return new ResponseEntity<>(ticketDto, HttpStatus.CREATED);
    }


    //TODO: refactor, same code on administrator controller
    @PostMapping("/ticket/{ticketId}/message")
    public ResponseEntity<?> addMessageToTicket(
            @PathVariable(value = "ticketId") Long ticketId,
            @RequestParam("message") String message,
            @RequestParam(value = "images", required = false) MultipartFile[] images
    ) {

        TicketMessageRequestDTO requestDTO = new TicketMessageRequestDTO(message);

        TicketResponseDTOLite ticketDto = ticketService.addMessageToTicket(ticketId, requestDTO);

        //TODO replace with actual user
        Long creatorId = 1L;

        if (saveImages(images, ticketDto, creatorId)) return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    //TODO: refactor, same code on administrator controller
    private boolean saveImages(@RequestParam(value = "images", required = false) MultipartFile[] images, TicketResponseDTOLite ticketDto, Long creatorId) {
        if (images == null) {
            return false;
        }

        try {

            // save the images to the disk
            Set<String> imageUrls = photoService.savePhotos(creatorId, ticketDto.ticketMessageId(), images);

            // save the path to the image into the ticket message
            ticketService.addPhotosUrlsToMessage(ticketDto.ticketMessageId(), imageUrls);
        } catch (IOException e) {
            log.error(e.toString());
            return true;
        }
        return false;
    }
}














