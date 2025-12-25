package de.jobst.resulter.adapter.driver.web.dto;

import java.util.List;

public record UserPermissionsDto(
        boolean canManageEvents,
        boolean canUploadResults,
        boolean canManageCups,
        boolean canViewReports,
        boolean canManageUsers,
        boolean canAccessAdmin) {
    public static UserPermissionsDto from(List<String> roles) {
        // Case-insensitive role check (Keycloak sends lowercase roles)
        boolean isAdmin = roles.stream().anyMatch(r -> r.equalsIgnoreCase("ADMIN"));
        boolean isEndpointAdmin = roles.stream().anyMatch(r -> r.equalsIgnoreCase("ENDPOINT_ADMIN"));

        return new UserPermissionsDto(
                isAdmin,
                isAdmin,
                isAdmin,
                true, // All authenticated users can view reports
                isAdmin,
                isAdmin || isEndpointAdmin);
    }
}
