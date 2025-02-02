package com.example.exercisetracker.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "exercise_entries")
data class ExerciseEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val day: Int,           // npr. 15
    val month: Int,         // npr. 7 (jul, ili 1-based vrednost ako ti tako više odgovara)
    val year: Int,          // npr. 2025
    val exerciseType: String, // npr. "pushups", "pullups", "situps", "squats", "plank"
    val count: Int          // Broj ponavljanja za taj unos
)
