package com.example.exercisetracker.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface ExerciseDao {
    @Insert
    suspend fun insertEntry(entry: ExerciseEntry)

    @Update
    suspend fun updateEntry(entry: ExerciseEntry)

    // Dohvatanje zapisa za dati dan, mesec, godinu i tip vežbe
    @Query("SELECT * FROM exercise_entries WHERE day = :day AND month = :month AND year = :year AND exerciseType = :exerciseType")
    suspend fun getEntry(day: Int, month: Int, year: Int, exerciseType: String): ExerciseEntry?


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
