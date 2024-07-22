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

    @Transient
    private Double originalSize; // Temporary field to store the original size

    @PrePersist
    public void initAvailableSize() {
        this.availableSize = this.size;
    }

    @PostLoad
    public void storeOriginalSize() {
        this.originalSize = this.size;
    }

    @PreUpdate
    public void updateAvailableSize() {
        if (this.size != null && this.originalSize != null) {
            double sizeDifference = this.size - this.originalSize;
            this.availableSize += sizeDifference;
            this.originalSize = this.size; // Update originalSize to current size after adjustment
        }
    }

    public void rentFloor(Double previousRentedSize, Double newRentedSize) {
        // Add back the previous rented size to availableSize
        this.availableSize += previousRentedSize;

        // Check if the new rented size can be accommodated
        if (newRentedSize <= this.availableSize) {
            this.availableSize -= newRentedSize;
        } else {
            throw new RuntimeException("Not enough available space on this floor.");
        }
    }
}
