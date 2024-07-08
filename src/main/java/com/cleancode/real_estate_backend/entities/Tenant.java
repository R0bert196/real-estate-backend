package com.cleancode.real_estate_backend.entities;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.proxy.HibernateProxy;

import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Tenant {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

//    un tenant are o lista de cladiri, iar pe fiecare cladire are un pret diferit
//    de mentenanta si chirie
//    iar pe fiecare cladire poate sa aiba etaje diferite
//    iar pe fiecare etaj, suprafata nu e neaparat sa fie toata

    // o pot lua de pe building, floors
//    private Double rentedSquareMeterS;
    private String name;

//    @ManyToMany(mappedBy = "tenants", fetch = FetchType.LAZY)
//    private Set<Building> buildings = new LinkedHashSet<>();

    @OneToMany(mappedBy = "tenant", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<RentedFloor> rentedFloors = new LinkedHashSet<>();

}
