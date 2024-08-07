package com.cleancode.real_estate_backend.repositories;

import com.cleancode.real_estate_backend.entities.Ticket;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface TicketRepository extends JpaRepository<Ticket, Long> {


    @Query(
            "SELECT t FROM Ticket t " +
            "LEFT JOIN FETCH t.creator " +
            "WHERE t.responsibleManager.id = :managerId")
    List<Ticket> findAllWithCreatorByManagerId(Pageable pageable, Long managerId);

    @Query(
            "SELECT t FROM Ticket t " +
            "LEFT JOIN FETCH t.creator " +
            "WHERE t.rentedFloor.tenant.id IN " +
            "(SELECT tenant.id FROM Tenant tenant " +
            "JOIN tenant.representants r " +
            "WHERE r.id = :representantId)")
    List<Ticket> findAllWithCreatorByRepresentantId(Pageable pageable, Long representantId);


    @Query(
            "SELECT t FROM Ticket t " +
            "LEFT JOIN FETCH t.creator " +
            " WHERE t.id = :id")
    Optional<Ticket> findWithCreatorById(Long id);

    Long countTicketByResponsibleManagerId(Long managerId);

    @Query(
            "SELECT COUNT(t) FROM Ticket t " +
                    "WHERE t.rentedFloor.tenant.id IN " +
                    "(SELECT tenant.id FROM Tenant tenant " +
                    "JOIN tenant.representants r " +
                    "WHERE r.id = :representantId)")
    Long countTicketsByRepresentantId(Long representantId);



}
