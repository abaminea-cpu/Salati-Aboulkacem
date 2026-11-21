package com.example.domain.model

enum class PrayerType(
    val id: String,
    val arabicName: String,
    val isObligatoryAdhan: Boolean
) {
    FAJR("fajr", "الفجر", true),
    SUNRISE("sunrise", "الشروق", false),
    DHUHR("dhuhr", "الظهر", true),
    ASR("asr", "العصر", true),
    MAGHRIB("maghrib", "المغرب", true),
    ISHA("isha", "العشاء", true);

    companion object {
        fun fromId(id: String): PrayerType = entries.firstOrNull { it.id == id } ?: FAJR
    }
}

enum class CalculationMethod(
    val apiId: Int,
    val arabicName: String,
    val fajrAngle: Double,
    val ishaAngle: Double,
    val ishaIntervalMinutes: Int? = null
) {
    MWL(
        apiId = 3,
        arabicName = "رابطة العالم الإسلامي",
        fajrAngle = 18.0,
        ishaAngle = 17.0
    ),
    UMM_AL_QURA(
        apiId = 4,
        arabicName = "أم القرى — مكة المكرمة",
        fajrAngle = 18.5,
        ishaAngle = 0.0,
        ishaIntervalMinutes = 90
    ),
    EGYPT(
        apiId = 5,
        arabicName = "الهيئة المصرية العامة للمساحة",
        fajrAngle = 19.5,
        ishaAngle = 17.5
    ),
    ALGERIA(
        apiId = 19,
        arabicName = "وزارة الشؤون الدينية — الجزائر",
        fajrAngle = 18.0,
        ishaAngle = 17.0
    ),
    MOROCCO(
        apiId = 21,
        arabicName = "وزارة الأوقاف — المغرب",
        fajrAngle = 19.0,
        ishaAngle = 17.0
    ),
    TUNISIA(
        apiId = 18,
        arabicName = "وزارة الشؤون الدينية — تونس",
        fajrAngle = 18.0,
        ishaAngle = 18.0
    ),
    ISNA(
        apiId = 2,
        arabicName = "الجمعية الإسلامية بأمريكا الشمالية (ISNA)",
        fajrAngle = 15.0,
        ishaAngle = 15.0
    ),
    KARACHI(
        apiId = 1,
        arabicName = "جامعة العلوم الإسلامية بكراتشي",
        fajrAngle = 18.0,
        ishaAngle = 18.0
    );

    companion object {
        fun fromApiId(id: Int): CalculationMethod = entries.firstOrNull { it.apiId == id } ?: MWL

        fun forCountryCode(countryCode: String): CalculationMethod {
            return when (countryCode.uppercase()) {
                "SA", "YE", "QA", "KW", "BH", "OM" -> UMM_AL_QURA
                "EG", "SD", "LY", "SY", "LB", "IQ", "PS", "JO" -> EGYPT
                "DZ" -> ALGERIA
                "MA" -> MOROCCO
                "TN" -> TUNISIA
                "US", "CA" -> ISNA
                "PK", "IN", "BD", "AF" -> KARACHI
                else -> MWL
            }
        }
    }
}

enum class Madhab(val schoolId: Int, val arabicName: String, val shadowFactor: Int) {
    SHAFI(0, "الشافعي والمالكي والحنبلي (الجمهور)", 1),
    HANAFI(1, "الحنفي", 2);

    companion object {
        fun fromSchoolId(id: Int): Madhab = if (id == 1) HANAFI else SHAFI
    }
}

enum class HighLatitudeRule(val arabicName: String) {
    MIDDLE_OF_THE_NIGHT("منتصف الليل"),
    SEVENTH_OF_THE_NIGHT("سُبع الليل"),
    TWILIGHT_ANGLE("زاوية الشفق")
}

enum class ThemeMode(val id: String, val arabicName: String) {
    SYSTEM("system", "تلقائي حسب النظام"),
    LIGHT("light", "الوضع النهاري"),
    DARK("dark", "الوضع الليلي");

    companion object {
        fun fromId(id: String): ThemeMode = entries.firstOrNull { it.id == id } ?: SYSTEM
    }
}

enum class MuezzinOption(
    val id: String,
    val arabicName: String,
    val subtitle: String,
    val streamUrl: String?
) {
    OFFLINE_CHIME(
        id = "offline_chime",
        arabicName = "تكبير ونغمة حجازية هادئة (دون إنترنت)",
        subtitle = "يعمل دائماً بدون اتصال بالإنترنت",
        streamUrl = null
    ),
    ALAFASY(
        id = "alafasy",
        arabicName = "الشيخ مشاري راشد العفاسي",
        subtitle = "أذان هادئ مع بديل محلي عند انقطاع الإنترنت",
        streamUrl = "https://www.islamcan.com/audio/adhan/azan1.mp3"
    ),
    MAKKAH(
        id = "makkah",
        arabicName = "أذان الحرم المكي — الشيخ علي ملا",
        subtitle = "أذان المسجد الحرام مع بديل محلي عند انقطاع الإنترنت",
        streamUrl = "https://www.islamcan.com/audio/adhan/azan2.mp3"
    ),
    MADINAH(
        id = "madinah",
        arabicName = "أذان المسجد النبوي الشريف",
        subtitle = "أذان الحرم النبوي مع بديل محلي عند انقطاع الإنترنت",
        streamUrl = "https://www.islamcan.com/audio/adhan/azan3.mp3"
    ),
    AL_AQSA(
        id = "alaqsa",
        arabicName = "أذان المسجد الأقصى المبارك",
        subtitle = "أذان القدس الشريف مع بديل محلي عند انقطاع الإنترنت",
        streamUrl = "https://www.islamcan.com/audio/adhan/azan4.mp3"
    );

    companion object {
        fun fromId(id: String): MuezzinOption = entries.firstOrNull { it.id == id } ?: OFFLINE_CHIME
    }
}

data class CityPreset(
    val id: String,
    val arabicName: String,
    val countryArabic: String,
    val countryCode: String,
    val latitude: Double,
    val longitude: Double,
    val timezoneId: String
)

object CityCatalog {
    val cities = listOf(
        CityPreset("makkah", "مكة المكرمة", "السعودية", "SA", 21.4225, 39.8262, "Asia/Riyadh"),
        CityPreset("madinah", "المدينة المنورة", "السعودية", "SA", 24.4672, 39.6111, "Asia/Riyadh"),
        CityPreset("riyadh", "الرياض", "السعودية", "SA", 24.7136, 46.6753, "Asia/Riyadh"),
        CityPreset("jeddah", "جدة", "السعودية", "SA", 21.5433, 39.1728, "Asia/Riyadh"),
        CityPreset("cairo", "القاهرة", "مصر", "EG", 30.0444, 31.2357, "Africa/Cairo"),
        CityPreset("alexandria", "الإسكندرية", "مصر", "EG", 31.2001, 29.9187, "Africa/Cairo"),
        CityPreset("algiers", "الجزائر العاصمة", "الجزائر", "DZ", 36.7538, 3.0588, "Africa/Algiers"),
        CityPreset("oran", "وهران", "الجزائر", "DZ", 35.6971, -0.6308, "Africa/Algiers"),
        CityPreset("rabat", "الرباط", "المغرب", "MA", 34.0209, -6.8416, "Africa/Casablanca"),
        CityPreset("casablanca", "الدار البيضاء", "المغرب", "MA", 33.5731, -7.5898, "Africa/Casablanca"),
        CityPreset("tunis", "تونس العاصمة", "تونس", "TN", 36.8065, 10.1815, "Africa/Tunis"),
        CityPreset("tripoli", "طرابلس", "ليبيا", "LY", 32.8872, 13.1913, "Africa/Tripoli"),
        CityPreset("khartoum", "الخرطوم", "السودان", "SD", 15.5007, 32.5599, "Africa/Khartoum"),
        CityPreset("jerusalem", "القدس الشريف", "فلسطين", "PS", 31.7683, 35.2137, "Asia/Jerusalem"),
        CityPreset("amman", "عمّان", "الأردن", "JO", 31.9454, 35.9284, "Asia/Amman"),
        CityPreset("damascus", "دمشق", "سوريا", "SY", 33.5138, 36.2765, "Asia/Damascus"),
        CityPreset("baghdad", "بغداد", "العراق", "IQ", 33.3152, 44.3661, "Asia/Baghdad"),
        CityPreset("beirut", "بيروت", "لبنان", "LB", 33.8938, 35.5018, "Asia/Beirut"),
        CityPreset("doha", "الدوحة", "قطر", "QA", 25.2854, 51.5310, "Asia/Qatar"),
        CityPreset("dubai", "دبي", "الإمارات", "AE", 25.2048, 55.2708, "Asia/Dubai"),
        CityPreset("abudhabi", "أبو ظبي", "الإمارات", "AE", 24.4539, 54.3773, "Asia/Dubai"),
        CityPreset("kuwait", "مدينة الكويت", "الكويت", "KW", 29.3759, 47.9774, "Asia/Kuwait"),
        CityPreset("manama", "المنامة", "البحرين", "BH", 26.2285, 50.5860, "Asia/Bahrain"),
        CityPreset("muscat", "مسقط", "عُمان", "OM", 23.5880, 58.3829, "Asia/Muscat"),
        CityPreset("sanaa", "صنعاء", "اليمن", "YE", 15.3694, 44.1910, "Asia/Aden"),
        CityPreset("istanbul", "إسطنبول", "تركيا", "TR", 41.0082, 28.9784, "Europe/Istanbul"),
        CityPreset("kualalumpur", "كوالالمبور", "ماليزيا", "MY", 3.1390, 101.6869, "Asia/Kuala_Lumpur"),
        CityPreset("jakarta", "جاكرتا", "إندونيسيا", "ID", -6.2088, 106.8456, "Asia/Jakarta"),
        CityPreset("london", "لندن", "المملكة المتحدة", "GB", 51.5072, -0.1276, "Europe/London"),
        CityPreset("paris", "باريس", "فرنسا", "FR", 48.8566, 2.3522, "Europe/Paris"),
        CityPreset("newyork", "نيويورك", "الولايات المتحدة", "US", 40.7128, -74.0060, "America/New_York")
    )
}

data class PrayerTimeEntry(
    val type: PrayerType,
    val epochMillis: Long,
    val formattedTime: String,
    val offsetMinutes: Int,
    val isAlertEnabled: Boolean
)

data class HijriDateInfo(
    val day: Int,
    val monthArabic: String,
    val year: Int,
    val weekdayArabic: String,
    val formattedArabic: String
)

enum class PrayerDataSource(val arabicLabel: String) {
    LOCAL_ADHAN("محسوب محلياً (دون إنترنت)"),
    ALADHAN_API("متزامن مع AlAdhan")
}

data class PrayerDaySchedule(
    val dateKey: String,
    val cityName: String,
    val hijriDate: HijriDateInfo,
    val gregorianDateArabic: String,
    val entries: List<PrayerTimeEntry>,
    val source: PrayerDataSource
)

data class QiblaInfo(
    val bearingDegrees: Double,
    val distanceKm: Double
)

data class AdhkarCategory(
    val id: String,
    val title: String,
    val subtitle: String,
    val iconKey: String,
    val totalCount: Int,
    val completedCount: Int
)

data class DhikrWithProgress(
    val id: String,
    val categoryId: String,
    val title: String,
    val arabicText: String,
    val referenceText: String,
    val targetCount: Int,
    val audioUrl: String?,
    val orderIndex: Int,
    val currentCount: Int,
    val isFavorite: Boolean
) {
    val isCompleted: Boolean get() = currentCount >= targetCount
    val remainingCount: Int get() = (targetCount - currentCount).coerceAtLeast(0)
}

data class UserPreferencesState(
    val latitude: Double = 21.4225,
    val longitude: Double = 39.8262,
    val cityName: String = "مكة المكرمة",
    val countryCode: String = "SA",
    val timezoneId: String = "Asia/Riyadh",
    val useGpsLocation: Boolean = false,
    val autoMethodByCountry: Boolean = true,
    val calculationMethod: CalculationMethod = CalculationMethod.UMM_AL_QURA,
    val madhab: Madhab = Madhab.SHAFI,
    val highLatitudeRule: HighLatitudeRule = HighLatitudeRule.MIDDLE_OF_THE_NIGHT,
    val manualOffsets: Map<PrayerType, Int> = PrayerType.entries.associateWith { 0 },
    val hijriAdjustmentDays: Int = 0,
    val preferApiSync: Boolean = true,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val selectedMuezzin: MuezzinOption = MuezzinOption.OFFLINE_CHIME,
    val prayerAlertsEnabled: Map<PrayerType, Boolean> = PrayerType.entries.associateWith { it != PrayerType.SUNRISE },
    val preAdhanMinutes: Int = 0,
    val morningAdhkarEnabled: Boolean = true,
    val morningAdhkarHour: Int = 6,
    val morningAdhkarMinute: Int = 0,
    val eveningAdhkarEnabled: Boolean = true,
    val eveningAdhkarHour: Int = 17,
    val eveningAdhkarMinute: Int = 0,
    val tasbihCount: Int = 0,
    val tasbihPhraseIndex: Int = 0
)
