package com.smarthostel.dto;

import java.time.LocalDate;
import java.time.LocalTime;

public class DayAttendanceDTO {

    private String date;
    private int dayOfMonth;
    private String dayOfWeek;
    private String status; // PRESENT | ABSENT | NOT_MARKED | FUTURE
    private String time;
    private Double distance;
    private String locationStatus;
    private boolean isToday;
    private boolean isFuture;

    public DayAttendanceDTO() {
    }

    public DayAttendanceDTO(String date, int dayOfMonth, String dayOfWeek, String status, 
                            String time, Double distance, String locationStatus, 
                            boolean isToday, boolean isFuture) {
        this.date = date;
        this.dayOfMonth = dayOfMonth;
        this.dayOfWeek = dayOfWeek;
        this.status = status;
        this.time = time;
        this.distance = distance;
        this.locationStatus = locationStatus;
        this.isToday = isToday;
        this.isFuture = isFuture;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public int getDayOfMonth() {
        return dayOfMonth;
    }

    public void setDayOfMonth(int dayOfMonth) {
        this.dayOfMonth = dayOfMonth;
    }

    public String getDayOfWeek() {
        return dayOfWeek;
    }

    public void setDayOfWeek(String dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public Double getDistance() {
        return distance;
    }

    public void setDistance(Double distance) {
        this.distance = distance;
    }

    public String getLocationStatus() {
        return locationStatus;
    }

    public void setLocationStatus(String locationStatus) {
        this.locationStatus = locationStatus;
    }

    public boolean isToday() {
        return isToday;
    }

    public void setToday(boolean today) {
        isToday = today;
    }

    public boolean isFuture() {
        return isFuture;
    }

    public void setFuture(boolean future) {
        isFuture = future;
    }
}
