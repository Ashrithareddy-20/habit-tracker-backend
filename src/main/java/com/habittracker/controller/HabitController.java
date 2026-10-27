package com.habittracker.controller;

import com.habittracker.dto.DailyHabitDto;
import com.habittracker.dto.DashboardStatsDto;
import com.habittracker.dto.HabitRequestDto;
import com.habittracker.dto.HabitResponseDto;
import com.habittracker.service.HabitService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/habits")
@CrossOrigin(origins = "*")
public class HabitController {

    private final HabitService habitService;

    public HabitController(HabitService habitService) {
        this.habitService = habitService;
    }

    /**
     * Add a new habit
     * POST /api/habits
     */
    @PostMapping
    public ResponseEntity<HabitResponseDto> createHabit(@Valid @RequestBody HabitRequestDto requestDto) {
        HabitResponseDto created = habitService.createHabit(requestDto);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    /**
     * Retrieve all saved habits
     * GET /api/habits
     */
    @GetMapping
    public ResponseEntity<List<HabitResponseDto>> getAllHabits() {
        List<HabitResponseDto> habits = habitService.getAllHabits();
        return ResponseEntity.ok(habits);
    }

    /**
     * Retrieve a specific habit by ID
     * GET /api/habits/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<HabitResponseDto> getHabitById(@PathVariable Long id) {
        HabitResponseDto habit = habitService.getHabitById(id);
        return ResponseEntity.ok(habit);
    }

    /**
     * Update an existing habit
     * PUT /api/habits/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<HabitResponseDto> updateHabit(
            @PathVariable Long id,
            @Valid @RequestBody HabitRequestDto requestDto) {
        HabitResponseDto updated = habitService.updateHabit(id, requestDto);
        return ResponseEntity.ok(updated);
    }

    /**
     * Delete a habit by ID
     * DELETE /api/habits/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteHabit(@PathVariable Long id) {
        habitService.deleteHabit(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Retrieve habits and their completion status for a selected date
     * GET /api/habits/daily?date=YYYY-MM-DD
     */
    @GetMapping("/daily")
    public ResponseEntity<List<DailyHabitDto>> getDailyHabits(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        if (date == null) {
            date = LocalDate.now();
        }
        List<DailyHabitDto> dailyHabits = habitService.getDailyHabits(date);
        return ResponseEntity.ok(dailyHabits);
    }

    /**
     * Mark a habit as completed for a selected date
     * PUT /api/habits/{id}/complete?date=YYYY-MM-DD
     */
    @PutMapping("/{id}/complete")
    public ResponseEntity<DailyHabitDto> markHabitCompleted(
            @PathVariable Long id,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        if (date == null) {
            date = LocalDate.now();
        }
        DailyHabitDto completedHabit = habitService.markHabitCompleted(id, date);
        return ResponseEntity.ok(completedHabit);
    }

    /**
     * Unmark / undo completion of a habit for a selected date
     * DELETE /api/habits/{id}/complete?date=YYYY-MM-DD
     */
    @DeleteMapping("/{id}/complete")
    public ResponseEntity<DailyHabitDto> unmarkHabitCompleted(
            @PathVariable Long id,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        if (date == null) {
            date = LocalDate.now();
        }
        DailyHabitDto uncompletedHabit = habitService.unmarkHabitCompleted(id, date);
        return ResponseEntity.ok(uncompletedHabit);
    }

    /**
     * Retrieve dashboard summary statistics for a selected date
     * GET /api/habits/stats?date=YYYY-MM-DD
     */
    @GetMapping("/stats")
    public ResponseEntity<DashboardStatsDto> getDashboardStats(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        if (date == null) {
            date = LocalDate.now();
        }
        DashboardStatsDto stats = habitService.getDashboardStats(date);
        return ResponseEntity.ok(stats);
    }
}
