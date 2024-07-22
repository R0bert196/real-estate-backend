package com.cleancode.real_estate_backend.repositories;

import com.cleancode.real_estate_backend.entities.Ticket;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface TicketRepository extends JpaRepository<Ticket, Long> {


    @Query(
    "SELECT t FROM Ticket t " +
    "LEFT JOIN FETCH t.creator")
    List<Ticket> findAllWithCreator(Pageable pageable);

    Long countTicketsBy();
}
