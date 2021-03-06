package me.mking.currentconditions.domain.repositories

import me.mking.currentconditions.domain.models.CurrentWeather

interface CurrentWeatherRepository {
    suspend fun getCurrentWeather(currentWeatherInput: CurrentWeatherInput): CurrentWeather
    suspend fun insertCurrentWeather(currentWeather: CurrentWeather)
}

data class CurrentWeatherInput(
    val latitude: Double,
    val longitude: Double,
    val unitType: UnitType,
    val maxAge: Long = 0L
) {
    enum class UnitType(val unitName: String) {
        METRIC("metric"),
        IMPERIAL("imperial")
    }
}