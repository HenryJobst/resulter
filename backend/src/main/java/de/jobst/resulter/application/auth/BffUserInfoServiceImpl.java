package de.jobst.resulter.application.auth;

import java.util.*;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class BffUserInfoServiceImpl implements BffUserInfoService {

    @Override
    public Optional<BffUserInfo> extractUserInfo(@Nullable Authentication authentication) {

        if (authentication == null
                || !authentication.isAuthenticated()
                || !(authentication instanceof OAuth2AuthenticationToken oauth2Token)) {
            return Optional.empty();
        }

        OAuth2User oauth2User = oauth2Token.getPrincipal();

        if (oauth2User != null) {
            String username = oauth2User.getAttribute("preferred_username");

            String email = oauth2User.getAttribute("email");
            String name = oauth2User.getAttribute("name");

            // Extract Keycloak roles from realm_access.roles claim
            Set<String> roles = extractRealmRoles(oauth2User);

            List<String> groups = extractGroups(oauth2User);

            BffUserInfo userInfo = new BffUserInfo(username != null ? username : "unknown", email, name, roles, groups);

            log.debug("Extracted user info for: {}", username);

            return Optional.of(userInfo);
        }
        return Optional.empty();
    }

    @SuppressWarnings("unchecked")
    private List<String> extractGroups(OAuth2User oauth2User) {
        if (oauth2User instanceof OidcUser oidcUser) {
            Object groupsClaim = oidcUser.getClaim("groups");
            if (groupsClaim instanceof List<?>) {
                return ((List<String>) groupsClaim);
            }
        }
        return List.of();
    }

    @SuppressWarnings("unchecked")
    private Set<String> extractRealmRoles(OAuth2User oauth2User) {
        Set<String> roles = Set.of();

        // Try to extract roles from realm_access.roles claim (OIDC)
        if (oauth2User instanceof OidcUser oidcUser) {
            Object realmAccess = oidcUser.getClaim("realm_access");
            if (realmAccess instanceof java.util.Map<?, ?> realmAccessMap) {
                Object rolesList = realmAccessMap.get("roles");
                if (rolesList instanceof List<?>) {
                    roles = new HashSet<>(((List<String>) rolesList));
                }
            }
        }

        // Fallback: Extract roles from granted authorities
        if (roles.isEmpty()) {
            roles = oauth2User.getAuthorities().stream().map(GrantedAuthority::getAuthority)
                    .filter(authority -> Objects.nonNull(authority) && authority.startsWith("ROLE_"))
                    .map(authority -> authority.substring(5)) // Remove "ROLE_" prefix
                    .collect(Collectors.toSet());
        }

        return roles;
    }
}
