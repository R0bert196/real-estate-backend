package com.cleancode.real_estate_backend.repositories;

import com.cleancode.real_estate_backend.entities.Floor;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FloorRepository extends JpaRepository<Floor, Long> {
}
