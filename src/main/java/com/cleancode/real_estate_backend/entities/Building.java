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

//    @ManyToMany
//    @JoinTable(name = "Building_tenants",
//            joinColumns = @JoinColumn(name = "building_id"),
//            inverseJoinColumns = @JoinColumn(name = "tenants_id"))
//    private Set<Tenant> tenants = new LinkedHashSet<>();


}
