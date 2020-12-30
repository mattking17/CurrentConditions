package me.mking.currentconditions.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class CurrentWeatherEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val location: String,
    val condition: String,
    val temperature: Double,
    val windSpeed: String,
    val windDirection: String,
    val iconUrl: String,
    val updated: Long
)