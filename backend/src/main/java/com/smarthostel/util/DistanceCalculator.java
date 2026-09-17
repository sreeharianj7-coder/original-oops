package com.smarthostel.util;

/**
 * ============================================================================
 * DISTANCE CALCULATOR UTILITY (Java OOP Implementation)
 * ============================================================================
 * 
 * Uses the Haversine Formula to compute the great-circle distance (in meters)
 * between two geographic coordinates specified in decimal degrees.
 * 
 * Haversine Formula:
 *   a = sin²(Δφ/2) + cos(φ1) * cos(φ2) * sin²(Δλ/2)
 *   c = 2 * atan2(√a, √(1−a))
 *   d = R * c
 * 
 * Where:
 *   φ is latitude, λ is longitude, R is Earth's mean radius (6,371,000 meters).
 * 
 * OOP Principles Demonstrated:
 *   - Encapsulation: Mathematical constants and radius are private static final.
 *   - Separation of Concerns: Distance mathematics isolated from web and persistence layers.
 *   - Pure Static Utility: No mutable state, thread-safe.
 */
public final class DistanceCalculator {

    /** Earth's mean radius in meters */
    private static final double EARTH_RADIUS_METERS = 6371000.0;

    /** Private constructor to prevent instantiation of utility class */
    private DistanceCalculator() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * Calculates distance between student coordinates and hostel coordinates in meters.
     * 
     * @param lat1 Latitude of first point (e.g. Student Latitude)
     * @param lon1 Longitude of first point (e.g. Student Longitude)
     * @param lat2 Latitude of second point (e.g. Hostel Latitude)
     * @param lon2 Longitude of second point (e.g. Hostel Longitude)
     * @return Distance in meters rounded to two decimal places
     */
    public static double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        // Convert latitude and longitude differences from degrees to radians
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);

        double radLat1 = Math.toRadians(lat1);
        double radLat2 = Math.toRadians(lat2);

        // Apply Haversine formula
        double a = Math.sin(latDistance / 2.0) * Math.sin(latDistance / 2.0)
                + Math.cos(radLat1) * Math.cos(radLat2)
                * Math.sin(lonDistance / 2.0) * Math.sin(lonDistance / 2.0);

        double c = 2.0 * Math.atan2(Math.sqrt(a), Math.sqrt(1.0 - a));

        double distanceInMeters = EARTH_RADIUS_METERS * c;

        // Round to 2 decimal places
        return Math.round(distanceInMeters * 100.0) / 100.0;
    }

    /**
     * Checks if the student's distance is within the permitted radius.
     * 
     * @param distanceInMeters The calculated distance
     * @param allowedRadiusMeters The maximum allowed radius in meters
     * @return true if distance <= allowedRadiusMeters, false otherwise
     */
    public static boolean isWithinRadius(double distanceInMeters, double allowedRadiusMeters) {
        return distanceInMeters <= allowedRadiusMeters;
    }
}
