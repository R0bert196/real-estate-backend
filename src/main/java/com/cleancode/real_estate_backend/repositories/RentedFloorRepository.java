package com.cleancode.real_estate_backend.repositories;

import com.cleancode.real_estate_backend.entities.RentedFloor;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RentedFloorRepository extends JpaRepository<RentedFloor, Long> {
}
