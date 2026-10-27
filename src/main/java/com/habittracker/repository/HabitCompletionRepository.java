package com.habittracker.repository;

import com.habittracker.model.HabitCompletion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface HabitCompletionRepository extends JpaRepository<HabitCompletion, Long> {

    Optional<HabitCompletion> findByHabitIdAndCompletionDate(Long habitId, LocalDate completionDate);

    boolean existsByHabitIdAndCompletionDate(Long habitId, LocalDate completionDate);

    List<HabitCompletion> findByCompletionDate(LocalDate completionDate);

    List<HabitCompletion> findByHabitIdOrderByCompletionDateDesc(Long habitId);

    void deleteByHabitIdAndCompletionDate(Long habitId, LocalDate completionDate);

    long countByCompletionDateAndCompletedTrue(LocalDate completionDate);
}
