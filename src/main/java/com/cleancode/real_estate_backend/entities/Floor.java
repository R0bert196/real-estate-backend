package com.cleancode.real_estate_backend.entities;

import jakarta.persistence.*;
import lombok.*;

import java.util.Objects;

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

    private Double squareMeter;
    private Integer orderNumber;

    @ManyToOne(optional = false)
    @JoinColumn(name = "building_id", nullable = false)
    private Building building;


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Floor floor = (Floor) o;
        return Objects.equals(id, floor.id) && Objects.equals(squareMeter, floor.squareMeter) && Objects.equals(orderNumber, floor.orderNumber) && Objects.equals(building, floor.building);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, squareMeter, orderNumber, building);
    }
}
