package de.jobst.resulter.application.auth;

import java.util.List;
import java.util.Set;

public record BffUserInfo(
        String username,
        String email,
        String name,
        Set<String> roles,
        List<String> groups) {
}
