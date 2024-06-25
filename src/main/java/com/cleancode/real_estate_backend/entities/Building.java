package com.cleancode.real_estate_backend.entities;

import jakarta.persistence.*;
import lombok.*;

import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Building {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    private String name;
    private String address;


    @OneToMany(mappedBy = "building", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Floor> floors = new LinkedHashSet<>();

    @ManyToMany
    @JoinTable(name = "Building_tenants",
            joinColumns = @JoinColumn(name = "building_id"),
            inverseJoinColumns = @JoinColumn(name = "tenants_id"))
    private Set<Tenant> tenants = new LinkedHashSet<>();


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Building building = (Building) o;
        return Objects.equals(id, building.id) && Objects.equals(name, building.name) && Objects.equals(address, building.address) && Objects.equals(floors, building.floors) && Objects.equals(tenants, building.tenants);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, address, floors, tenants);
    }
}
