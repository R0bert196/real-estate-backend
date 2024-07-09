package com.cleancode.real_estate_backend.repositories;

import com.cleancode.real_estate_backend.entities.Tenant;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface TenantRepository extends JpaRepository<Tenant, Long> {

    @Query("SELECT DISTINCT t FROM Tenant t " +
            "LEFT JOIN FETCH t.rentedFloors rf " +
            "LEFT JOIN FETCH rf.floor")
    List<Tenant> findAllWithRentedFloorsAndFloors();
}
