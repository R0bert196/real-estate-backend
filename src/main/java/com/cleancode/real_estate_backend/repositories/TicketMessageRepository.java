package com.cleancode.real_estate_backend.repositories;

import com.cleancode.real_estate_backend.entities.TicketMessage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TicketMessageRepository extends JpaRepository<TicketMessage, Long> {
}
