package com.cleancode.real_estate_backend.repositories;

import com.cleancode.real_estate_backend.entities.Tenant;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TenantRepository extends JpaRepository<Tenant, Long> {
}
