package com.cleancode.real_estate_backend.repositories;

import com.cleancode.real_estate_backend.entities.TicketMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TicketMessageRepository extends JpaRepository<TicketMessage, Long> {

    List<TicketMessage> findAllByTicket_Id(Long ticket_id);
}
