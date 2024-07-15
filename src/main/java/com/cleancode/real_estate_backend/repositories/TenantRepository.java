package com.cleancode.real_estate_backend.repositories;

import com.cleancode.real_estate_backend.entities.Tenant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface TenantRepository extends JpaRepository<Tenant, Long> {

    @Query("SELECT DISTINCT t FROM Tenant t " +
            "LEFT JOIN FETCH t.rentedFloors rf " +
            "LEFT JOIN FETCH rf.floor f " +
            "LEFT JOIN FETCH f.building")
    List<Tenant> findAllWithRentedFloorsAndFloorsAndBuilding();


    @Query("SELECT DISTINCT t FROM Tenant t " +
            "LEFT JOIN FETCH t.rentedFloors rf " +
            "LEFT JOIN FETCH rf.floor " +
            "WHERE t.id = :id")
    Optional<Tenant> findWithRentedFloorsAndFloorsById(Long id);
}
