package com.foodtech.kitchen.infrastructure.config;

import com.foodtech.kitchen.infrastructure.security.Auth0JwtGrantedAuthoritiesConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtDecoders;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * Security configuration for Auth0 JWT token validation and RBAC.
 * 
 * This configuration:
 * 1. Validates JWT tokens issued by Auth0
 * 2. Extracts permissions from JWT claims and converts to Spring Security authorities
 * 3. Enforces role-based access control on API endpoints
 * 4. Defines public vs protected endpoints
 * 5. Configures CORS for SPA frontend
 * 
 * Architecture Notes:
 * - Infrastructure layer: Adapts Auth0 security to Spring Security
 * - No business logic: Pure security configuration
 * - @EnableMethodSecurity allows @PreAuthorize annotations in controllers
 * 
 * Roles and Permissions:
 * - admin: Full access (admin:all)
 * - bar-staff: View all tasks, update BAR station tasks
 * - hot-kitchen-staff: View all tasks, update HOT_KITCHEN station tasks  
 * - cold-kitchen-staff: View all tasks, update COLD_KITCHEN station tasks
 * - waiter: Create orders, view order status
 * 
 * Note: This configuration is disabled when running with 'test' or 'authorization-test' profiles.
 * - 'test' profile uses TestSecurityConfig (security disabled for business logic tests)
 * - 'authorization-test' profile uses AuthorizationTestSecurityConfig (method security enabled with @WithMockUser)
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@Profile("!test & !authorization-test")
public class Auth0SecurityConfig {

    @Value("${auth0.audience}")
    private String audience;

    @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}")
    private String issuer;

    /**
     * Main security filter chain.
     * Configures JWT authentication, authorization rules, and CORS.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // Disable CSRF for stateless JWT authentication
            // JWT tokens are not stored in cookies, so CSRF protection is not needed
            .csrf(csrf -> csrf.disable())
            
            // Configure CORS for SPA frontend
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            
            // Configure authorization rules
            .authorizeHttpRequests(auth -> auth
                // Public endpoints - no authentication required
                .requestMatchers(HttpMethod.GET, "/actuator/health").permitAll()
                .requestMatchers(HttpMethod.GET, "/actuator/info").permitAll()
                
                // All other /api/** endpoints require authentication
                // Fine-grained authorization is handled by @PreAuthorize in controllers
                .requestMatchers("/api/**").authenticated()
                
                // Deny all other requests by default
                .anyRequest().denyAll()
            )
            
            // Configure JWT authentication
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt
                    .decoder(jwtDecoder())
                    .jwtAuthenticationConverter(jwtAuthenticationConverter())
                )
            )
            
            // Stateless session management (no server-side sessions)
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            );
        
        return http.build();
    }

    /**
     * JWT decoder that validates tokens from Auth0.
     * Validates:
     * - Token signature (RS256)
     * - Issuer (Auth0 tenant)
     * - Audience (this API)
     * - Expiration time
     */
    @Bean
    public JwtDecoder jwtDecoder() {
        NimbusJwtDecoder jwtDecoder = JwtDecoders.fromOidcIssuerLocation(issuer);
        
        OAuth2TokenValidator<Jwt> audienceValidator = new AudienceValidator(audience);
        OAuth2TokenValidator<Jwt> withIssuer = JwtValidators.createDefaultWithIssuer(issuer);
        OAuth2TokenValidator<Jwt> withAudience = new DelegatingOAuth2TokenValidator<>(withIssuer, audienceValidator);
        
        jwtDecoder.setJwtValidator(withAudience);
        
        return jwtDecoder;
    }

    /**
     * Converts JWT claims to Spring Security Authentication.
     * Extracts permissions from Auth0 JWT and converts to GrantedAuthority objects.
     */
    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(new Auth0JwtGrantedAuthoritiesConverter());
        return converter;
    }

    /**
     * CORS configuration for SPA frontend.
     * 
     * Development: Allows all origins
     * Production: Should restrict to specific frontend domains
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // TODO: In production, replace with specific frontend URLs
        configuration.setAllowedOriginPatterns(List.of("*"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        
        return source;
    }

    /**
     * Custom validator to check JWT audience claim.
     * Ensures the token was issued for this specific API.
     */
    private static class AudienceValidator implements OAuth2TokenValidator<Jwt> {
        private final String audience;

        AudienceValidator(String audience) {
            this.audience = audience;
        }

        @Override
        public org.springframework.security.oauth2.core.OAuth2TokenValidatorResult validate(Jwt jwt) {
            if (jwt.getAudience().contains(audience)) {
                return org.springframework.security.oauth2.core.OAuth2TokenValidatorResult.success();
            }
            
            return org.springframework.security.oauth2.core.OAuth2TokenValidatorResult.failure(
                new org.springframework.security.oauth2.core.OAuth2Error(
                    "invalid_token",
                    "The required audience is missing",
                    null
                )
            );
        }
    }
}
