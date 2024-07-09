package com.cleancode.real_estate_backend.entities;

import jakarta.persistence.*;
import lombok.*;

import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Floor {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    private Double size;
    private Double availableSize;
    private Integer floorNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "building_id", nullable = false)
    private Building building;

    @OneToMany(mappedBy = "floor", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<RentedFloor> rentedFloors = new LinkedHashSet<>();

    @PrePersist
    public void initAvailableSize() {
        this.availableSize = this.size;
    }

    public void rentFloor(Double rentedSize) {
        if (rentedSize <= this.availableSize) {
            this.availableSize -= rentedSize;
        } else {
            throw new RuntimeException("Not enough available space on this floor.");
        }
    }
}
