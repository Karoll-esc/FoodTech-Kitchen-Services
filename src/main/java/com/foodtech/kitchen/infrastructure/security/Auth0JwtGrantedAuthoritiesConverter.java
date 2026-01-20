package com.foodtech.kitchen.infrastructure.security;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Converts JWT token claims to Spring Security GrantedAuthority objects.
 * 
 * Auth0 stores permissions in a "permissions" array claim in the JWT.
 * This converter extracts those permissions and converts them to Spring Security authorities.
 * 
 * Example JWT claims:
 * {
 *   "permissions": ["read:orders", "create:orders", "update:tasks"],
 *   "sub": "auth0|123456789",
 *   "aud": "https://api.foodtech-kitchen.com"
 * }
 * 
 * These permissions are then used with @PreAuthorize annotations in controllers.
 * 
 * Following Hexagonal Architecture:
 * - This is an infrastructure adapter for security
 * - Converts external Auth0 JWT format to internal Spring Security format
 * - No business logic - pure technical concern
 */
public class Auth0JwtGrantedAuthoritiesConverter implements Converter<Jwt, Collection<GrantedAuthority>> {

    private static final String PERMISSIONS_CLAIM = "permissions";
    private static final String SCOPE_CLAIM = "scope";

    @Override
    public Collection<GrantedAuthority> convert(Jwt jwt) {
        Collection<GrantedAuthority> authorities = extractPermissionsFromClaim(jwt);
        
        // Also extract scope claim (standard OAuth2 claim)
        authorities.addAll(extractScopeFromClaim(jwt));
        
        return authorities;
    }

    /**
     * Extracts permissions from Auth0's custom "permissions" claim.
     * Auth0 adds this claim when using RBAC with API permissions.
     * 
     * @param jwt The JWT token
     * @return Collection of GrantedAuthority objects
     */
    private Collection<GrantedAuthority> extractPermissionsFromClaim(Jwt jwt) {
        Object permissions = jwt.getClaim(PERMISSIONS_CLAIM);
        
        if (permissions instanceof List<?>) {
            List<?> permissionsList = (List<?>) permissions;
            return permissionsList.stream()
                .filter(permission -> permission instanceof String)
                .map(permission -> new SimpleGrantedAuthority((String) permission))
                .collect(Collectors.toList());
        }
        
        return Collections.emptyList();
    }

    /**
     * Extracts scopes from the standard OAuth2 "scope" claim.
     * This claim may contain space-separated scopes.
     * 
     * @param jwt The JWT token
     * @return Collection of GrantedAuthority objects
     */
    private Collection<GrantedAuthority> extractScopeFromClaim(Jwt jwt) {
        String scopeClaim = jwt.getClaimAsString(SCOPE_CLAIM);
        
        if (scopeClaim == null || scopeClaim.trim().isEmpty()) {
            return Collections.emptyList();
        }
        
        // Scopes are space-separated in the JWT
        String[] scopes = scopeClaim.split("\\s+");
        return List.of(scopes).stream()
            .map(scope -> new SimpleGrantedAuthority("SCOPE_" + scope))
            .collect(Collectors.toList());
    }
}
