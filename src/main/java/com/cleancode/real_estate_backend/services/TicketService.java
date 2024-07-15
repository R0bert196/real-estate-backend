package com.cleancode.real_estate_backend.services;

import com.cleancode.real_estate_backend.dtos.administrator.ticket.response.TicketResponseDTOView;
import com.cleancode.real_estate_backend.entities.Ticket;
import com.cleancode.real_estate_backend.repositories.TicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TicketService {

    private final TicketRepository ticketRepository;
    public List<TicketResponseDTOView> getTicketsView() {
        return ticketRepository.findAllWithCreator().stream().map(this::convertToDTOView).toList();
    }

    private TicketResponseDTOView convertToDTOView(Ticket ticket) {

        return new TicketResponseDTOView(
                ticket.getId(),
                ticket.getSeverity().toString(),
                ticket.getStatus().toString(),
                ticket.getCreator().getId(),
                ticket.getCreator().getUsername(),
                ticket.getDepartment().toString()
        );
    }
}
