package com.habittracker.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class HabitRequestDto {

    @NotBlank(message = "Habit name cannot be empty or contain only whitespace")
    @Size(min = 2, max = 100, message = "Habit name must be between 2 and 100 characters")
    private String name;

    public HabitRequestDto() {
    }

    public HabitRequestDto(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
