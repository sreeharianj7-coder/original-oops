package com.smarthostel.dto;

import java.util.List;

public class MonthlyAttendanceResponse {

    private int month;
    private String monthName;
    private int year;
    private long present;
    private long absent;
    private long notMarked;
    private double attendancePercentage;
    private long totalWorkingDays;
    private List<DayAttendanceDTO> days;

    public MonthlyAttendanceResponse() {
    }

    public MonthlyAttendanceResponse(int month, String monthName, int year, long present, 
                                     long absent, long notMarked, double attendancePercentage, 
                                     long totalWorkingDays, List<DayAttendanceDTO> days) {
        this.month = month;
        this.monthName = monthName;
        this.year = year;
        this.present = present;
        this.absent = absent;
        this.notMarked = notMarked;
        this.attendancePercentage = attendancePercentage;
        this.totalWorkingDays = totalWorkingDays;
        this.days = days;
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public String getMonthName() {
        return monthName;
    }

    public void setMonthName(String monthName) {
        this.monthName = monthName;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public long getPresent() {
        return present;
    }

    public void setPresent(long present) {
        this.present = present;
    }

    public long getAbsent() {
        return absent;
    }

    public void setAbsent(long absent) {
        this.absent = absent;
    }

    public long getNotMarked() {
        return notMarked;
    }

    public void setNotMarked(long notMarked) {
        this.notMarked = notMarked;
    }

    public double getAttendancePercentage() {
        return attendancePercentage;
    }

    public void setAttendancePercentage(double attendancePercentage) {
        this.attendancePercentage = attendancePercentage;
    }

    public long getTotalWorkingDays() {
        return totalWorkingDays;
    }

    public void setTotalWorkingDays(long totalWorkingDays) {
        this.totalWorkingDays = totalWorkingDays;
    }

    public List<DayAttendanceDTO> getDays() {
        return days;
    }

    public void setDays(List<DayAttendanceDTO> days) {
        this.days = days;
    }
}
