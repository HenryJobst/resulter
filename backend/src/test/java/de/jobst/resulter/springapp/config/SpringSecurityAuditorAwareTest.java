package de.jobst.resulter.springapp.config;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SpringSecurityAuditorAwareTest {

    SpringSecurityAuditorAware auditorAware = new SpringSecurityAuditorAware();

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getCurrentAuditor_returnsSystem_whenNoAuthentication() {
        SecurityContextHolder.clearContext();

        Optional<String> auditor = auditorAware.getCurrentAuditor();

        assertThat(auditor).contains(SpringSecurityAuditorAware.SYSTEM);
    }

    @Test
    void getCurrentAuditor_returnsSystem_whenNotAuthenticated() {
        Authentication auth = mock(Authentication.class);
        when(auth.isAuthenticated()).thenReturn(false);
        SecurityContextHolder.getContext().setAuthentication(auth);

        Optional<String> auditor = auditorAware.getCurrentAuditor();

        assertThat(auditor).contains(SpringSecurityAuditorAware.SYSTEM);
    }

    @Test
    void getCurrentAuditor_returnsUsername_whenUserDetailsAuthenticated() {
        User user = new User("testuser", "pwd", List.of());
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(user, null, List.of());
        SecurityContextHolder.getContext().setAuthentication(auth);

        Optional<String> auditor = auditorAware.getCurrentAuditor();

        assertThat(auditor).contains("testuser");
    }

    @Test
    void getCurrentAuditor_returnsEmpty_whenPrincipalIsUnknownType() {
        Authentication auth = mock(Authentication.class);
        when(auth.isAuthenticated()).thenReturn(true);
        when(auth.getPrincipal()).thenReturn("anonymous");
        SecurityContextHolder.getContext().setAuthentication(auth);

        Optional<String> auditor = auditorAware.getCurrentAuditor();

        assertThat(auditor).isEmpty();
    }
}
