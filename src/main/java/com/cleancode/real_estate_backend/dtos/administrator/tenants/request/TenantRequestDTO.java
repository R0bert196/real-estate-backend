package com.cleancode.real_estate_backend.dtos.administrator.tenants.request;

import java.util.List;

public record TenantRequestDTO(String tenantName, List<TenantSelectedBuildingsRequestDTO> buildings) {
}
