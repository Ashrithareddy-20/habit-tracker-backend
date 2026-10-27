package com.habittracker;

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
import com.habittracker.service.HabitServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HabitServiceTest {

    @Mock
    private HabitRepository habitRepository;

    @Mock
    private HabitCompletionRepository habitCompletionRepository;

    @InjectMocks
    private HabitServiceImpl habitService;

    private Habit sampleHabit;

    @BeforeEach
    void setUp() {
        sampleHabit = new Habit(1L, "Morning Exercise");
        sampleHabit.setCreatedAt(LocalDateTime.now());
    }

    @Test
    @DisplayName("Should successfully create a new habit")
    void testCreateHabit_Success() {
        HabitRequestDto request = new HabitRequestDto("Morning Exercise");
        when(habitRepository.existsByNameIgnoreCase("Morning Exercise")).thenReturn(false);
        when(habitRepository.save(any(Habit.class))).thenReturn(sampleHabit);

        HabitResponseDto response = habitService.createHabit(request);

        assertNotNull(response);
        assertEquals("Morning Exercise", response.getName());
        assertEquals(1L, response.getId());
        verify(habitRepository, times(1)).save(any(Habit.class));
    }

    @Test
    @DisplayName("Should throw DuplicateRecordException when creating habit with existing name")
    void testCreateHabit_Duplicate() {
        HabitRequestDto request = new HabitRequestDto("Morning Exercise");
        when(habitRepository.existsByNameIgnoreCase("Morning Exercise")).thenReturn(true);

        assertThrows(DuplicateRecordException.class, () -> habitService.createHabit(request));
        verify(habitRepository, never()).save(any(Habit.class));
    }

    @Test
    @DisplayName("Should throw BadRequestException when creating habit with empty name")
    void testCreateHabit_EmptyName() {
        HabitRequestDto request = new HabitRequestDto("   ");

        assertThrows(BadRequestException.class, () -> habitService.createHabit(request));
        verify(habitRepository, never()).save(any(Habit.class));
    }

    @Test
    @DisplayName("Should retrieve all habits")
    void testGetAllHabits() {
        when(habitRepository.findAllByOrderByCreatedAtDesc()).thenReturn(List.of(sampleHabit));

        List<HabitResponseDto> result = habitService.getAllHabits();

        assertEquals(1, result.size());
        assertEquals("Morning Exercise", result.get(0).getName());
    }

    @Test
    @DisplayName("Should retrieve habit by ID")
    void testGetHabitById_Success() {
        when(habitRepository.findById(1L)).thenReturn(Optional.of(sampleHabit));

        HabitResponseDto response = habitService.getHabitById(1L);

        assertNotNull(response);
        assertEquals(1L, response.getId());
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when habit not found")
    void testGetHabitById_NotFound() {
        when(habitRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> habitService.getHabitById(99L));
    }

    @Test
    @DisplayName("Should update habit name successfully")
    void testUpdateHabit_Success() {
        HabitRequestDto updateRequest = new HabitRequestDto("Evening Workout");
        when(habitRepository.findById(1L)).thenReturn(Optional.of(sampleHabit));
        when(habitRepository.existsByNameIgnoreCaseAndIdNot("Evening Workout", 1L)).thenReturn(false);
        when(habitRepository.save(any(Habit.class))).thenReturn(new Habit(1L, "Evening Workout"));

        HabitResponseDto response = habitService.updateHabit(1L, updateRequest);

        assertNotNull(response);
        assertEquals("Evening Workout", response.getName());
    }

    @Test
    @DisplayName("Should delete habit successfully")
    void testDeleteHabit_Success() {
        when(habitRepository.findById(1L)).thenReturn(Optional.of(sampleHabit));

        habitService.deleteHabit(1L);

        verify(habitRepository, times(1)).delete(sampleHabit);
    }

    @Test
    @DisplayName("Should mark habit as completed for a date")
    void testMarkHabitCompleted_Success() {
        LocalDate today = LocalDate.now();
        when(habitRepository.findById(1L)).thenReturn(Optional.of(sampleHabit));
        when(habitCompletionRepository.findByHabitIdAndCompletionDate(1L, today)).thenReturn(Optional.empty());

        HabitCompletion completion = new HabitCompletion(sampleHabit, today, true);
        completion.setId(10L);
        when(habitCompletionRepository.save(any(HabitCompletion.class))).thenReturn(completion);
        when(habitCompletionRepository.findByHabitIdOrderByCompletionDateDesc(1L)).thenReturn(List.of(completion));

        DailyHabitDto result = habitService.markHabitCompleted(1L, today);

        assertNotNull(result);
        assertTrue(result.isCompleted());
        assertEquals(10L, result.getCompletionId());
        assertEquals(1, result.getCurrentStreak());
    }

    @Test
    @DisplayName("Should throw DuplicateRecordException when habit already completed for date")
    void testMarkHabitCompleted_Duplicate() {
        LocalDate today = LocalDate.now();
        when(habitRepository.findById(1L)).thenReturn(Optional.of(sampleHabit));
        HabitCompletion existing = new HabitCompletion(sampleHabit, today, true);
        when(habitCompletionRepository.findByHabitIdAndCompletionDate(1L, today)).thenReturn(Optional.of(existing));

        assertThrows(DuplicateRecordException.class, () -> habitService.markHabitCompleted(1L, today));
        verify(habitCompletionRepository, never()).save(any(HabitCompletion.class));
    }

    @Test
    @DisplayName("Should unmark habit completion for a date")
    void testUnmarkHabitCompleted_Success() {
        LocalDate today = LocalDate.now();
        when(habitRepository.findById(1L)).thenReturn(Optional.of(sampleHabit));
        when(habitCompletionRepository.findByHabitIdOrderByCompletionDateDesc(1L)).thenReturn(Collections.emptyList());

        DailyHabitDto result = habitService.unmarkHabitCompleted(1L, today);

        assertNotNull(result);
        assertFalse(result.isCompleted());
        assertNull(result.getCompletionId());
        verify(habitCompletionRepository, times(1)).deleteByHabitIdAndCompletionDate(1L, today);
    }

    @Test
    @DisplayName("Should calculate dashboard stats accurately")
    void testGetDashboardStats() {
        LocalDate today = LocalDate.now();
        when(habitRepository.count()).thenReturn(5L);
        when(habitCompletionRepository.countByCompletionDateAndCompletedTrue(today)).thenReturn(3L);

        DashboardStatsDto stats = habitService.getDashboardStats(today);

        assertEquals(5, stats.getTotalHabits());
        assertEquals(3, stats.getCompletedHabits());
        assertEquals(2, stats.getPendingHabits());
        assertEquals(60.0, stats.getProgressPercentage());
    }
}
