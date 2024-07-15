package com.cleancode.real_estate_backend.repositories;

import com.cleancode.real_estate_backend.entities.RentedFloor;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface RentedFloorRepository extends JpaRepository<RentedFloor, Long> {

    @EntityGraph(attributePaths = {"floor"})
    List<RentedFloor> findAllWithFloorByTenantId(Long tenantId);

    @Query("SELECT rf FROM RentedFloor rf "+
            "LEFT JOIN FETCH rf.floor f " +
            "LEFT JOIN FETCH f.building b")
    List<RentedFloor> findAllWithFloorAndBuilding();
}
