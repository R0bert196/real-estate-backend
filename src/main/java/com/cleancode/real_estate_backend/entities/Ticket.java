package com.cleancode.real_estate_backend.entities;

import com.cleancode.real_estate_backend.enums.ticket.TicketDepartment;
import com.cleancode.real_estate_backend.enums.ticket.TicketSeverity;
import com.cleancode.real_estate_backend.enums.ticket.TicketStatus;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @Enumerated(EnumType.STRING)
    private TicketSeverity severity;

    @Enumerated(EnumType.STRING)
    private TicketStatus status;

    @Enumerated(EnumType.STRING)
    private TicketDepartment department;

    private String message;

    @ElementCollection
    private Set<String> photoFileNames = new HashSet<>();

    @ManyToOne(optional = false)
    @JoinColumn(name = "creator_id", nullable = false, unique = true)
    private AppUser creator;

    @ManyToOne(optional = false)
    @JoinColumn(name = "responsible_manager_id", nullable = false, unique = true)
    private AppUser responsibleManager;


}
