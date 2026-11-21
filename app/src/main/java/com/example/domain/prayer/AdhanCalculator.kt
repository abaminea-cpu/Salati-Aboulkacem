package com.example.domain.prayer

import android.icu.util.IslamicCalendar
import com.example.domain.model.CalculationMethod
import com.example.domain.model.HighLatitudeRule
import com.example.domain.model.HijriDateInfo
import com.example.domain.model.Madhab
import com.example.domain.model.PrayerDataSource
import com.example.domain.model.PrayerDaySchedule
import com.example.domain.model.PrayerTimeEntry
import com.example.domain.model.PrayerType
import com.example.domain.model.QiblaInfo
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlin.math.abs
import kotlin.math.acos
import kotlin.math.asin
import kotlin.math.atan
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.roundToLong
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.math.tan

object AdhanCalculator {

    private const val KAABA_LAT = 21.422487
    private const val KAABA_LNG = 39.826206

    private val HIJRI_MONTHS_AR = listOf(
        "محرم",
        "صفر",
        "ربيع الأول",
        "ربيع الآخر",
        "جمادى الأولى",
        "جمادى الآخرة",
        "رجب",
        "شعبان",
        "رمضان",
        "شوال",
        "ذو القعدة",
        "ذو الحجة"
    )

    private val WEEKDAYS_AR = mapOf(
        Calendar.SUNDAY to "الأحد",
        Calendar.MONDAY to "الإثنين",
        Calendar.TUESDAY to "الثلاثاء",
        Calendar.WEDNESDAY to "الأربعاء",
        Calendar.THURSDAY to "الخميس",
        Calendar.FRIDAY to "الجمعة",
        Calendar.SATURDAY to "السبت"
    )

    private data class SolarCoordinates(
        val declination: Double,
        val equationOfTimeHours: Double
    )

    fun calculateSchedule(
        year: Int,
        month: Int, // 1..12
        day: Int,
        latitude: Double,
        longitude: Double,
        timezoneId: String,
        cityName: String,
        method: CalculationMethod,
        madhab: Madhab,
        highLatitudeRule: HighLatitudeRule,
        manualOffsets: Map<PrayerType, Int>,
        hijriAdjustmentDays: Int,
        prayerAlertsEnabled: Map<PrayerType, Boolean>,
        source: PrayerDataSource = PrayerDataSource.LOCAL_ADHAN
    ): PrayerDaySchedule {
        val tz = TimeZone.getTimeZone(timezoneId)
        val localCal = Calendar.getInstance(tz).apply {
            set(year, month - 1, day, 12, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val hijriInfo = formatHijriDate(localCal, hijriAdjustmentDays)
        val gregorianAr = formatGregorianDateArabic(localCal)
        val dateKey = String.format(Locale.US, "%04d-%02d-%02d", year, month, day)

        val jd0 = julianDate(year, month, day)
        val noonSeedUtc = 12.0 - longitude / 15.0

        // Iterative solar calculations around each approximate prayer UTC hour
        val dhuhrUtc = computeSolarTransitUtc(jd0, noonSeedUtc, longitude)
        val sunriseUtc = computeSunAngleTimeUtc(
            jd0 = jd0,
            approxUtc = dhuhrUtc - 6.0,
            longitude = longitude,
            latitude = latitude,
            angleBelowHorizon = 0.8333,
            isMorning = true
        ) ?: (dhuhrUtc - 6.0)

        val sunsetUtc = computeSunAngleTimeUtc(
            jd0 = jd0,
            approxUtc = dhuhrUtc + 6.0,
            longitude = longitude,
            latitude = latitude,
            angleBelowHorizon = 0.8333,
            isMorning = false
        ) ?: (dhuhrUtc + 6.0)

        // Night length from today's sunset to next day's sunrise (approx 24 - (sunset - sunrise))
        val nightDurationHours = (24.0 - (sunsetUtc - sunriseUtc)).coerceIn(1.0, 23.0)

        val rawFajrUtc = computeSunAngleTimeUtc(
            jd0 = jd0,
            approxUtc = sunriseUtc - 1.5,
            longitude = longitude,
            latitude = latitude,
            angleBelowHorizon = method.fajrAngle,
            isMorning = true
        )
        val fajrUtc = applyHighLatitudeRule(
            rawTime = rawFajrUtc,
            referenceSunsetOrSunrise = sunriseUtc,
            nightDuration = nightDurationHours,
            angle = method.fajrAngle,
            rule = highLatitudeRule,
            isFajr = true
        )

        val asrUtc = computeAsrUtc(
            jd0 = jd0,
            approxUtc = dhuhrUtc + 3.5,
            longitude = longitude,
            latitude = latitude,
            shadowFactor = madhab.shadowFactor
        )

        val maghribUtc = sunsetUtc

        val ishaUtc = if (method.ishaIntervalMinutes != null) {
            val isRamadan = hijriInfo.monthArabic == "رمضان"
            val mins = if (method == CalculationMethod.UMM_AL_QURA && isRamadan) 120 else method.ishaIntervalMinutes
            maghribUtc + mins / 60.0
        } else {
            val rawIsha = computeSunAngleTimeUtc(
                jd0 = jd0,
                approxUtc = sunsetUtc + 1.5,
                longitude = longitude,
                latitude = latitude,
                angleBelowHorizon = method.ishaAngle,
                isMorning = false
            )
            applyHighLatitudeRule(
                rawTime = rawIsha,
                referenceSunsetOrSunrise = sunsetUtc,
                nightDuration = nightDurationHours,
                angle = method.ishaAngle,
                rule = highLatitudeRule,
                isFajr = false
            )
        }

        val utcBaseMillis = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
            set(year, month - 1, day, 0, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        val rawHoursMap = mapOf(
            PrayerType.FAJR to fajrUtc,
            PrayerType.SUNRISE to sunriseUtc,
            PrayerType.DHUHR to dhuhrUtc,
            PrayerType.ASR to asrUtc,
            PrayerType.MAGHRIB to maghribUtc,
            PrayerType.ISHA to ishaUtc
        )

        val entries = PrayerType.entries.map { type ->
            val utcHours = rawHoursMap.getValue(type)
            val offsetMins = manualOffsets[type] ?: 0
            // Round to nearest minute for clean prayer display and alarm alignment
            val roundedMinutesFromUtcMidnight = (utcHours * 60.0).roundToLong() + offsetMins
            val epochMillis = utcBaseMillis + roundedMinutesFromUtcMidnight * 60_000L
            PrayerTimeEntry(
                type = type,
                epochMillis = epochMillis,
                formattedTime = formatTimeArabic(epochMillis, tz),
                offsetMinutes = offsetMins,
                isAlertEnabled = prayerAlertsEnabled[type] ?: type.isObligatoryAdhan
            )
        }

        return PrayerDaySchedule(
            dateKey = dateKey,
            cityName = cityName,
            hijriDate = hijriInfo,
            gregorianDateArabic = gregorianAr,
            entries = entries,
            source = source
        )
    }

    fun calculateQibla(latitude: Double, longitude: Double): QiblaInfo {
        val phi1 = Math.toRadians(latitude)
        val phi2 = Math.toRadians(KAABA_LAT)
        val dLambda = Math.toRadians(KAABA_LNG - longitude)

        val y = sin(dLambda) * cos(phi2)
        val x = cos(phi1) * sin(phi2) - sin(phi1) * cos(phi2) * cos(dLambda)
        var bearing = Math.toDegrees(atan2(y, x))
        bearing = ((bearing % 360.0) + 360.0) % 360.0

        // Haversine distance in km
        val earthRadiusKm = 6371.0
        val dLat = phi2 - phi1
        val a = sin(dLat / 2) * sin(dLat / 2) +
            cos(phi1) * cos(phi2) * sin(dLambda / 2) * sin(dLambda / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        val distanceKm = earthRadiusKm * c

        return QiblaInfo(bearingDegrees = bearing, distanceKm = distanceKm)
    }

    fun formatHijriDate(gregorianCal: Calendar, adjustmentDays: Int): HijriDateInfo {
        val adjusted = (gregorianCal.clone() as Calendar).apply {
            add(Calendar.DAY_OF_MONTH, adjustmentDays)
        }
        val weekdayAr = WEEKDAYS_AR[gregorianCal.get(Calendar.DAY_OF_WEEK)] ?: ""

        val (day, monthIdx, year) = runCatching {
            val islamic = IslamicCalendar()
            islamic.calculationType = IslamicCalendar.CalculationType.ISLAMIC_UMALQURA
            islamic.timeInMillis = adjusted.timeInMillis
            Triple(
                islamic.get(IslamicCalendar.DAY_OF_MONTH),
                islamic.get(IslamicCalendar.MONTH).coerceIn(0, 11),
                islamic.get(IslamicCalendar.YEAR)
            )
        }.getOrElse {
            computeTabularHijri(
                adjusted.get(Calendar.YEAR),
                adjusted.get(Calendar.MONTH) + 1,
                adjusted.get(Calendar.DAY_OF_MONTH)
            )
        }

        val monthAr = HIJRI_MONTHS_AR[monthIdx]
        return HijriDateInfo(
            day = day,
            monthArabic = monthAr,
            year = year,
            weekdayArabic = weekdayAr,
            formattedArabic = "$weekdayAr، $day $monthAr $year هـ"
        )
    }

    private fun computeTabularHijri(gYear: Int, gMonth: Int, gDay: Int): Triple<Int, Int, Int> {
        val jd = floor(julianDate(gYear, gMonth, gDay) + 0.5).toLong()
        val l = jd - 1948440L + 10632L
        val n = (l - 1L) / 10631L
        val l2 = l - 10631L * n + 354L
        val j = ((10985L - l2) / 5316L) * ((50L * l2) / 17719L) +
            (l2 / 5670L) * ((43L * l2) / 15238L)
        val l3 = l2 - ((30L - j) / 15L) * ((17719L * j) / 50L) -
            (j / 16L) * ((15238L * j) / 43L) + 29L
        val month = ((24L * l3) / 709L).toInt()
        val day = (l3 - (709L * month) / 24L).toInt()
        val year = (30L * n + j - 30L).toInt()
        return Triple(day, (month - 1).coerceIn(0, 11), year)
    }

    fun formatGregorianDateArabic(cal: Calendar): String {
        val fmt = SimpleDateFormat("d MMMM yyyy", Locale("ar"))
        fmt.timeZone = cal.timeZone
        return fmt.format(Date(cal.timeInMillis)) + " م"
    }

    fun formatTimeArabic(epochMillis: Long, tz: TimeZone): String {
        val cal = Calendar.getInstance(tz).apply { timeInMillis = epochMillis }
        val hour24 = cal.get(Calendar.HOUR_OF_DAY)
        val minute = cal.get(Calendar.MINUTE)
        val period = if (hour24 < 12) "ص" else "م"
        val hour12 = when {
            hour24 == 0 -> 12
            hour24 > 12 -> hour24 - 12
            else -> hour24
        }
        return String.format(Locale.US, "%d:%02d %s", hour12, minute, period)
    }

    private fun julianDate(year: Int, month: Int, day: Int): Double {
        var y = year
        var m = month
        if (m <= 2) {
            y -= 1
            m += 12
        }
        val a = floor(y / 100.0)
        val b = 2.0 - a + floor(a / 4.0)
        return floor(365.25 * (y + 4716)) + floor(30.6001 * (m + 1)) + day + b - 1524.5
    }

    private fun sunPosition(jd: Double): SolarCoordinates {
        val d = jd - 2451545.0
        val g = fixAngle(357.529 + 0.98560028 * d)
        val q = fixAngle(280.459 + 0.98564736 * d)
        val l = fixAngle(q + 1.915 * sin(Math.toRadians(g)) + 0.020 * sin(Math.toRadians(2.0 * g)))
        val e = 23.439 - 0.00000036 * d

        val ra = Math.toDegrees(
            atan2(
                cos(Math.toRadians(e)) * sin(Math.toRadians(l)),
                cos(Math.toRadians(l))
            )
        ) / 15.0
        val eqt = q / 15.0 - fixHour(ra)
        val decl = Math.toDegrees(asin(sin(Math.toRadians(e)) * sin(Math.toRadians(l))))
        return SolarCoordinates(declination = decl, equationOfTimeHours = eqt)
    }

    private fun computeSolarTransitUtc(jd0: Double, approxUtc: Double, longitude: Double): Double {
        var transit = approxUtc
        repeat(2) {
            val pos = sunPosition(jd0 + transit / 24.0)
            transit = 12.0 - longitude / 15.0 - pos.equationOfTimeHours
        }
        return transit
    }

    private fun computeSunAngleTimeUtc(
        jd0: Double,
        approxUtc: Double,
        longitude: Double,
        latitude: Double,
        angleBelowHorizon: Double,
        isMorning: Boolean
    ): Double? {
        var time = approxUtc
        repeat(2) {
            val pos = sunPosition(jd0 + time / 24.0)
            val transit = 12.0 - longitude / 15.0 - pos.equationOfTimeHours
            val cosHa = (-sin(Math.toRadians(angleBelowHorizon)) -
                sin(Math.toRadians(latitude)) * sin(Math.toRadians(pos.declination))) /
                (cos(Math.toRadians(latitude)) * cos(Math.toRadians(pos.declination)))
            if (cosHa < -1.0 || cosHa > 1.0) return null
            val haHours = Math.toDegrees(acos(cosHa)) / 15.0
            time = if (isMorning) transit - haHours else transit + haHours
        }
        return time
    }

    private fun computeAsrUtc(
        jd0: Double,
        approxUtc: Double,
        longitude: Double,
        latitude: Double,
        shadowFactor: Int
    ): Double {
        var time = approxUtc
        repeat(2) {
            val pos = sunPosition(jd0 + time / 24.0)
            val transit = 12.0 - longitude / 15.0 - pos.equationOfTimeHours
            val targetAngle = Math.toDegrees(
                atan(1.0 / (shadowFactor + tan(Math.toRadians(abs(latitude - pos.declination)))))
            )
            val cosHa = (sin(Math.toRadians(targetAngle)) -
                sin(Math.toRadians(latitude)) * sin(Math.toRadians(pos.declination))) /
                (cos(Math.toRadians(latitude)) * cos(Math.toRadians(pos.declination)))
            val clamped = cosHa.coerceIn(-1.0, 1.0)
            val haHours = Math.toDegrees(acos(clamped)) / 15.0
            time = transit + haHours
        }
        return time
    }

    private fun applyHighLatitudeRule(
        rawTime: Double?,
        referenceSunsetOrSunrise: Double,
        nightDuration: Double,
        angle: Double,
        rule: HighLatitudeRule,
        isFajr: Boolean
    ): Double {
        val portion = when (rule) {
            HighLatitudeRule.MIDDLE_OF_THE_NIGHT -> 0.5
            HighLatitudeRule.SEVENTH_OF_THE_NIGHT -> 1.0 / 7.0
            HighLatitudeRule.TWILIGHT_ANGLE -> angle / 60.0
        }
        val maxDiff = portion * nightDuration
        if (rawTime == null) {
            return if (isFajr) referenceSunsetOrSunrise - maxDiff else referenceSunsetOrSunrise + maxDiff
        }
        val diff = if (isFajr) referenceSunsetOrSunrise - rawTime else rawTime - referenceSunsetOrSunrise
        return if (diff < 0 || diff > maxDiff) {
            if (isFajr) referenceSunsetOrSunrise - maxDiff else referenceSunsetOrSunrise + maxDiff
        } else {
            rawTime
        }
    }

    private fun fixAngle(a: Double): Double {
        val res = a % 360.0
        return if (res < 0) res + 360.0 else res
    }

    private fun fixHour(h: Double): Double {
        val res = h % 24.0
        return if (res < 0) res + 24.0 else res
    }
}
