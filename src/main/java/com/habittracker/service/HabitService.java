package com.habittracker.service;

import com.habittracker.dto.DailyHabitDto;
import com.habittracker.dto.DashboardStatsDto;
import com.habittracker.dto.HabitRequestDto;
import com.habittracker.dto.HabitResponseDto;

import java.time.LocalDate;
import java.util.List;

public interface HabitService {

    HabitResponseDto createHabit(HabitRequestDto requestDto);

    List<HabitResponseDto> getAllHabits();

    HabitResponseDto getHabitById(Long id);

    HabitResponseDto updateHabit(Long id, HabitRequestDto requestDto);

    void deleteHabit(Long id);

    List<DailyHabitDto> getDailyHabits(LocalDate date);

    DailyHabitDto markHabitCompleted(Long id, LocalDate date);

    DailyHabitDto unmarkHabitCompleted(Long id, LocalDate date);

    DashboardStatsDto getDashboardStats(LocalDate date);
}
