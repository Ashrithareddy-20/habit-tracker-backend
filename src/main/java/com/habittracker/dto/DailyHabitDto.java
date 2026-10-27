package com.habittracker.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class DailyHabitDto {

    private Long id;
    private String name;
    private LocalDateTime createdAt;
    private boolean completed;
    private LocalDate completionDate;
    private Long completionId;
    private int currentStreak;

    public DailyHabitDto() {
    }

    public DailyHabitDto(Long id, String name, LocalDateTime createdAt, boolean completed, LocalDate completionDate, Long completionId, int currentStreak) {
        this.id = id;
        this.name = name;
        this.createdAt = createdAt;
        this.completed = completed;
        this.completionDate = completionDate;
        this.completionId = completionId;
        this.currentStreak = currentStreak;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public LocalDate getCompletionDate() {
        return completionDate;
    }

    public void setCompletionDate(LocalDate completionDate) {
        this.completionDate = completionDate;
    }

    public Long getCompletionId() {
        return completionId;
    }

    public void setCompletionId(Long completionId) {
        this.completionId = completionId;
    }

    public int getCurrentStreak() {
        return currentStreak;
    }

    public void setCurrentStreak(int currentStreak) {
        this.currentStreak = currentStreak;
    }
}
