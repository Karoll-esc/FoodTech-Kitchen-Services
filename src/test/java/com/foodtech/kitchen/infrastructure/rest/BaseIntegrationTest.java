package com.foodtech.kitchen.infrastructure.rest;

import com.foodtech.kitchen.infrastructure.config.Auth0SecurityConfig;
import com.foodtech.kitchen.infrastructure.config.TestSecurityConfig;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

/**
 * Base class for integration tests.
 * 
 * Provides common configuration for all integration tests:
 * - Loads full Spring Boot application context (@SpringBootTest)
 * - Excludes Auth0SecurityConfig to prevent Auth0 JWT validation attempts
 * - Configures MockMvc for HTTP request testing (@AutoConfigureMockMvc)
 * - Imports TestSecurityConfig to disable Auth0 security for tests
 * - Uses H2 in-memory database (configured in application-test.yaml)
 * - Activates 'test' profile
 * 
 * Architecture Notes:
 * - All controller integration tests should extend this class
 * - Ensures consistent test configuration across all integration tests
 * - Follows DRY principle - configure once, inherit everywhere
 * 
 * Usage:
 * {@code
 * class OrderControllerIntegrationTest extends BaseIntegrationTest {
 *     @Autowired
 *     private MockMvc mockMvc;
 *     
 *     @Test
 *     void shouldCreateOrder() throws Exception {
 *         mockMvc.perform(post("/api/orders")...)
 *     }
 * }
 * }
 */
@SpringBootTest(
    classes = {com.foodtech.kitchen.KitchenServiceApplication.class},
    properties = {
        "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.security.oauth2.resource.servlet.OAuth2ResourceServerAutoConfiguration"
    }
)
@AutoConfigureMockMvc
@Import(TestSecurityConfig.class)
@ActiveProfiles("test")
public abstract class BaseIntegrationTest {
    
    // Common setup can be added here if needed in the future
    // For example:
    // - Common test data setup
    // - Common utilities
    // - Shared @Autowired dependencies
}
