package com.example.exercisetracker.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [ExerciseEntry::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun exerciseDao(): ExerciseDao
}
