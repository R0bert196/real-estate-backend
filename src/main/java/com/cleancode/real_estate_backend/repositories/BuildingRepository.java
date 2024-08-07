package com.cleancode.real_estate_backend.repositories;

import com.cleancode.real_estate_backend.entities.Building;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface BuildingRepository extends JpaRepository<Building, Long> {


    @Query("SELECT b FROM Building b LEFT JOIN FETCH b.floors WHERE b.manager.id = :id")
    List<Building> findAllWithFloorsByManagerId(Long id);


    @Query("SELECT b FROM Building b LEFT JOIN FETCH b.manager LEFT JOIN FETCH b.floors WHERE b.id = :id")
    Optional<Building> findWithManagerAndFloorsById(Long id);

    @Query("SELECT b FROM Building b LEFT JOIN FETCH b.manager WHERE b.id = :id")
    Optional<Building> findByIdWithManager(Long id);
}
