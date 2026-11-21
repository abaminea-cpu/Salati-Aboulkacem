package com.example.data.repository

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.example.data.local.db.CachedPrayerDayEntity
import com.example.data.local.db.PrayerCacheDao
import com.example.data.remote.AlAdhanApiService
import com.example.domain.model.HijriDateInfo
import com.example.domain.model.PrayerDataSource
import com.example.domain.model.PrayerDaySchedule
import com.example.domain.model.PrayerTimeEntry
import com.example.domain.model.PrayerType
import com.example.domain.model.UserPreferencesState
import com.example.domain.prayer.AdhanCalculator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

class PrayerRepository(
    private val context: Context,
    private val cacheDao: PrayerCacheDao,
    private val apiService: AlAdhanApiService
) {

    suspend fun getScheduleForDate(
        calendar: Calendar,
        prefs: UserPreferencesState,
        forceRefreshApi: Boolean = false
    ): PrayerDaySchedule = withContext(Dispatchers.IO) {
        val tz = TimeZone.getTimeZone(prefs.timezoneId)
        val localCal = (calendar.clone() as Calendar).apply { timeZone = tz }
        val year = localCal.get(Calendar.YEAR)
        val month = localCal.get(Calendar.MONTH) + 1
        val day = localCal.get(Calendar.DAY_OF_MONTH)
        val dateKey = String.format(Locale.US, "%04d-%02d-%02d", year, month, day)
        val cacheKey = buildCacheKey(dateKey, prefs)

        // 1. Check cached schedule in Room first
        val cached = cacheDao.getByCacheKey(cacheKey)
        if (cached != null && !forceRefreshApi) {
            // If cached locally and user wants API sync, try refreshing in background only if source was LOCAL_ADHAN
            if (cached.source == PrayerDataSource.ALADHAN_API.name || !prefs.preferApiSync || !isNetworkAvailable()) {
                return@withContext mapEntityToSchedule(cached, localCal, prefs, tz)
            }
        }

        // 2. Try AlAdhan API if enabled and network is available
        if (prefs.preferApiSync && isNetworkAvailable()) {
            val apiSchedule = tryFetchFromAlAdhan(year, month, day, dateKey, cacheKey, localCal, prefs, tz)
            if (apiSchedule != null) {
                return@withContext apiSchedule
            }
        }

        // 3. Always succeed offline via Batoul Apps Adhan astronomical engine & save to Room cache
        val localSchedule = AdhanCalculator.calculateSchedule(
            year = year,
            month = month,
            day = day,
            latitude = prefs.latitude,
            longitude = prefs.longitude,
            timezoneId = prefs.timezoneId,
            cityName = prefs.cityName,
            method = prefs.calculationMethod,
            madhab = prefs.madhab,
            highLatitudeRule = prefs.highLatitudeRule,
            manualOffsets = prefs.manualOffsets,
            hijriAdjustmentDays = prefs.hijriAdjustmentDays,
            prayerAlertsEnabled = prefs.prayerAlertsEnabled,
            source = PrayerDataSource.LOCAL_ADHAN
        )
        cacheDao.upsert(mapScheduleToEntity(cacheKey, localSchedule, stripOffsets = true, prefs = prefs))
        localSchedule
    }

    private suspend fun tryFetchFromAlAdhan(
        year: Int,
        month: Int,
        day: Int,
        dateKey: String,
        cacheKey: String,
        localCal: Calendar,
        prefs: UserPreferencesState,
        tz: TimeZone
    ): PrayerDaySchedule? {
        return try {
            val ddMmYyyy = String.format(Locale.US, "%02d-%02d-%04d", day, month, year)
            val response = apiService.getTimings(
                ddMmYyyy = ddMmYyyy,
                latitude = prefs.latitude,
                longitude = prefs.longitude,
                method = prefs.calculationMethod.apiId,
                school = prefs.madhab.schoolId
            )
            val data = response.data ?: return null
            val timings = data.timings

            val baseFajr = parseHhMmToEpoch(year, month, day, timings.fajr, tz) ?: return null
            val baseSunrise = parseHhMmToEpoch(year, month, day, timings.sunrise, tz) ?: return null
            val baseDhuhr = parseHhMmToEpoch(year, month, day, timings.dhuhr, tz) ?: return null
            val baseAsr = parseHhMmToEpoch(year, month, day, timings.asr, tz) ?: return null
            val baseMaghrib = parseHhMmToEpoch(year, month, day, timings.maghrib, tz) ?: return null
            val baseIsha = parseHhMmToEpoch(year, month, day, timings.isha, tz) ?: return null

            val hijriDay = data.date.hijri.day.toIntOrNull() ?: 1
            val hijriYear = data.date.hijri.year.toIntOrNull() ?: 1448
            val hijriMonthAr = data.date.hijri.month.ar
            val hijriWeekdayAr = data.date.hijri.weekday.ar

            val entity = CachedPrayerDayEntity(
                cacheKey = cacheKey,
                dateKey = dateKey,
                cityName = prefs.cityName,
                fajrMillis = baseFajr,
                sunriseMillis = baseSunrise,
                dhuhrMillis = baseDhuhr,
                asrMillis = baseAsr,
                maghribMillis = baseMaghrib,
                ishaMillis = baseIsha,
                hijriDay = hijriDay,
                hijriMonthAr = hijriMonthAr,
                hijriYear = hijriYear,
                hijriWeekdayAr = hijriWeekdayAr,
                source = PrayerDataSource.ALADHAN_API.name
            )
            cacheDao.upsert(entity)
            mapEntityToSchedule(entity, localCal, prefs, tz)
        } catch (_: Throwable) {
            null
        }
    }

    private fun mapEntityToSchedule(
        entity: CachedPrayerDayEntity,
        localCal: Calendar,
        prefs: UserPreferencesState,
        tz: TimeZone
    ): PrayerDaySchedule {
        val rawMap = mapOf(
            PrayerType.FAJR to entity.fajrMillis,
            PrayerType.SUNRISE to entity.sunriseMillis,
            PrayerType.DHUHR to entity.dhuhrMillis,
            PrayerType.ASR to entity.asrMillis,
            PrayerType.MAGHRIB to entity.maghribMillis,
            PrayerType.ISHA to entity.ishaMillis
        )
        val entries = PrayerType.entries.map { type ->
            val offset = prefs.manualOffsets[type] ?: 0
            val adjustedMillis = rawMap.getValue(type) + offset * 60_000L
            PrayerTimeEntry(
                type = type,
                epochMillis = adjustedMillis,
                formattedTime = AdhanCalculator.formatTimeArabic(adjustedMillis, tz),
                offsetMinutes = offset,
                isAlertEnabled = prefs.prayerAlertsEnabled[type] ?: type.isObligatoryAdhan
            )
        }
        val hijri = if (prefs.hijriAdjustmentDays != 0) {
            AdhanCalculator.formatHijriDate(localCal, prefs.hijriAdjustmentDays)
        } else {
            HijriDateInfo(
                day = entity.hijriDay,
                monthArabic = entity.hijriMonthAr,
                year = entity.hijriYear,
                weekdayArabic = entity.hijriWeekdayAr,
                formattedArabic = "${entity.hijriWeekdayAr}، ${entity.hijriDay} ${entity.hijriMonthAr} ${entity.hijriYear} هـ"
            )
        }
        val source = runCatching { PrayerDataSource.valueOf(entity.source) }
            .getOrDefault(PrayerDataSource.LOCAL_ADHAN)

        return PrayerDaySchedule(
            dateKey = entity.dateKey,
            cityName = prefs.cityName,
            hijriDate = hijri,
            gregorianDateArabic = AdhanCalculator.formatGregorianDateArabic(localCal),
            entries = entries,
            source = source
        )
    }

    private fun mapScheduleToEntity(
        cacheKey: String,
        schedule: PrayerDaySchedule,
        stripOffsets: Boolean,
        prefs: UserPreferencesState
    ): CachedPrayerDayEntity {
        fun rawMillis(type: PrayerType): Long {
            val entry = schedule.entries.first { it.type == type }
            return if (stripOffsets) {
                entry.epochMillis - (prefs.manualOffsets[type] ?: 0) * 60_000L
            } else {
                entry.epochMillis
            }
        }
        return CachedPrayerDayEntity(
            cacheKey = cacheKey,
            dateKey = schedule.dateKey,
            cityName = schedule.cityName,
            fajrMillis = rawMillis(PrayerType.FAJR),
            sunriseMillis = rawMillis(PrayerType.SUNRISE),
            dhuhrMillis = rawMillis(PrayerType.DHUHR),
            asrMillis = rawMillis(PrayerType.ASR),
            maghribMillis = rawMillis(PrayerType.MAGHRIB),
            ishaMillis = rawMillis(PrayerType.ISHA),
            hijriDay = schedule.hijriDate.day,
            hijriMonthAr = schedule.hijriDate.monthArabic,
            hijriYear = schedule.hijriDate.year,
            hijriWeekdayAr = schedule.hijriDate.weekdayArabic,
            source = schedule.source.name
        )
    }

    private fun parseHhMmToEpoch(
        year: Int,
        month: Int,
        day: Int,
        rawTime: String,
        tz: TimeZone
    ): Long? {
        val clean = rawTime.substringBefore(" ").trim()
        val parts = clean.split(":")
        if (parts.size < 2) return null
        val hour = parts[0].toIntOrNull() ?: return null
        val minute = parts[1].toIntOrNull() ?: return null
        return Calendar.getInstance(tz).apply {
            set(year, month - 1, day, hour, minute, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    private fun buildCacheKey(dateKey: String, prefs: UserPreferencesState): String {
        val latRounded = String.format(Locale.US, "%.2f", prefs.latitude)
        val lngRounded = String.format(Locale.US, "%.2f", prefs.longitude)
        return "${dateKey}_${latRounded}_${lngRounded}_${prefs.calculationMethod.apiId}_${prefs.madhab.schoolId}"
    }

    fun isNetworkAvailable(): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager ?: return false
        val network = cm.activeNetwork ?: return false
        val caps = cm.getNetworkCapabilities(network) ?: return false
        return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
}
