package com.cleancode.real_estate_backend.repositories;


import com.cleancode.real_estate_backend.entities.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AppUserRepository extends JpaRepository<AppUser, Long> {


    Optional<AppUser> findByEmail(String email);

    Optional<AppUser> findByVerificationCode(String code);

}
