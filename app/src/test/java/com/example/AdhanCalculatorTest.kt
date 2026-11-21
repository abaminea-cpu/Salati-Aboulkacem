package com.example

import com.example.domain.model.CalculationMethod
import com.example.domain.model.HighLatitudeRule
import com.example.domain.model.Madhab
import com.example.domain.model.PrayerType
import com.example.domain.prayer.AdhanCalculator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar
import java.util.TimeZone

class AdhanCalculatorTest {

    @Test
    fun calculatesChronologicalPrayerTimesForMakkah() {
        val schedule = AdhanCalculator.calculateSchedule(
            year = 2026,
            month = 11,
            day = 21,
            latitude = 21.4225,
            longitude = 39.8262,
            timezoneId = "Asia/Riyadh",
            cityName = "مكة المكرمة",
            method = CalculationMethod.UMM_AL_QURA,
            madhab = Madhab.SHAFI,
            highLatitudeRule = HighLatitudeRule.MIDDLE_OF_THE_NIGHT,
            manualOffsets = emptyMap(),
            hijriAdjustmentDays = 0,
            prayerAlertsEnabled = emptyMap()
        )

        assertEquals(6, schedule.entries.size)
        for (i in 0 until schedule.entries.lastIndex) {
            assertTrue(
                "Prayer ${schedule.entries[i].type} must precede ${schedule.entries[i + 1].type}",
                schedule.entries[i].epochMillis < schedule.entries[i + 1].epochMillis
            )
        }

        // Check Makkah Dhuhr in late November is right around 12:06-12:12 PM AST
        val dhuhr = schedule.entries.first { it.type == PrayerType.DHUHR }
        val cal = Calendar.getInstance(TimeZone.getTimeZone("Asia/Riyadh")).apply {
            timeInMillis = dhuhr.epochMillis
        }
        assertEquals(12, cal.get(Calendar.HOUR_OF_DAY))
        assertTrue(cal.get(Calendar.MINUTE) in 3..15)
    }

    @Test
    fun hanafiAsrIsLaterThanShafiAsr() {
        val shafi = AdhanCalculator.calculateSchedule(
            year = 2026,
            month = 6,
            day = 15,
            latitude = 30.0444,
            longitude = 31.2357,
            timezoneId = "Africa/Cairo",
            cityName = "القاهرة",
            method = CalculationMethod.EGYPT,
            madhab = Madhab.SHAFI,
            highLatitudeRule = HighLatitudeRule.MIDDLE_OF_THE_NIGHT,
            manualOffsets = emptyMap(),
            hijriAdjustmentDays = 0,
            prayerAlertsEnabled = emptyMap()
        )
        val hanafi = AdhanCalculator.calculateSchedule(
            year = 2026,
            month = 6,
            day = 15,
            latitude = 30.0444,
            longitude = 31.2357,
            timezoneId = "Africa/Cairo",
            cityName = "القاهرة",
            method = CalculationMethod.EGYPT,
            madhab = Madhab.HANAFI,
            highLatitudeRule = HighLatitudeRule.MIDDLE_OF_THE_NIGHT,
            manualOffsets = emptyMap(),
            hijriAdjustmentDays = 0,
            prayerAlertsEnabled = emptyMap()
        )
        val shafiAsr = shafi.entries.first { it.type == PrayerType.ASR }.epochMillis
        val hanafiAsr = hanafi.entries.first { it.type == PrayerType.ASR }.epochMillis
        assertTrue(hanafiAsr - shafiAsr >= 45 * 60_000L)
    }

    @Test
    fun calculatesAccurateQiblaBearingFromCairoAndNewYork() {
        // Cairo is NW of Makkah -> Qibla should be ~136 degrees (SE)
        val cairoQibla = AdhanCalculator.calculateQibla(30.0444, 31.2357)
        assertTrue("Cairo Qibla ${cairoQibla.bearingDegrees}", cairoQibla.bearingDegrees in 134.0..138.0)
        assertTrue(cairoQibla.distanceKm in 1250.0..1320.0)

        // New York Qibla is ~58-59 degrees (ENE)
        val nyQibla = AdhanCalculator.calculateQibla(40.7128, -74.0060)
        assertTrue("NY Qibla ${nyQibla.bearingDegrees}", nyQibla.bearingDegrees in 57.0..60.5)
    }
}
