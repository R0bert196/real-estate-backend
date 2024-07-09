package com.cleancode.real_estate_backend.repositories;

import com.cleancode.real_estate_backend.entities.Floor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FloorRepository extends JpaRepository<Floor, Long> {

    List<Floor> findAllByBuilding_Id(Long buildingId);

}
