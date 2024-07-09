package com.cleancode.real_estate_backend.repositories;

import com.cleancode.real_estate_backend.entities.RentedFloor;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RentedFloorRepository extends JpaRepository<RentedFloor, Long> {

    @EntityGraph(attributePaths = {"floor"})
    List<RentedFloor> findAllWithFloorByTenantId(Long tenantId);
}
