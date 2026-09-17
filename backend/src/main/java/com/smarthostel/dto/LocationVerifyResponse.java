package com.smarthostel.dto;

public class LocationVerifyResponse {

    private boolean verified;
    private double distance; // calculated distance in meters
    private int allowedRadius; // configured radius in meters
    private String hostelName;
    private double hostelLatitude;
    private double hostelLongitude;
    private String status; // VERIFIED | OUTSIDE_HOSTEL
    private String message;

    public LocationVerifyResponse() {
    }

    public LocationVerifyResponse(boolean verified, double distance, int allowedRadius, 
                                  String hostelName, double hostelLatitude, double hostelLongitude,
                                  String status, String message) {
        this.verified = verified;
        this.distance = distance;
        this.allowedRadius = allowedRadius;
        this.hostelName = hostelName;
        this.hostelLatitude = hostelLatitude;
        this.hostelLongitude = hostelLongitude;
        this.status = status;
        this.message = message;
    }

    public boolean isVerified() {
        return verified;
    }

    public void setVerified(boolean verified) {
        this.verified = verified;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public int getAllowedRadius() {
        return allowedRadius;
    }

    public void setAllowedRadius(int allowedRadius) {
        this.allowedRadius = allowedRadius;
    }

    public String getHostelName() {
        return hostelName;
    }

    public void setHostelName(String hostelName) {
        this.hostelName = hostelName;
    }

    public double getHostelLatitude() {
        return hostelLatitude;
    }

    public void setHostelLatitude(double hostelLatitude) {
        this.hostelLatitude = hostelLatitude;
    }

    public double getHostelLongitude() {
        return hostelLongitude;
    }

    public void setHostelLongitude(double hostelLongitude) {
        this.hostelLongitude = hostelLongitude;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
