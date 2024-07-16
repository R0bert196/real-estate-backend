package com.cleancode.real_estate_backend.services;

import com.cleancode.real_estate_backend.dtos.administrator.ticket.response.TicketResponseDTOView;
import com.cleancode.real_estate_backend.dtos.tenant.ticket.request.TicketRequestDTO;
import com.cleancode.real_estate_backend.dtos.tenant.ticket.response.TicketMessageResponseDTO;
import com.cleancode.real_estate_backend.dtos.tenant.ticket.response.TicketResponseDTO;
import com.cleancode.real_estate_backend.dtos.tenant.ticket.response.TicketResponseDTOLite;
import com.cleancode.real_estate_backend.dtos.user.AppUserResponseDTOLite;
import com.cleancode.real_estate_backend.entities.AppUser;
import com.cleancode.real_estate_backend.entities.RentedFloor;
import com.cleancode.real_estate_backend.entities.Ticket;
import com.cleancode.real_estate_backend.entities.TicketMessage;
import com.cleancode.real_estate_backend.enums.ticket.TicketSeverity;
import com.cleancode.real_estate_backend.repositories.AppUserRepository;
import com.cleancode.real_estate_backend.repositories.RentedFloorRepository;
import com.cleancode.real_estate_backend.repositories.TicketMessageRepository;
import com.cleancode.real_estate_backend.repositories.TicketRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class TicketService {

    private final TicketRepository ticketRepository;
    private final AppUserRepository appUserRepository;
    private final TicketMessageRepository ticketMessageRepository;
    private final RentedFloorRepository rentedFloorRepository;
    private final PhotoService photoService;

    public List<TicketResponseDTOView> getTicketsViewAdministrator(Long administratorId) {
        return ticketRepository.findAllWithCreator().stream().map(this::convertToDTOView).toList();
    }

    public List<TicketResponseDTOView> getTicketsViewTenant(Long tenantId) {
        return ticketRepository.findAllWithCreator().stream().map(this::convertToDTOView).toList();
    }

    private TicketResponseDTOView convertToDTOView(Ticket ticket) {

        return new TicketResponseDTOView(
                ticket.getId(),
                ticket.getSubject(),
                String.valueOf(ticket.getSeverity()),
                String.valueOf(ticket.getStatus()),
                ticket.getCreator().getId(),
                ticket.getCreator().getUsername(),
                String.valueOf(ticket.getDepartment())
        );
    }

//    private Ticket convertToEntity(TicketRequestDTO dto) {
//
//        // Assume current user is the creator
////        AppUser creator = appUserRepository.findCurrentLoggedInUser();
//
//        Ticket entity = Ticket.builder()
//                .subject(dto.subject())
//                .severity(TicketSeverity.valueOf(dto.severity()))
//                .status(TicketStatus.PENDING) // Default status
////                .creator(creator)
//                .imageUrls(dto.imageUrls())
//                .build();
//
//        return entity;
//    }

    @Transactional
    public TicketResponseDTOLite addTicket(TicketRequestDTO ticketRequestDTO) {

        TicketMessage ticketMessage = TicketMessage.builder().message(ticketRequestDTO.message()).build();
//        ticketMessage.setImageUrls(ticketRequestDTO.imageUrls());

        RentedFloor rentedFloor = rentedFloorRepository.findById(ticketRequestDTO.rentedFloorId()).orElseThrow(EntityNotFoundException::new);


        //TODO: replace with actual users
        AppUser creator = AppUser.builder().email("test@test.com").name("robbob").build();
        AppUser manager = AppUser.builder().email("test2@test.com").name("managerRob").build();

        appUserRepository.save(creator);
        appUserRepository.save(manager);

        Ticket ticket = new Ticket();
        ticket.setSubject(ticketRequestDTO.subject());
        ticket.setSeverity(TicketSeverity.valueOf(ticketRequestDTO.severity()));
        ticket.setRentedFloor(rentedFloor);

        //TODO: replace with actual users
        ticket.setCreator(creator);
        ticket.setResponsibleManager(manager);

        Ticket savedTicket = ticketRepository.save(ticket);

        ticketMessage.setTicket(savedTicket);

        TicketMessage savedTicketMessage = ticketMessageRepository.save(ticketMessage);

        return new TicketResponseDTOLite(savedTicket.getId(), savedTicketMessage.getId(), ticket.getSubject());
    }

    public TicketResponseDTO getTicket(Long ticketId) {

        //TODO left join fetch
        Ticket ticket = ticketRepository.findById(ticketId).orElseThrow(EntityNotFoundException::new);

        List<TicketMessage> ticketMessages = ticketMessageRepository.findAllWithImageUrlsByTicket_Id(ticket.getId());

        List<TicketMessageResponseDTO> ticketMessageResponseDTOS = new ArrayList<>();

        ticketMessages.forEach(ticketMessage -> {

            List<byte[]> photos;

            try {
                photos = photoService.getPhotos(ticketMessage.getImageUrls());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            TicketMessageResponseDTO ticketMessageResponseDTO = new TicketMessageResponseDTO(
                    ticketMessage.getId(),
                    ticketMessage.getMessage(),
                    ticketMessage.getCreateTs(),
                    photos
            );

            ticketMessageResponseDTOS.add(ticketMessageResponseDTO);

        });

        AppUserResponseDTOLite creator = new AppUserResponseDTOLite(
                ticket.getCreator().getId(),
                ticket.getCreator().getEmail(),
                ticket.getCreator().getName());


        return new TicketResponseDTO(
                ticket.getId(),
                String.valueOf(ticket.getSeverity()),
                String.valueOf(ticket.getStatus()),
                String.valueOf(ticket.getDepartment()),
                ticket.getSubject(),
                creator,
                ticketMessageResponseDTOS
        );
    }

    public void addPhotosUrlsToMessage(Long ticketMessageId, Set<String> imageUrls) {
        TicketMessage ticketMessage = ticketMessageRepository.findById(ticketMessageId).orElseThrow(EntityNotFoundException::new);
        ticketMessage.setImageUrls(imageUrls);
        ticketMessageRepository.save(ticketMessage);
    }
}
