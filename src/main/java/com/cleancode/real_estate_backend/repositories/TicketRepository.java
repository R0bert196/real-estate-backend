package com.cleancode.real_estate_backend.repositories;

import com.cleancode.real_estate_backend.entities.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface TicketRepository extends JpaRepository<Ticket, Long> {


    @Query(
    "SELECT t FROM Ticket t " +
    "LEFT JOIN FETCH t.creator")
    List<Ticket> findAllWithCreator();

    @Query("SELECT t FROM Ticket t " +
            "LEFT JOIN FETCH t.creator " +
            " WHERE t.id = :id")
    Optional<Ticket> findWithCreatorById(Long id);
}
