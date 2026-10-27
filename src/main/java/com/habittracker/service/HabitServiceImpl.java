package com.habittracker.service;

import com.habittracker.dto.DailyHabitDto;
import com.habittracker.dto.DashboardStatsDto;
import com.habittracker.dto.HabitRequestDto;
import com.habittracker.dto.HabitResponseDto;
import com.habittracker.exception.BadRequestException;
import com.habittracker.exception.DuplicateRecordException;
import com.habittracker.exception.ResourceNotFoundException;
import com.habittracker.model.Habit;
import com.habittracker.model.HabitCompletion;
import com.habittracker.repository.HabitCompletionRepository;
import com.habittracker.repository.HabitRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class HabitServiceImpl implements HabitService {

    private final HabitRepository habitRepository;
    private final HabitCompletionRepository habitCompletionRepository;

    public HabitServiceImpl(HabitRepository habitRepository, HabitCompletionRepository habitCompletionRepository) {
        this.habitRepository = habitRepository;
        this.habitCompletionRepository = habitCompletionRepository;
    }

    @Override
    public HabitResponseDto createHabit(HabitRequestDto requestDto) {
        if (requestDto == null || requestDto.getName() == null || requestDto.getName().trim().isEmpty()) {
            throw new BadRequestException("Habit name cannot be empty or contain only whitespace");
        }

        String trimmedName = requestDto.getName().trim();
        if (habitRepository.existsByNameIgnoreCase(trimmedName)) {
            throw new DuplicateRecordException("A habit with name '" + trimmedName + "' already exists");
        }

        Habit habit = new Habit(trimmedName);
        Habit savedHabit = habitRepository.save(habit);
        return mapToHabitResponseDto(savedHabit);
    }

    @Override
    @Transactional(readOnly = true)
    public List<HabitResponseDto> getAllHabits() {
        return habitRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(this::mapToHabitResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public HabitResponseDto getHabitById(Long id) {
        Habit habit = habitRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Habit not found with id: " + id));
        return mapToHabitResponseDto(habit);
    }

    @Override
    public HabitResponseDto updateHabit(Long id, HabitRequestDto requestDto) {
        if (requestDto == null || requestDto.getName() == null || requestDto.getName().trim().isEmpty()) {
            throw new BadRequestException("Habit name cannot be empty or contain only whitespace");
        }

        Habit habit = habitRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Habit not found with id: " + id));

        String trimmedName = requestDto.getName().trim();
        if (habitRepository.existsByNameIgnoreCaseAndIdNot(trimmedName, id)) {
            throw new DuplicateRecordException("Another habit with name '" + trimmedName + "' already exists");
        }

        habit.setName(trimmedName);
        Habit updatedHabit = habitRepository.save(habit);
        return mapToHabitResponseDto(updatedHabit);
    }

    @Override
    public void deleteHabit(Long id) {
        Habit habit = habitRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Habit not found with id: " + id));
        habitRepository.delete(habit);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DailyHabitDto> getDailyHabits(LocalDate date) {
        if (date == null) {
            date = LocalDate.now();
        }

        List<Habit> habits = habitRepository.findAllByOrderByCreatedAtDesc();
        List<HabitCompletion> completions = habitCompletionRepository.findByCompletionDate(date);

        Map<Long, HabitCompletion> completionMap = completions.stream()
                .collect(Collectors.toMap(c -> c.getHabit().getId(), c -> c, (existing, replacement) -> existing));

        LocalDate queryDate = date;
        return habits.stream().map(habit -> {
            HabitCompletion comp = completionMap.get(habit.getId());
            boolean isCompleted = comp != null && comp.isCompleted();
            Long completionId = comp != null ? comp.getId() : null;
            int streak = calculateCurrentStreak(habit.getId(), queryDate);

            return new DailyHabitDto(
                    habit.getId(),
                    habit.getName(),
                    habit.getCreatedAt(),
                    isCompleted,
                    queryDate,
                    completionId,
                    streak
            );
        }).collect(Collectors.toList());
    }

    @Override
    public DailyHabitDto markHabitCompleted(Long id, LocalDate date) {
        if (date == null) {
            date = LocalDate.now();
        }

        Habit habit = habitRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Habit not found with id: " + id));

        Optional<HabitCompletion> existingOpt = habitCompletionRepository.findByHabitIdAndCompletionDate(id, date);
        if (existingOpt.isPresent()) {
            throw new DuplicateRecordException("Habit with id " + id + " is already marked as completed for date " + date);
        }

        HabitCompletion newCompletion = new HabitCompletion(habit, date, true);
        HabitCompletion saved = habitCompletionRepository.save(newCompletion);
        int streak = calculateCurrentStreak(id, date);

        return new DailyHabitDto(
                habit.getId(),
                habit.getName(),
                habit.getCreatedAt(),
                true,
                date,
                saved.getId(),
                streak
        );
    }

    @Override
    public DailyHabitDto unmarkHabitCompleted(Long id, LocalDate date) {
        if (date == null) {
            date = LocalDate.now();
        }

        Habit habit = habitRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Habit not found with id: " + id));

        habitCompletionRepository.deleteByHabitIdAndCompletionDate(id, date);
        int streak = calculateCurrentStreak(id, date);

        return new DailyHabitDto(
                habit.getId(),
                habit.getName(),
                habit.getCreatedAt(),
                false,
                date,
                null,
                streak
        );
    }

    @Override
    @Transactional(readOnly = true)
    public DashboardStatsDto getDashboardStats(LocalDate date) {
        if (date == null) {
            date = LocalDate.now();
        }

        int totalHabits = (int) habitRepository.count();
        int completedHabits = (int) habitCompletionRepository.countByCompletionDateAndCompletedTrue(date);
        int pendingHabits = Math.max(0, totalHabits - completedHabits);
        double progressPercentage = totalHabits > 0
                ? Math.round(((double) completedHabits / totalHabits) * 100.0 * 10.0) / 10.0
                : 0.0;

        return new DashboardStatsDto(date, totalHabits, completedHabits, pendingHabits, progressPercentage);
    }

    private int calculateCurrentStreak(Long habitId, LocalDate referenceDate) {
        List<HabitCompletion> completions = habitCompletionRepository.findByHabitIdOrderByCompletionDateDesc(habitId);
        if (completions.isEmpty()) {
            return 0;
        }

        Set<LocalDate> completedDates = completions.stream()
                .filter(HabitCompletion::isCompleted)
                .map(HabitCompletion::getCompletionDate)
                .collect(Collectors.toSet());

        int streak = 0;
        LocalDate checkDate = referenceDate;

        // If not completed on reference date, start checking from yesterday
        if (!completedDates.contains(checkDate)) {
            checkDate = checkDate.minusDays(1);
        }

        while (completedDates.contains(checkDate)) {
            streak++;
            checkDate = checkDate.minusDays(1);
        }

        return streak;
    }

    private HabitResponseDto mapToHabitResponseDto(Habit habit) {
        return new HabitResponseDto(habit.getId(), habit.getName(), habit.getCreatedAt());
    }
}
