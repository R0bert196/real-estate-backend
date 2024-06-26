package com.cleancode.real_estate_backend.repositories;

import com.cleancode.real_estate_backend.entities.Building;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BuildingRepository extends JpaRepository<Building, Long> {

}
