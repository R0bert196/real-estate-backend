package com.cleancode.real_estate_backend.services;

import com.cleancode.real_estate_backend.dtos.administrator.ticket.response.TicketResponseDTOView;
import com.cleancode.real_estate_backend.dtos.tenant.ticket.request.TicketRequestDTO;
import com.cleancode.real_estate_backend.dtos.tenant.ticket.response.TicketResponseDTOLite;
import com.cleancode.real_estate_backend.entities.AppUser;
import com.cleancode.real_estate_backend.entities.Ticket;
import com.cleancode.real_estate_backend.entities.TicketMessage;
import com.cleancode.real_estate_backend.enums.ticket.TicketSeverity;
import com.cleancode.real_estate_backend.enums.ticket.TicketStatus;
import com.cleancode.real_estate_backend.repositories.AppUserRepository;
import com.cleancode.real_estate_backend.repositories.TicketMessageRepository;
import com.cleancode.real_estate_backend.repositories.TicketRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TicketService {

    private final TicketRepository ticketRepository;
    private final AppUserRepository appUserRepository;
    private final TicketMessageRepository ticketMessageRepository;

    public List<TicketResponseDTOView> getTicketsView() {
        return ticketRepository.findAllWithCreator().stream().map(this::convertToDTOView).toList();
    }

    private TicketResponseDTOView convertToDTOView(Ticket ticket) {

        return new TicketResponseDTOView(
                ticket.getId(),
                ticket.getSubject(),
                ticket.getSeverity().toString(),
                ticket.getStatus().toString(),
                ticket.getCreator().getId(),
                ticket.getCreator().getUsername(),
                ticket.getDepartment().toString()
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


        //TODO: replace with actual users
        AppUser creator = AppUser.builder().email("test@test.com").name("robbob").build();
        AppUser manager = AppUser.builder().email("test2@test.com").name("managerRob").build();

        appUserRepository.save(creator);
        appUserRepository.save(manager);

        Ticket ticket = new Ticket();
        ticket.setSubject(ticketRequestDTO.subject());
        ticket.setSeverity(TicketSeverity.valueOf(ticketRequestDTO.severity()));
        ticket.setImageUrls(ticketRequestDTO.imageUrls());

        //TODO: replace with actual users
        ticket.setCreator(creator);
        ticket.setResponsibleManager(manager);

        Ticket savedTicket = ticketRepository.save(ticket);

        ticketMessage.setTicket(savedTicket);

        ticketMessageRepository.save(ticketMessage);

        return new TicketResponseDTOLite(ticket.getId(), ticket.getSubject());
    }
}
