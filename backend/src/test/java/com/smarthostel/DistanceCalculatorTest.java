package com.smarthostel;

import com.smarthostel.util.DistanceCalculator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ============================================================================
 * UNIT TESTS FOR HAVERSINE DISTANCE CALCULATOR & 1KM GEOFENCE VALIDATION
 * ============================================================================
 */
public class DistanceCalculatorTest {

    @Test
    @DisplayName("Should return zero distance for identical coordinates")
    void testZeroDistanceForSameCoordinates() {
        double lat = 10.170600;
        double lon = 76.435700;
        double distance = DistanceCalculator.calculateDistance(lat, lon, lat, lon);
        assertEquals(0.0, distance, 0.01);
    }

    @Test
    @DisplayName("Should correctly calculate distance for close campus proximity")
    void testProximityDistance() {
        // Point A: Adi Shankara Institute Main Campus (10.170600, 76.435700)
        // Point B: Student ~20 meters away (10.170750, 76.435780)
        double distance = DistanceCalculator.calculateDistance(10.170600, 76.435700, 10.170750, 76.435780);
        assertTrue(distance > 15.0 && distance < 25.0, "Expected distance around 15-25 meters, got " + distance);
    }

    @Test
    @DisplayName("Should validate within 1500m (1.5 km) campus radius correctly")
    void testWithin1500MeterRadius() {
        // Within 1.5km geofence
        assertTrue(DistanceCalculator.isWithinRadius(0.0, 1500));
        assertTrue(DistanceCalculator.isWithinRadius(450.5, 1500));
        assertTrue(DistanceCalculator.isWithinRadius(999.9, 1500));
        assertTrue(DistanceCalculator.isWithinRadius(1500.0, 1500));

        // Outside 1.5km geofence
        assertFalse(DistanceCalculator.isWithinRadius(1500.1, 1500));
        assertFalse(DistanceCalculator.isWithinRadius(1850.0, 1500));
        assertFalse(DistanceCalculator.isWithinRadius(2500.0, 1500));
    }
}
