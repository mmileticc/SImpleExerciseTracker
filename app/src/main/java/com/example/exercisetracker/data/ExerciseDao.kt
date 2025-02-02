package com.example.exercisetracker.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface ExerciseDao {
    @Insert
    suspend fun insertEntry(entry: ExerciseEntry)

    // Ukupno za tačan dan
    @Query("SELECT SUM(count) FROM exercise_entries WHERE year = :year AND month = :month AND day = :day AND exerciseType = :exerciseType")
    suspend fun getTotalForDay(year: Int, month: Int, day: Int, exerciseType: String): Int?

    // Ukupno za određeni mesec u godini
    @Query("SELECT SUM(count) FROM exercise_entries WHERE year = :year AND month = :month AND exerciseType = :exerciseType")
    suspend fun getTotalForMonth(year: Int, month: Int, exerciseType: String): Int?

    // Ukupno za određenu godinu
    @Query("SELECT SUM(count) FROM exercise_entries WHERE year = :year AND exerciseType = :exerciseType")
    suspend fun getTotalForYear(year: Int, exerciseType: String): Int?
}
