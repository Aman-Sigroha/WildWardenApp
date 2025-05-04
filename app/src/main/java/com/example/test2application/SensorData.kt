package com.example.test2application

data class SensorData(
    val deviceId: String,
    val heartRate: Int,
    val temperature: Float,
    val location: GpsLocation,
    val acceleration: AccelerometerData,
    val timestamp: Long = System.currentTimeMillis()
)

data class GpsLocation(
    val latitude: Double,
    val longitude: Double,
    val altitude: Double? = null
)

data class AccelerometerData(
    val x: Float,
    val y: Float,
    val z: Float
) 