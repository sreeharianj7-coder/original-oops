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
    @DisplayName("Should validate within 1000m (1 km) campus radius correctly")
    void testWithin1000MeterRadius() {
        // Within 1km geofence
        assertTrue(DistanceCalculator.isWithinRadius(0.0, 1000));
        assertTrue(DistanceCalculator.isWithinRadius(450.5, 1000));
        assertTrue(DistanceCalculator.isWithinRadius(999.9, 1000));
        assertTrue(DistanceCalculator.isWithinRadius(1000.0, 1000));

        // Outside 1km geofence
        assertFalse(DistanceCalculator.isWithinRadius(1000.1, 1000));
        assertFalse(DistanceCalculator.isWithinRadius(1250.0, 1000));
        assertFalse(DistanceCalculator.isWithinRadius(2500.0, 1000));
    }
}
