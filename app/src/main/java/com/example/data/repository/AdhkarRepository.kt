package com.example.data.repository

import com.example.data.local.AdhkarSeedData
import com.example.data.local.db.AdhkarDao
import com.example.data.local.db.DhikrProgressEntity
import com.example.domain.model.AdhkarCategory
import com.example.domain.model.DhikrWithProgress
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class AdhkarRepository(private val dao: AdhkarDao) {

    suspend fun ensureSeeded() {
        if (dao.getCount() == 0) {
            dao.insertAll(AdhkarSeedData.items)
        }
    }

    val categoriesFlow: Flow<List<AdhkarCategory>> = combine(
        dao.observeAllItems(),
        dao.observeAllProgress()
    ) { items, progressList ->
        val progressMap = progressList.associateBy { it.dhikrId }
        AdhkarSeedData.categories.map { meta ->
            val catItems = items.filter { it.categoryId == meta.id }
            val completed = catItems.count { item ->
                (progressMap[item.id]?.currentCount ?: 0) >= item.targetCount
            }
            AdhkarCategory(
                id = meta.id,
                title = meta.title,
                subtitle = meta.subtitle,
                iconKey = meta.iconKey,
                totalCount = catItems.size,
                completedCount = completed
            )
        }
    }

    val allItemsWithProgressFlow: Flow<List<DhikrWithProgress>> = combine(
        dao.observeAllItems(),
        dao.observeAllProgress()
    ) { items, progressList ->
        val progressMap = progressList.associateBy { it.dhikrId }
        items.map { entity ->
            val prog = progressMap[entity.id]
            DhikrWithProgress(
                id = entity.id,
                categoryId = entity.categoryId,
                title = entity.title,
                arabicText = entity.arabicText,
                referenceText = entity.referenceText,
                targetCount = entity.targetCount,
                audioUrl = entity.audioUrl,
                orderIndex = entity.orderIndex,
                currentCount = prog?.currentCount ?: 0,
                isFavorite = prog?.isFavorite ?: false
            )
        }
    }

    fun observeCategoryItems(categoryId: String): Flow<List<DhikrWithProgress>> {
        if (categoryId == FAVORITES_CATEGORY_ID) {
            return combine(dao.observeAllItems(), dao.observeAllProgress()) { items, progressList ->
                val progressMap = progressList.associateBy { it.dhikrId }
                items.mapNotNull { entity ->
                    val prog = progressMap[entity.id]
                    if (prog?.isFavorite == true) {
                        DhikrWithProgress(
                            id = entity.id,
                            categoryId = entity.categoryId,
                            title = entity.title,
                            arabicText = entity.arabicText,
                            referenceText = entity.referenceText,
                            targetCount = entity.targetCount,
                            audioUrl = entity.audioUrl,
                            orderIndex = entity.orderIndex,
                            currentCount = prog.currentCount,
                            isFavorite = true
                        )
                    } else null
                }
            }
        }
        return combine(
            dao.observeByCategory(categoryId),
            dao.observeAllProgress()
        ) { items, progressList ->
            val progressMap = progressList.associateBy { it.dhikrId }
            items.map { entity ->
                val prog = progressMap[entity.id]
                DhikrWithProgress(
                    id = entity.id,
                    categoryId = entity.categoryId,
                    title = entity.title,
                    arabicText = entity.arabicText,
                    referenceText = entity.referenceText,
                    targetCount = entity.targetCount,
                    audioUrl = entity.audioUrl,
                    orderIndex = entity.orderIndex,
                    currentCount = prog?.currentCount ?: 0,
                    isFavorite = prog?.isFavorite ?: false
                )
            }
        }
    }

    suspend fun incrementCount(dhikrId: String, targetCount: Int) {
        val current = dao.getProgress(dhikrId)
        val next = ((current?.currentCount ?: 0) + 1).coerceAtMost(targetCount)
        dao.upsertProgress(
            DhikrProgressEntity(
                dhikrId = dhikrId,
                currentCount = next,
                isFavorite = current?.isFavorite ?: false,
                lastUpdated = System.currentTimeMillis()
            )
        )
    }

    suspend fun resetItemCount(dhikrId: String) {
        val current = dao.getProgress(dhikrId)
        dao.upsertProgress(
            DhikrProgressEntity(
                dhikrId = dhikrId,
                currentCount = 0,
                isFavorite = current?.isFavorite ?: false,
                lastUpdated = System.currentTimeMillis()
            )
        )
    }

    suspend fun toggleFavorite(dhikrId: String) {
        val current = dao.getProgress(dhikrId)
        dao.upsertProgress(
            DhikrProgressEntity(
                dhikrId = dhikrId,
                currentCount = current?.currentCount ?: 0,
                isFavorite = !(current?.isFavorite ?: false),
                lastUpdated = System.currentTimeMillis()
            )
        )
    }

    suspend fun resetCategory(categoryId: String) {
        dao.resetCategoryCounts(categoryId)
    }

    companion object {
        const val FAVORITES_CATEGORY_ID = "favorites"
    }
}
