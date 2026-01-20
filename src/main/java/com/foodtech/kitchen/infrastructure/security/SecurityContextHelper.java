package com.foodtech.kitchen.infrastructure.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Utility class to access the current authenticated user's information from the security context.
 * 
 * Provides convenient methods to extract:
 * - User ID (from JWT 'sub' claim)
 * - User email (from JWT 'email' claim)
 * - User permissions/authorities
 * - Check if user has specific permissions
 * 
 * Architecture Notes:
 * - Infrastructure layer utility
 * - Abstracts Spring Security's SecurityContextHolder
 * - Can be injected into services/controllers to get current user info
 * 
 * Usage Example:
 * {@code
 * @RestController
 * public class OrderController {
 *     private final SecurityContextHelper securityHelper;
 *     
 *     public void createOrder() {
 *         String userId = securityHelper.getCurrentUserId();
 *         logger.info("Order created by user: {}", userId);
 *     }
 * }
 * }
 */
@Component
public class SecurityContextHelper {

    /**
     * Gets the current authenticated user's ID from JWT 'sub' claim.
     * The 'sub' (subject) claim contains the Auth0 user ID.
     * 
     * @return User ID (e.g., "auth0|123456789") or null if not authenticated
     */
    public String getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        
        // For JWT authentication, the principal is the token itself
        if (authentication.getPrincipal() instanceof Jwt jwt) {
            return jwt.getSubject();
        }
        
        return authentication.getName();
    }

    /**
     * Gets the current authenticated user's email from JWT 'email' claim.
     * 
     * @return User email or null if not present/authenticated
     */
    public String getCurrentUserEmail() {
        Jwt jwt = getCurrentJwt();
        if (jwt == null) {
            return null;
        }
        
        return jwt.getClaimAsString("email");
    }

    /**
     * Gets the current JWT token.
     * 
     * @return JWT token or null if not authenticated with JWT
     */
    public Jwt getCurrentJwt() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        
        if (authentication.getPrincipal() instanceof Jwt jwt) {
            return jwt;
        }
        
        return null;
    }

    /**
     * Gets all permissions/authorities of the current user.
     * 
     * @return Set of permission strings (e.g., ["read:orders", "create:orders"])
     */
    public Set<String> getCurrentUserPermissions() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            return Collections.emptySet();
        }
        
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        return authorities.stream()
            .map(GrantedAuthority::getAuthority)
            .collect(Collectors.toSet());
    }

    /**
     * Checks if the current user has a specific permission.
     * 
     * @param permission The permission to check (e.g., "read:orders")
     * @return true if user has the permission, false otherwise
     */
    public boolean hasPermission(String permission) {
        return getCurrentUserPermissions().contains(permission);
    }

    /**
     * Checks if the current user has ALL of the specified permissions.
     * 
     * @param permissions The permissions to check
     * @return true if user has all permissions, false otherwise
     */
    public boolean hasAllPermissions(String... permissions) {
        Set<String> userPermissions = getCurrentUserPermissions();
        for (String permission : permissions) {
            if (!userPermissions.contains(permission)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Checks if the current user has ANY of the specified permissions.
     * 
     * @param permissions The permissions to check
     * @return true if user has at least one permission, false otherwise
     */
    public boolean hasAnyPermission(String... permissions) {
        Set<String> userPermissions = getCurrentUserPermissions();
        for (String permission : permissions) {
            if (userPermissions.contains(permission)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if the user is authenticated.
     * 
     * @return true if authenticated, false otherwise
     */
    public boolean isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.isAuthenticated();
    }
}
