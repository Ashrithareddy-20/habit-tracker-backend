package com.habittracker;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.habittracker.controller.HabitController;
import com.habittracker.dto.DailyHabitDto;
import com.habittracker.dto.HabitRequestDto;
import com.habittracker.dto.HabitResponseDto;
import com.habittracker.exception.DuplicateRecordException;
import com.habittracker.exception.GlobalExceptionHandler;
import com.habittracker.exception.ResourceNotFoundException;
import com.habittracker.service.HabitService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class HabitControllerTest {

    private MockMvc mockMvc;

    @Mock
    private HabitService habitService;

    @InjectMocks
    private HabitController habitController;

    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(habitController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("POST /api/habits - Should create habit and return 201 Created")
    void testCreateHabit_Success() throws Exception {
        HabitRequestDto request = new HabitRequestDto("Read 20 Pages");
        HabitResponseDto response = new HabitResponseDto(1L, "Read 20 Pages", LocalDateTime.now());

        when(habitService.createHabit(any(HabitRequestDto.class))).thenReturn(response);

        mockMvc.perform(post("/api/habits")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Read 20 Pages"));
    }

    @Test
    @DisplayName("POST /api/habits - Should return 400 Bad Request on invalid payload")
    void testCreateHabit_Invalid() throws Exception {
        HabitRequestDto request = new HabitRequestDto(" ");

        mockMvc.perform(post("/api/habits")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    @DisplayName("GET /api/habits - Should return all habits")
    void testGetAllHabits() throws Exception {
        HabitResponseDto h1 = new HabitResponseDto(1L, "Exercise", LocalDateTime.now());
        when(habitService.getAllHabits()).thenReturn(List.of(h1));

        mockMvc.perform(get("/api/habits"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Exercise"));
    }

    @Test
    @DisplayName("GET /api/habits/{id} - Should return habit by ID")
    void testGetHabitById_Success() throws Exception {
        HabitResponseDto h1 = new HabitResponseDto(1L, "Exercise", LocalDateTime.now());
        when(habitService.getHabitById(1L)).thenReturn(h1);

        mockMvc.perform(get("/api/habits/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Exercise"));
    }

    @Test
    @DisplayName("GET /api/habits/{id} - Should return 404 when not found")
    void testGetHabitById_NotFound() throws Exception {
        when(habitService.getHabitById(99L)).thenThrow(new ResourceNotFoundException("Habit not found with id: 99"));

        mockMvc.perform(get("/api/habits/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Habit not found with id: 99"));
    }

    @Test
    @DisplayName("PUT /api/habits/{id}/complete - Should mark complete")
    void testMarkComplete_Success() throws Exception {
        LocalDate today = LocalDate.now();
        DailyHabitDto dailyHabit = new DailyHabitDto(1L, "Exercise", LocalDateTime.now(), true, today, 10L, 1);
        when(habitService.markHabitCompleted(eq(1L), any(LocalDate.class))).thenReturn(dailyHabit);

        mockMvc.perform(put("/api/habits/1/complete?date=" + today))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.completed").value(true))
                .andExpect(jsonPath("$.currentStreak").value(1));
    }

    @Test
    @DisplayName("PUT /api/habits/{id}/complete - Should return 409 Conflict if already marked completed")
    void testMarkComplete_Duplicate() throws Exception {
        LocalDate today = LocalDate.now();
        when(habitService.markHabitCompleted(eq(1L), any(LocalDate.class)))
                .thenThrow(new DuplicateRecordException("Habit with id 1 is already marked as completed for date " + today));

        mockMvc.perform(put("/api/habits/1/complete?date=" + today))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }
}
