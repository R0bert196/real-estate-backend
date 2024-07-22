package com.cleancode.real_estate_backend.repositories;

import com.cleancode.real_estate_backend.entities.TicketMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface TicketMessageRepository extends JpaRepository<TicketMessage, Long> {

    @Query("SELECT t FROM TicketMessage t " +
            "LEFT JOIN FETCH t.imageUrls " +
            "WHERE t.ticket.id = :ticket_id " +
            "ORDER BY t.createTs ASC")
    List<TicketMessage> findAllWithImageUrlsByTicket_Id(Long ticket_id);
}
