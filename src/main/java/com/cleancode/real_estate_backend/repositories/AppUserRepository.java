package com.cleancode.real_estate_backend.repositories;


import com.cleancode.real_estate_backend.entities.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface AppUserRepository extends JpaRepository<AppUser, Long> {


    Optional<AppUser> findByEmail(String email);

    Optional<AppUser> findByVerificationCode(String code);


    @Query(
            "SELECT DISTINCT r FROM Ticket t " +
                    "JOIN t.rentedFloor.tenant tenant " +
                    "JOIN tenant.representants r"
    )
    List<AppUser> findTicketTenantRepresentatns();

}
