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

    private String name;


    @OneToMany(mappedBy = "tenant", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<RentedFloor> rentedFloors = new LinkedHashSet<>();

    @OneToMany(mappedBy = "tenantRepresentant", orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<AppUser> representants = new LinkedHashSet<>();

    @ManyToOne
    @JoinColumn(name = "manager_id")
    private AppUser manager;

}
