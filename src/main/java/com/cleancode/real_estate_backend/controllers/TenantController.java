package com.cleancode.real_estate_backend.controllers;

import com.cleancode.real_estate_backend.dtos.administrator.tenants.response.RentedFloorResponseDTO;
import com.cleancode.real_estate_backend.dtos.administrator.ticket.response.TicketResponseDTOView;
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
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
        List<RentedFloorResponseDTO> rentedFloors =  rentedFloorService.getRentedFloors();
        return ResponseEntity.ok(rentedFloors);
    }

    @GetMapping("/ticket")
    public ResponseEntity<?> getTickets(
            @RequestParam (name = "pageNumber", required = true) Integer pageNumber,
            @RequestParam (name = "numberOfItems", required = true) Integer numberOfItems
    ) {
        Pageable pageable= PageRequest.of(pageNumber, numberOfItems);
        List<TicketResponseDTOView> ticketResponseDTOViews = ticketService.getTicketsViewTenant(1L, pageable);
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

    //severity : TicketSeverity
    //rentedFloorId : foreignKey
    @PostMapping(path = "/ticket",  consumes = "multipart/form-data")
    public ResponseEntity<TicketResponseDTOLite> createTicket(
            @RequestParam("subject") String subject,
            @RequestParam("message") String message,
            @RequestParam("severity") String severity,
            @RequestParam("rentedFloorId") Long rentedFloorId,
            @RequestParam(name = "images", required = false) MultipartFile[] images) {

        // Replace these with actual ID
        Long creatorId = 2L;

        TicketRequestDTO ticketRequestDTO = new TicketRequestDTO(subject, message, severity, rentedFloorId);

        TicketResponseDTOLite ticketResponse = ticketService.addTicket(ticketRequestDTO);
//
//        try {
//
//            // save the images to the disk
//            Set<String> imageUrls = photoService.savePhotos(creatorId, ticketResponse.ticketMessageId(), images);
//
//            // save the path to the image into the ticket message
//            ticketService.addPhotosUrlsToMessage(ticketResponse.ticketMessageId(), imageUrls);
//        } catch (IOException e) {
//            log.error(e.toString());
//            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
//        }
        return new ResponseEntity<>(ticketResponse, HttpStatus.CREATED);
    }
}
