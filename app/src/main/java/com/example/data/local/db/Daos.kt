package com.example.data.local.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PrayerCacheDao {
    @Query("SELECT * FROM cached_prayer_days WHERE cacheKey = :cacheKey LIMIT 1")
    suspend fun getByCacheKey(cacheKey: String): CachedPrayerDayEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: CachedPrayerDayEntity)

    @Query("DELETE FROM cached_prayer_days WHERE updatedAt < :olderThanMillis")
    suspend fun pruneOldEntries(olderThanMillis: Long)
}

@Dao
interface AdhkarDao {
    @Query("SELECT COUNT(*) FROM adhkar_items")
    suspend fun getCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<DhikrEntity>)

    @Query("SELECT * FROM adhkar_items ORDER BY categoryId, orderIndex ASC")
    fun observeAllItems(): Flow<List<DhikrEntity>>

    @Query("SELECT * FROM adhkar_items WHERE categoryId = :categoryId ORDER BY orderIndex ASC")
    fun observeByCategory(categoryId: String): Flow<List<DhikrEntity>>

    @Query("SELECT * FROM adhkar_progress")
    fun observeAllProgress(): Flow<List<DhikrProgressEntity>>

    @Query("SELECT * FROM adhkar_progress WHERE dhikrId = :dhikrId LIMIT 1")
    suspend fun getProgress(dhikrId: String): DhikrProgressEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertProgress(progress: DhikrProgressEntity)

    @Query("UPDATE adhkar_progress SET currentCount = 0 WHERE dhikrId IN (SELECT id FROM adhkar_items WHERE categoryId = :categoryId)")
    suspend fun resetCategoryCounts(categoryId: String)
}
