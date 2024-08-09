package com.cleancode.real_estate_backend.repositories;


import com.cleancode.real_estate_backend.entities.AppUser;
import com.cleancode.real_estate_backend.enums.Role;
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

    @Query(
            "SELECT DISTINCT r FROM Ticket t " +
                    "JOIN t.rentedFloor.tenant tenant " +
                    "JOIN tenant.representants r " +
                    "WHERE tenant.id = :tenantId"
    )
    List<AppUser> findTicketTenantRepresentatnsByTenantId(Long tenantId);


    @Query("SELECT u FROM AppUser u WHERE :role MEMBER OF u.role AND EXISTS (SELECT t FROM Ticket t WHERE t.creator = u)")
    List<AppUser> findUsersWithRoleAndIsTicketCreator(Role role);

}
