package de.jobst.resulter.adapter.driver.web;

import static org.assertj.core.api.Assertions.assertThat;

import de.jobst.resulter.adapter.driver.web.dto.BffUserInfoDto;
import de.jobst.resulter.application.auth.BffUserInfoService;
import de.jobst.resulter.application.auth.BffUserInfoServiceImpl;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;

class BffControllerIntegrationTest {

    private BffUserInfoService bffUserInfoService;
    private BffController bffController;

    @BeforeEach
    void setUp() {
        bffUserInfoService = new BffUserInfoServiceImpl();
        bffController = new BffController(bffUserInfoService);
    }

    @Test
    void getUserInfo_withOAuth2Authentication_returnsUserInfo() {
        // Arrange
        OAuth2User oauth2User = new DefaultOAuth2User(
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN")),
                java.util.Map.of("preferred_username", "testuser", "email", "test@example.com", "name", "Test User"),
                "preferred_username");

        Authentication authentication = new OAuth2AuthenticationToken(
                oauth2User, List.of(new SimpleGrantedAuthority("ROLE_ADMIN")), "keycloak");

        // Act
        ResponseEntity<BffUserInfoDto> response = bffController.getUserInfo(authentication);

        // Assert
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().username()).isEqualTo("testuser");
        assertThat(response.getBody().email()).isEqualTo("test@example.com");
        assertThat(response.getBody().roles()).contains("ADMIN");
        assertThat(response.getBody().permissions().canAccessAdmin()).isTrue();
    }

    @Test
    void getUserInfo_withoutAuthentication_returnsUnauthorized() {
        // Act
        ResponseEntity<BffUserInfoDto> response = bffController.getUserInfo(null);

        // Assert
        assertThat(response.getStatusCode().value()).isEqualTo(401);
    }

    @Test
    void getCsrfToken_returnsOk() {
        // Act
        ResponseEntity<Void> response = bffController.getCsrfToken();

        // Assert
        assertThat(response.getStatusCode().value()).isEqualTo(200);
    }

    @Test
    void bffUserInfoService_extractsRolesCorrectly() {
        // Arrange
        OAuth2User oauth2User = new DefaultOAuth2User(
                List.of(new SimpleGrantedAuthority("ROLE_USER")),
                java.util.Map.of("preferred_username", "normaluser", "email", "normal@example.com"),
                "preferred_username");

        Authentication authentication =
                new OAuth2AuthenticationToken(oauth2User, List.of(new SimpleGrantedAuthority("ROLE_USER")), "keycloak");

        // Act
        Optional<BffUserInfoDto> result = bffUserInfoService.extractUserInfo(authentication);

        // Assert
        assertThat(result).isPresent();
        BffUserInfoDto userInfo = result.get();
        assertThat(userInfo.roles()).contains("USER");
        assertThat(userInfo.permissions().canManageEvents()).isFalse(); // USER role can't manage events
        assertThat(userInfo.permissions().canViewReports()).isTrue(); // All users can view reports
    }
}
