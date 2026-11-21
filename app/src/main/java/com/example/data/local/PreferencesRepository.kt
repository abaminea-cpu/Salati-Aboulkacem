package com.example.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.domain.model.CalculationMethod
import com.example.domain.model.HighLatitudeRule
import com.example.domain.model.Madhab
import com.example.domain.model.MuezzinOption
import com.example.domain.model.PrayerType
import com.example.domain.model.ThemeMode
import com.example.domain.model.UserPreferencesState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "mawaqit_prefs")

class PreferencesRepository(private val context: Context) {

    private object Keys {
        val LATITUDE = doublePreferencesKey("latitude")
        val LONGITUDE = doublePreferencesKey("longitude")
        val CITY_NAME = stringPreferencesKey("city_name")
        val COUNTRY_CODE = stringPreferencesKey("country_code")
        val TIMEZONE_ID = stringPreferencesKey("timezone_id")
        val USE_GPS = booleanPreferencesKey("use_gps")
        val AUTO_METHOD_BY_COUNTRY = booleanPreferencesKey("auto_method_by_country")
        val CALCULATION_METHOD_ID = intPreferencesKey("calculation_method_id")
        val MADHAB_ID = intPreferencesKey("madhab_id")
        val HIGH_LAT_RULE = stringPreferencesKey("high_lat_rule")
        val HIJRI_ADJUSTMENT = intPreferencesKey("hijri_adjustment")
        val PREFER_API_SYNC = booleanPreferencesKey("prefer_api_sync")
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val MUEZZIN_ID = stringPreferencesKey("muezzin_id")
        val PRE_ADHAN_MINUTES = intPreferencesKey("pre_adhan_minutes")
        val MORNING_ADHKAR_ENABLED = booleanPreferencesKey("morning_adhkar_enabled")
        val MORNING_ADHKAR_HOUR = intPreferencesKey("morning_adhkar_hour")
        val MORNING_ADHKAR_MINUTE = intPreferencesKey("morning_adhkar_minute")
        val EVENING_ADHKAR_ENABLED = booleanPreferencesKey("evening_adhkar_enabled")
        val EVENING_ADHKAR_HOUR = intPreferencesKey("evening_adhkar_hour")
        val EVENING_ADHKAR_MINUTE = intPreferencesKey("evening_adhkar_minute")
        val TASBIH_COUNT = intPreferencesKey("tasbih_count")
        val TASBIH_PHRASE_INDEX = intPreferencesKey("tasbih_phrase_index")

        fun offsetKey(type: PrayerType) = intPreferencesKey("offset_${type.id}")
        fun alertKey(type: PrayerType) = booleanPreferencesKey("alert_${type.id}")
    }

    val preferencesFlow: Flow<UserPreferencesState> = context.dataStore.data.map { prefs ->
        val offsets = PrayerType.entries.associateWith { type ->
            prefs[Keys.offsetKey(type)] ?: 0
        }
        val alerts = PrayerType.entries.associateWith { type ->
            prefs[Keys.alertKey(type)] ?: type.isObligatoryAdhan
        }
        UserPreferencesState(
            latitude = prefs[Keys.LATITUDE] ?: 21.4225,
            longitude = prefs[Keys.LONGITUDE] ?: 39.8262,
            cityName = prefs[Keys.CITY_NAME] ?: "مكة المكرمة",
            countryCode = prefs[Keys.COUNTRY_CODE] ?: "SA",
            timezoneId = prefs[Keys.TIMEZONE_ID] ?: "Asia/Riyadh",
            useGpsLocation = prefs[Keys.USE_GPS] ?: false,
            autoMethodByCountry = prefs[Keys.AUTO_METHOD_BY_COUNTRY] ?: true,
            calculationMethod = CalculationMethod.fromApiId(
                prefs[Keys.CALCULATION_METHOD_ID] ?: CalculationMethod.UMM_AL_QURA.apiId
            ),
            madhab = Madhab.fromSchoolId(prefs[Keys.MADHAB_ID] ?: 0),
            highLatitudeRule = prefs[Keys.HIGH_LAT_RULE]?.let { name ->
                HighLatitudeRule.entries.firstOrNull { it.name == name }
            } ?: HighLatitudeRule.MIDDLE_OF_THE_NIGHT,
            manualOffsets = offsets,
            hijriAdjustmentDays = prefs[Keys.HIJRI_ADJUSTMENT] ?: 0,
            preferApiSync = prefs[Keys.PREFER_API_SYNC] ?: true,
            themeMode = ThemeMode.fromId(prefs[Keys.THEME_MODE] ?: ThemeMode.SYSTEM.id),
            selectedMuezzin = MuezzinOption.fromId(prefs[Keys.MUEZZIN_ID] ?: MuezzinOption.OFFLINE_CHIME.id),
            prayerAlertsEnabled = alerts,
            preAdhanMinutes = prefs[Keys.PRE_ADHAN_MINUTES] ?: 0,
            morningAdhkarEnabled = prefs[Keys.MORNING_ADHKAR_ENABLED] ?: true,
            morningAdhkarHour = prefs[Keys.MORNING_ADHKAR_HOUR] ?: 6,
            morningAdhkarMinute = prefs[Keys.MORNING_ADHKAR_MINUTE] ?: 0,
            eveningAdhkarEnabled = prefs[Keys.EVENING_ADHKAR_ENABLED] ?: true,
            eveningAdhkarHour = prefs[Keys.EVENING_ADHKAR_HOUR] ?: 17,
            eveningAdhkarMinute = prefs[Keys.EVENING_ADHKAR_MINUTE] ?: 0,
            tasbihCount = prefs[Keys.TASBIH_COUNT] ?: 0,
            tasbihPhraseIndex = prefs[Keys.TASBIH_PHRASE_INDEX] ?: 0
        )
    }

    suspend fun updateLocation(
        latitude: Double,
        longitude: Double,
        cityName: String,
        countryCode: String,
        timezoneId: String,
        isGps: Boolean
    ) {
        context.dataStore.edit { prefs ->
            prefs[Keys.LATITUDE] = latitude
            prefs[Keys.LONGITUDE] = longitude
            prefs[Keys.CITY_NAME] = cityName
            prefs[Keys.COUNTRY_CODE] = countryCode
            prefs[Keys.TIMEZONE_ID] = timezoneId
            prefs[Keys.USE_GPS] = isGps
            val autoMethod = prefs[Keys.AUTO_METHOD_BY_COUNTRY] ?: true
            if (autoMethod && countryCode.isNotBlank()) {
                prefs[Keys.CALCULATION_METHOD_ID] = CalculationMethod.forCountryCode(countryCode).apiId
            }
        }
    }

    suspend fun setCalculationMethod(method: CalculationMethod) {
        context.dataStore.edit { prefs ->
            prefs[Keys.CALCULATION_METHOD_ID] = method.apiId
        }
    }

    suspend fun setAutoMethodByCountry(enabled: Boolean, currentCountryCode: String) {
        context.dataStore.edit { prefs ->
            prefs[Keys.AUTO_METHOD_BY_COUNTRY] = enabled
            if (enabled && currentCountryCode.isNotBlank()) {
                prefs[Keys.CALCULATION_METHOD_ID] = CalculationMethod.forCountryCode(currentCountryCode).apiId
            }
        }
    }

    suspend fun setMadhab(madhab: Madhab) {
        context.dataStore.edit { prefs ->
            prefs[Keys.MADHAB_ID] = madhab.schoolId
        }
    }

    suspend fun setManualOffset(type: PrayerType, minutes: Int) {
        context.dataStore.edit { prefs ->
            prefs[Keys.offsetKey(type)] = minutes.coerceIn(-30, 30)
        }
    }

    suspend fun setHijriAdjustment(days: Int) {
        context.dataStore.edit { prefs ->
            prefs[Keys.HIJRI_ADJUSTMENT] = days.coerceIn(-2, 2)
        }
    }

    suspend fun setPreferApiSync(prefer: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[Keys.PREFER_API_SYNC] = prefer
        }
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.dataStore.edit { prefs ->
            prefs[Keys.THEME_MODE] = mode.id
        }
    }

    suspend fun setSelectedMuezzin(option: MuezzinOption) {
        context.dataStore.edit { prefs ->
            prefs[Keys.MUEZZIN_ID] = option.id
        }
    }

    suspend fun setPrayerAlertEnabled(type: PrayerType, enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[Keys.alertKey(type)] = enabled
        }
    }

    suspend fun setPreAdhanMinutes(minutes: Int) {
        context.dataStore.edit { prefs ->
            prefs[Keys.PRE_ADHAN_MINUTES] = minutes
        }
    }

    suspend fun setMorningAdhkarReminder(enabled: Boolean, hour: Int, minute: Int) {
        context.dataStore.edit { prefs ->
            prefs[Keys.MORNING_ADHKAR_ENABLED] = enabled
            prefs[Keys.MORNING_ADHKAR_HOUR] = hour
            prefs[Keys.MORNING_ADHKAR_MINUTE] = minute
        }
    }

    suspend fun setEveningAdhkarReminder(enabled: Boolean, hour: Int, minute: Int) {
        context.dataStore.edit { prefs ->
            prefs[Keys.EVENING_ADHKAR_ENABLED] = enabled
            prefs[Keys.EVENING_ADHKAR_HOUR] = hour
            prefs[Keys.EVENING_ADHKAR_MINUTE] = minute
        }
    }

    suspend fun updateTasbih(count: Int, phraseIndex: Int) {
        context.dataStore.edit { prefs ->
            prefs[Keys.TASBIH_COUNT] = count
            prefs[Keys.TASBIH_PHRASE_INDEX] = phraseIndex
        }
    }
}
