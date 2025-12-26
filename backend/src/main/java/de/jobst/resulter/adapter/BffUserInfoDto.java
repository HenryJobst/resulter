package de.jobst.resulter.adapter;

import de.jobst.resulter.adapter.driver.web.dto.UserPermissionsDto;

import java.util.List;
import java.util.Set;

public record BffUserInfoDto(
        String username,
        String email,
        String name,
        List<String> roles,
        List<String> groups,
        UserPermissionsDto permissions) {
    public static BffUserInfoDto from(
            String username, String email, String name, Set<String> roles, List<String> groups) {
        // Strips ROLE_ prefix for frontend consumption
        List<String> cleanRoles =
                roles.stream().map(role -> role.replace("ROLE_", "")).toList();

        return new BffUserInfoDto(
                username,
                email,
                name,
                cleanRoles,
                groups != null ? groups : List.of(),
                UserPermissionsDto.from(cleanRoles));
    }
}
