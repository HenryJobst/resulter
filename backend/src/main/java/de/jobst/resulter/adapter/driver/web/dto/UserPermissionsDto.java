package de.jobst.resulter.adapter.driver.web.dto;

import java.util.List;

public record UserPermissionsDto(
        boolean canManageEvents,
        boolean canUploadResults,
        boolean canManageCups,
        boolean canViewReports,
        boolean canManageUsers,
        boolean canAccessAdmin) {
    public static UserPermissionsDto from(List<String> roles, List<String> groups) {
        // Case-insensitive role/group check (Keycloak sends lowercase roles and groups)
        boolean isAdmin = roles.stream().anyMatch(r -> r.equalsIgnoreCase("ADMIN"))
                || groups.stream().anyMatch(g -> g.equalsIgnoreCase("ADMIN"));
        boolean isEndpointAdmin = roles.stream().anyMatch(r -> r.equalsIgnoreCase("ENDPOINT_ADMIN"))
                || groups.stream().anyMatch(g -> g.equalsIgnoreCase("ENDPOINT_ADMIN"));

        return new UserPermissionsDto(
                isAdmin,
                isAdmin,
                isAdmin,
                true, // All authenticated users can view reports
                isAdmin,
                isAdmin || isEndpointAdmin);
    }
}
