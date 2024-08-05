package com.cleancode.real_estate_backend.repositories;

import com.cleancode.real_estate_backend.entities.Building;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface BuildingRepository extends JpaRepository<Building, Long> {


    @Query("SELECT b FROM Building b LEFT JOIN FETCH b.floors WHERE b.manager.id = :id")
    List<Building> findAllWithFloorsByManagerId(Long id);
}
