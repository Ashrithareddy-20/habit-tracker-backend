package com.habittracker.dto;

import java.time.LocalDate;

public class DashboardStatsDto {

    private LocalDate date;
    private int totalHabits;
    private int completedHabits;
    private int pendingHabits;
    private double progressPercentage;

    public DashboardStatsDto() {
    }

    public DashboardStatsDto(LocalDate date, int totalHabits, int completedHabits, int pendingHabits, double progressPercentage) {
        this.date = date;
        this.totalHabits = totalHabits;
        this.completedHabits = completedHabits;
        this.pendingHabits = pendingHabits;
        this.progressPercentage = progressPercentage;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public int getTotalHabits() {
        return totalHabits;
    }

    public void setTotalHabits(int totalHabits) {
        this.totalHabits = totalHabits;
    }

    public int getCompletedHabits() {
        return completedHabits;
    }

    public void setCompletedHabits(int completedHabits) {
        this.completedHabits = completedHabits;
    }

    public int getPendingHabits() {
        return pendingHabits;
    }

    public void setPendingHabits(int pendingHabits) {
        this.pendingHabits = pendingHabits;
    }

    public double getProgressPercentage() {
        return progressPercentage;
    }

    public void setProgressPercentage(double progressPercentage) {
        this.progressPercentage = progressPercentage;
    }
}
