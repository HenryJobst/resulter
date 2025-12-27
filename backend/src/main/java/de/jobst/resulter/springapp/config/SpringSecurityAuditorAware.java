package de.jobst.resulter.springapp.config;

import org.jspecify.annotations.NonNull;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class SpringSecurityAuditorAware implements AuditorAware<String> {

    public static final String SYSTEM = "System";
    public static final String UNKNOWN = "Unknown";

    @NonNull
    @Override
    public Optional<String> getCurrentAuditor() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return Optional.of(SYSTEM);
        }
        return Optional.of(SecurityContextHolder.getContext())
            .map(SecurityContext::getAuthentication)
            .filter(Authentication::isAuthenticated)
            .map(Authentication::getPrincipal)
            .map(principal -> {
                if (principal instanceof User user) {
                    return user.getUsername();
                } else if (principal instanceof UserDetails details) {
                    return details.getUsername();
                }
                return null; // Oder eine angemessene Handhabung für unbekannte Principal-Typen
            });
    }

}
