-- Habit Tracker MySQL Schema Definition
CREATE DATABASE IF NOT EXISTS habit_tracker_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE habit_tracker_db;

-- 1. Habits Table
CREATE TABLE IF NOT EXISTS habits (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2. Habit Completions Table
CREATE TABLE IF NOT EXISTS habit_completions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    habit_id BIGINT NOT NULL,
    completion_date DATE NOT NULL,
    completed BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_habit_completion_habit FOREIGN KEY (habit_id) REFERENCES habits(id) ON DELETE CASCADE,
    CONSTRAINT uk_habit_completion_date UNIQUE (habit_id, completion_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
