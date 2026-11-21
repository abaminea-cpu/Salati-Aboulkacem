package com.example.data.local.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cached_prayer_days")
data class CachedPrayerDayEntity(
    @PrimaryKey val cacheKey: String, // e.g., "2026-11-21_21.42_39.83_4_0"
    val dateKey: String,
    val cityName: String,
    val fajrMillis: Long,
    val sunriseMillis: Long,
    val dhuhrMillis: Long,
    val asrMillis: Long,
    val maghribMillis: Long,
    val ishaMillis: Long,
    val hijriDay: Int,
    val hijriMonthAr: String,
    val hijriYear: Int,
    val hijriWeekdayAr: String,
    val source: String, // LOCAL_ADHAN or ALADHAN_API
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "adhkar_items")
data class DhikrEntity(
    @PrimaryKey val id: String,
    val categoryId: String,
    val title: String,
    val arabicText: String,
    val referenceText: String,
    val targetCount: Int,
    val audioUrl: String?,
    val orderIndex: Int
)

@Entity(tableName = "adhkar_progress")
data class DhikrProgressEntity(
    @PrimaryKey val dhikrId: String,
    val currentCount: Int = 0,
    val isFavorite: Boolean = false,
    val lastUpdated: Long = System.currentTimeMillis()
)
