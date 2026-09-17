/**
 * ============================================================================
 * GEOLOCATION & DISTANCE VALIDATION HANDLER
 * ============================================================================
 */

const GeoLocationHandler = {
  /**
   * Retrieves current position using browser Geolocation API.
   */
  getCurrentPosition() {
    return new Promise((resolve, reject) => {
      if (!navigator.geolocation) {
        reject(new Error('Geolocation is not supported by your browser.'));
        return;
      }

      const options = {
        enableHighAccuracy: true,
        timeout: 15000,
        maximumAge: 0
      };

      navigator.geolocation.getCurrentPosition(
        (position) => {
          resolve({
            latitude: position.coords.latitude,
            longitude: position.coords.longitude,
            accuracy: position.coords.accuracy || 10.0
          });
        },
        (error) => {
          let message = 'An error occurred while fetching your location.';
          switch (error.code) {
            case error.PERMISSION_DENIED:
              message = 'Location permission was denied. Please allow location access in your browser settings to mark attendance.';
              break;
            case error.POSITION_UNAVAILABLE:
              message = 'Location information is currently unavailable. Please check your GPS/Network connection.';
              break;
            case error.TIMEOUT:
              message = 'The request to get your location timed out. Please try again.';
              break;
          }
          reject(new Error(message));
        },
        options
      );
    });
  },

  /**
   * Sends coordinates to Java Backend for authoritative validation.
   */
  async verifyWithBackend(studentId, latitude, longitude, accuracy) {
    try {
      const response = await fetch(`${CONFIG.API_BASE_URL}/attendance/verify-location`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          studentId: studentId,
          latitude: latitude,
          longitude: longitude,
          accuracy: accuracy
        })
      });

      const result = await response.json();
      if (result.data) {
        return result.data;
      }
      return {
        verified: result.success,
        message: result.message,
        distance: result.data ? result.data.distance : -1,
        allowedRadius: 1000
      };
    } catch (err) {
      console.warn('Backend verification API unavailable, performing local Haversine computation fallback', err);
      // Fallback calculation matching DistanceCalculator.java
      const hostel = CONFIG.DEFAULT_HOSTEL;
      const distance = calculateHaversineDistance(latitude, longitude, hostel.latitude, hostel.longitude);
      const isVerified = distance <= hostel.allowedRadius;

      return {
        verified: isVerified,
        distance: distance,
        allowedRadius: hostel.allowedRadius,
        hostelName: hostel.name,
        hostelLatitude: hostel.latitude,
        hostelLongitude: hostel.longitude,
        status: isVerified ? 'VERIFIED' : 'OUTSIDE_HOSTEL',
        message: isVerified
          ? `Location verified successfully. You are ${distance}m from ${hostel.name} (Allowed: ${hostel.allowedRadius}m).`
          : `Location verification failed. You are ${distance}m away, which exceeds the permitted ${hostel.allowedRadius}m radius of ${hostel.name}.`
      };
    }
  }
};
