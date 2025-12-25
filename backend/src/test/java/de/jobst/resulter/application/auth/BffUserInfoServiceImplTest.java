package de.jobst.resulter.application.auth;

import static org.assertj.core.api.Assertions.assertThat;

import de.jobst.resulter.adapter.driver.web.dto.BffUserInfoDto;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

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
        Optional<BffUserInfoDto> result = service.extractUserInfo(authentication);

        // Assert
        assertThat(result).isPresent();
        BffUserInfoDto userInfo = result.get();
        assertThat(userInfo.username()).isEqualTo("testuser");
        assertThat(userInfo.email()).isEqualTo("test@example.com");
        assertThat(userInfo.name()).isEqualTo("Test User");
        assertThat(userInfo.roles()).containsExactly("ADMIN");
        assertThat(userInfo.groups()).containsExactlyInAnyOrder("sales", "marketing");
        assertThat(userInfo.permissions().canAccessAdmin()).isTrue();
    }

    @Test
    void extractUserInfo_withNullAuthentication_returnsEmpty() {
        Optional<BffUserInfoDto> result = service.extractUserInfo(null);
        assertThat(result).isEmpty();
    }

    @Test
    void extractUserInfo_withMultipleRoles_returnsAllRoles() {
        // Arrange
        Map<String, Object> claims =
                Map.of("sub", "user123", "preferred_username", "testuser", "email", "test@example.com");

        OidcIdToken idToken =
                new OidcIdToken("token", Instant.now(), Instant.now().plusSeconds(3600), claims);

        OidcUser oidcUser = new DefaultOidcUser(
                List.of(
                        new SimpleGrantedAuthority("ROLE_ADMIN"),
                        new SimpleGrantedAuthority("ROLE_USER"),
                        new SimpleGrantedAuthority("ROLE_ENDPOINT_ADMIN")),
                idToken);

        OAuth2AuthenticationToken authentication = new OAuth2AuthenticationToken(
                oidcUser,
                List.of(
                        new SimpleGrantedAuthority("ROLE_ADMIN"),
                        new SimpleGrantedAuthority("ROLE_USER"),
                        new SimpleGrantedAuthority("ROLE_ENDPOINT_ADMIN")),
                "keycloak");

        // Act
        Optional<BffUserInfoDto> result = service.extractUserInfo(authentication);

        // Assert
        assertThat(result).isPresent();
        BffUserInfoDto userInfo = result.get();
        assertThat(userInfo.roles()).containsExactlyInAnyOrder("ADMIN", "USER", "ENDPOINT_ADMIN");
        assertThat(userInfo.permissions().canAccessAdmin()).isTrue();
        assertThat(userInfo.permissions().canManageEvents()).isTrue();
    }
}
