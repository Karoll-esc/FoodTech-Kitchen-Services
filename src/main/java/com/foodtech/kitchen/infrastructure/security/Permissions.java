package com.foodtech.kitchen.infrastructure.security;

/**
 * Constants for Auth0 permissions used throughout the application.
 * 
 * These permission strings must match exactly with the permissions
 * defined in Auth0 dashboard (API → Permissions tab).
 * 
 * Usage with @PreAuthorize:
 * {@code
 * @PreAuthorize("hasAuthority('" + Permissions.CREATE_ORDERS + "')")
 * public void createOrder() { ... }
 * }
 * 
 * Or using SecurityContextHelper:
 * {@code
 * if (securityHelper.hasPermission(Permissions.UPDATE_TASKS_BAR)) {
 *     // Allow update
 * }
 * }
 */
public final class Permissions {

    // Order permissions
    public static final String READ_ORDERS = "read:orders";
    public static final String CREATE_ORDERS = "create:orders";

    // Task permissions
    public static final String READ_TASKS = "read:tasks";
    public static final String UPDATE_TASKS_BAR = "update:tasks:bar";
    public static final String UPDATE_TASKS_HOT_KITCHEN = "update:tasks:hot-kitchen";
    public static final String UPDATE_TASKS_COLD_KITCHEN = "update:tasks:cold-kitchen";

    // Table permissions (HU-007)
    public static final String UPDATE_TABLES = "update:tables";

    // Admin permissions
    public static final String ADMIN_ALL = "admin:all";

    // Private constructor to prevent instantiation
    private Permissions() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    /**
     * Helper method to check if a permission is for updating tasks at a specific station.
     * 
     * @param permission The permission to check
     * @return true if it's a task update permission, false otherwise
     */
    public static boolean isTaskUpdatePermission(String permission) {
        return UPDATE_TASKS_BAR.equals(permission)
            || UPDATE_TASKS_HOT_KITCHEN.equals(permission)
            || UPDATE_TASKS_COLD_KITCHEN.equals(permission);
    }

    /**
     * Gets the task update permission for a specific station.
     * 
     * @param station The station name (BAR, HOT_KITCHEN, COLD_KITCHEN)
     * @return The corresponding permission string
     * @throws IllegalArgumentException if station is not recognized
     */
    public static String getUpdatePermissionForStation(String station) {
        return switch (station) {
            case "BAR" -> UPDATE_TASKS_BAR;
            case "HOT_KITCHEN" -> UPDATE_TASKS_HOT_KITCHEN;
            case "COLD_KITCHEN" -> UPDATE_TASKS_COLD_KITCHEN;
            default -> throw new IllegalArgumentException("Unknown station: " + station);
        };
    }
}
