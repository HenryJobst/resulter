package de.jobst.resulter.application.auth;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;

class BffUserInfoServiceImplTest {

    private BffUserInfoServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new BffUserInfoServiceImpl();
    }

    @Test
    void extractUserInfo_withValidOAuth2Authentication_returnsUserInfo() {
        // Arrange
        Map<String, Object> claims = Map.of(
                "sub", "user123",
                "preferred_username", "testuser",
                "email", "test@example.com",
                "name", "Test User",
                "groups", List.of("sales", "marketing"));

        OidcIdToken idToken =
                new OidcIdToken("token", Instant.now(), Instant.now().plusSeconds(3600), claims);

        OidcUser oidcUser = new DefaultOidcUser(List.of(new SimpleGrantedAuthority("ROLE_ADMIN")), idToken);

        OAuth2AuthenticationToken authentication =
                new OAuth2AuthenticationToken(oidcUser, List.of(new SimpleGrantedAuthority("ROLE_ADMIN")), "keycloak");

        // Act
        Optional<BffUserInfo> result = service.extractUserInfo(authentication);

        // Assert
        assertThat(result).isPresent();
        BffUserInfo userInfo = result.get();
        assertThat(userInfo.username()).isEqualTo("testuser");
        assertThat(userInfo.email()).isEqualTo("test@example.com");
        assertThat(userInfo.name()).isEqualTo("Test User");
        assertThat(userInfo.roles()).containsExactlyInAnyOrder("ADMIN");
        assertThat(userInfo.groups()).containsExactlyInAnyOrder("sales", "marketing");
    }

    @Test
    void extractUserInfo_withNullAuthentication_returnsEmpty() {
        Optional<BffUserInfo> result = service.extractUserInfo(null);
        assertThat(result).isEmpty();
    }

    @Test
    void extractUserInfo_withMultipleRoles_returnsAllRoles() {
        // Arrange
        Map<String, Object> claims = Map.of(
                "sub", "user123",
                "preferred_username", "testuser",
                "email", "test@example.com",
                "realm_access", Map.of("roles", List.of("ADMIN", "USER", "ENDPOINT_ADMIN")));

        OidcIdToken idToken =
                new OidcIdToken("token", Instant.now(), Instant.now().plusSeconds(3600), claims);

        OAuth2AuthenticationToken authentication = getOAuth2AuthenticationToken(idToken);

        // Act
        Optional<BffUserInfo> result = service.extractUserInfo(authentication);

        // Assert
        assertThat(result).isPresent();
        BffUserInfo userInfo = result.get();
        assertThat(userInfo.roles()).containsExactlyInAnyOrder("ADMIN", "USER", "ENDPOINT_ADMIN");
    }

    @Test
    void extractUserInfo_withUnauthenticatedToken_returnsEmpty() {
        // 2-Argument-Konstruktor erzeugt ein NICHT authentifiziertes Token
        var notAuthenticated = new UsernamePasswordAuthenticationToken("user", "password");
        assertThat(service.extractUserInfo(notAuthenticated)).isEmpty();
    }

    @Test
    void extractUserInfo_withNonOAuth2Authentication_returnsEmpty() {
        // Authentifiziert, aber kein OAuth2AuthenticationToken
        var authenticated = UsernamePasswordAuthenticationToken.authenticated("user", "password", List.of());
        assertThat(service.extractUserInfo(authenticated)).isEmpty();
    }

    @Test
    void extractUserInfo_withNonOidcOAuth2User_returnsUserInfoFromAuthorities() {
        // DefaultOAuth2User ist kein OidcUser → extractGroups/extractRealmRoles !instanceof-Branch
        Map<String, Object> attributes = Map.of(
                "sub", "user123",
                "preferred_username", "testuser",
                "email", "test@example.com");
        DefaultOAuth2User oauth2User = new DefaultOAuth2User(
                List.of(new SimpleGrantedAuthority("ROLE_VIEWER")), attributes, "sub");
        OAuth2AuthenticationToken token = new OAuth2AuthenticationToken(
                oauth2User, List.of(new SimpleGrantedAuthority("ROLE_VIEWER")), "keycloak");

        Optional<BffUserInfo> result = service.extractUserInfo(token);

        assertThat(result).isPresent();
        assertThat(result.get().username()).isEqualTo("testuser");
        assertThat(result.get().roles()).containsExactly("VIEWER");
        assertThat(result.get().groups()).isEmpty();
    }

    @Test
    void extractUserInfo_withNonListRolesInRealmAccess_fallsBackToAuthorities() {
        // realm_access.roles ist ein String statt eine Liste → rolesList instanceof List<?> → false
        Map<String, Object> claims = Map.of(
                "sub", "user123",
                "preferred_username", "testuser",
                "email", "test@example.com",
                "realm_access", Map.of("roles", "not-a-list"));

        OidcIdToken idToken =
                new OidcIdToken("token", Instant.now(), Instant.now().plusSeconds(3600), claims);
        OidcUser oidcUser = new DefaultOidcUser(List.of(new SimpleGrantedAuthority("ROLE_ADMIN")), idToken);
        OAuth2AuthenticationToken token = new OAuth2AuthenticationToken(
                oidcUser, List.of(new SimpleGrantedAuthority("ROLE_ADMIN")), "keycloak");

        Optional<BffUserInfo> result = service.extractUserInfo(token);

        assertThat(result).isPresent();
        assertThat(result.get().roles()).containsExactly("ADMIN");
    }

    @Test
    void extractUserInfo_withMissingPreferredUsername_usesUnknownFallback() {
        // preferred_username fehlt → username == null → "unknown"
        Map<String, Object> claims = Map.of(
                "sub", "user123",
                "email", "test@example.com");
        OidcIdToken idToken =
                new OidcIdToken("token", Instant.now(), Instant.now().plusSeconds(3600), claims);
        OidcUser oidcUser = new DefaultOidcUser(List.of(new SimpleGrantedAuthority("ROLE_USER")), idToken);
        OAuth2AuthenticationToken token = new OAuth2AuthenticationToken(
                oidcUser, List.of(new SimpleGrantedAuthority("ROLE_USER")), "keycloak");

        Optional<BffUserInfo> result = service.extractUserInfo(token);

        assertThat(result).isPresent();
        assertThat(result.get().username()).isEqualTo("unknown");
    }

    @Test
    void extractUserInfo_withNonRolePrefixAuthority_filtersItOut() {
        // Autorität beginnt nicht mit "ROLE_" → authority.startsWith("ROLE_") → false → wird gefiltert
        Map<String, Object> attributes = Map.of(
                "sub", "user123",
                "preferred_username", "testuser",
                "email", "test@example.com");
        DefaultOAuth2User oauth2User = new DefaultOAuth2User(
                List.of(new SimpleGrantedAuthority("SCOPE_read")), attributes, "sub");
        OAuth2AuthenticationToken token = new OAuth2AuthenticationToken(
                oauth2User, List.of(new SimpleGrantedAuthority("SCOPE_read")), "keycloak");

        Optional<BffUserInfo> result = service.extractUserInfo(token);

        assertThat(result).isPresent();
        assertThat(result.get().roles()).isEmpty();
    }

    @Test
    void extractUserInfo_withNullOAuth2User_returnsEmpty() {
        // oauth2Token.getPrincipal() gibt null zurück → oauth2User == null → empty
        OAuth2AuthenticationToken token = org.mockito.Mockito.mock(OAuth2AuthenticationToken.class);
        org.mockito.Mockito.when(token.isAuthenticated()).thenReturn(true);
        org.mockito.Mockito.when(token.getPrincipal()).thenReturn(null);

        assertThat(service.extractUserInfo(token)).isEmpty();
    }

    @Test
    @SuppressWarnings("unchecked")
    void extractUserInfo_withNullAuthority_filtersItOut() {
        // GrantedAuthority.getAuthority() == null → Objects.nonNull → false → wird gefiltert
        GrantedAuthority nullAuthority = () -> null;
        OAuth2AuthenticationToken token = org.mockito.Mockito.mock(OAuth2AuthenticationToken.class);
        org.springframework.security.oauth2.core.user.OAuth2User mockUser =
                org.mockito.Mockito.mock(org.springframework.security.oauth2.core.user.OAuth2User.class);
        org.mockito.Mockito.when(token.isAuthenticated()).thenReturn(true);
        org.mockito.Mockito.when(token.getPrincipal()).thenReturn(mockUser);
        org.mockito.Mockito.when((java.util.Collection<GrantedAuthority>) mockUser.getAuthorities())
                .thenReturn(List.of(nullAuthority));
        org.mockito.Mockito.when(mockUser.getAttribute("preferred_username")).thenReturn("testuser");
        org.mockito.Mockito.when(mockUser.getAttribute("email")).thenReturn("email@test.de");
        org.mockito.Mockito.when(mockUser.getAttribute("name")).thenReturn("Test User");

        Optional<BffUserInfo> result = service.extractUserInfo(token);

        assertThat(result).isPresent();
        assertThat(result.get().roles()).isEmpty();
    }

    private static @NonNull OAuth2AuthenticationToken getOAuth2AuthenticationToken(OidcIdToken idToken) {
        OidcUser oidcUser = new DefaultOidcUser(
                List.of(
                        new SimpleGrantedAuthority("ROLE_ADMIN"),
                        new SimpleGrantedAuthority("ROLE_USER"),
                        new SimpleGrantedAuthority("ROLE_ENDPOINT_ADMIN")), idToken);

        return new OAuth2AuthenticationToken(
                oidcUser,
                List.of(
                        new SimpleGrantedAuthority("ROLE_ADMIN"),
                        new SimpleGrantedAuthority("ROLE_USER"),
                        new SimpleGrantedAuthority("ROLE_ENDPOINT_ADMIN")),
                "keycloak");
    }
}
