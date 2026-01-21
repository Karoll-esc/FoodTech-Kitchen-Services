package com.foodtech.kitchen.infrastructure.config;

import com.foodtech.kitchen.application.services.StationAuthorizationService;
import com.foodtech.kitchen.domain.model.Station;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.security.oauth2.resource.servlet.OAuth2ResourceServerAutoConfiguration;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

import java.util.Set;

/**
 * Test security configuration that disables authentication for integration tests.
 * 
 * This configuration allows tests to run without requiring valid JWT tokens.
 * All requests are permitted to simplify testing of business logic.
 * 
 * Key Features:
 * - Excludes OAuth2ResourceServerAutoConfiguration to prevent Auth0 JWT validation
 * - Permits all requests without authentication
 * - Disables CSRF for stateless testing
 * - Provides a test StationAuthorizationService that always grants access
 * 
 * Usage:
 * Add @Import(TestSecurityConfig.class) to test classes that need this configuration.
 * Or extend BaseIntegrationTest which already imports this.
 * 
 * Note: For security-specific tests (testing authentication/authorization),
 * use a different configuration that actually validates tokens.
 */
@TestConfiguration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = false)
@EnableAutoConfiguration(exclude = {OAuth2ResourceServerAutoConfiguration.class})
public class TestSecurityConfig {

    /**
     * Security filter chain that permits all requests without authentication.
     * This is marked as @Primary to override the production Auth0SecurityConfig.
     */
    @Bean
    @Primary
    public SecurityFilterChain testSecurityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .anyRequest().permitAll()
            )
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            );
        
        return http.build();
    }

    /**
     * Test implementation of StationAuthorizationService that always grants access.
     * This allows integration tests to focus on business logic rather than authorization.
     * Marked as @Primary to override the production StationAuthorizationService.
     */
    @Bean
    @Primary
    public StationAuthorizationService testStationAuthorizationService() {
        return new StationAuthorizationService() {
            @Override
            public void validateUserCanUpdateTaskAtStation(Set<String> permissions, Station station) {
                // In tests, always grant access - no exception thrown
                // This allows integration tests to focus on business logic
            }
        };
    }
}
