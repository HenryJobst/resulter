package de.jobst.resulter.springapp.config;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.context.annotation.Profile;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
@Profile("!nosecurity")
public class JwtAuthConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    private static final String REALM_ACCESS_CLAIM = "realm_access";
    private static final String ROLES_CLAIM = "roles";
    public static final String ROLE_PREFIX = "ROLE_";

    private final JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter;

    public JwtAuthConverter() {
        this.jwtGrantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
    }

    public JwtAuthConverter(JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter) {
        this.jwtGrantedAuthoritiesConverter = jwtGrantedAuthoritiesConverter;
    }

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        final Set<GrantedAuthority> authorities =
            Stream.concat(jwtGrantedAuthoritiesConverter.convert(jwt).stream(), extractUserRoles(jwt).stream())
                .collect(Collectors.toSet());
        return new JwtAuthenticationToken(jwt, authorities);
    }

    private Set<? extends GrantedAuthority> extractUserRoles(Jwt jwt) {
        final Map<String, Object> realmAccess = jwt.getClaim(REALM_ACCESS_CLAIM);
        final List<String> realmRoles = (List<String>) realmAccess.get(ROLES_CLAIM);

        if (CollectionUtils.isNotEmpty(realmRoles)) {
            return realmRoles.stream()
                .map(role -> new SimpleGrantedAuthority(ROLE_PREFIX + role.toUpperCase()))
                .collect(Collectors.toSet());
        }

        return Collections.emptySet();
    }
}
