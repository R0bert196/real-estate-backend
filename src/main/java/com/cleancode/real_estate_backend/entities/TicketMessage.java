package com.cleancode.real_estate_backend.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TicketMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;


    @Column(nullable = false)
    private String message;

    @Temporal(TemporalType.TIMESTAMP)
    private Long createTs;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_id")
    private Ticket ticket;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "app_user_id")
    private AppUser creator;

    @ElementCollection
    private Set<String> imageUrls = new HashSet<>();

    @PrePersist
    private void prePersist() {
        this.createTs = Instant.now().toEpochMilli();
    }
}
