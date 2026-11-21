package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import android.content.Intent
import android.hardware.GeomagneticField
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.MawaqitApp
import com.example.audio.AdhanPlaybackService
import com.example.audio.AdhkarPlaybackState
import com.example.data.location.ResolvedLocation
import com.example.domain.model.AdhkarCategory
import com.example.domain.model.CalculationMethod
import com.example.domain.model.CityPreset
import com.example.domain.model.DhikrWithProgress
import com.example.domain.model.Madhab
import com.example.domain.model.MuezzinOption
import com.example.domain.model.PrayerDaySchedule
import com.example.domain.model.PrayerTimeEntry
import com.example.domain.model.PrayerType
import com.example.domain.model.QiblaInfo
import com.example.domain.model.ThemeMode
import com.example.domain.model.UserPreferencesState
import com.example.domain.prayer.AdhanCalculator
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone
import kotlin.math.abs

data class NextPrayerCountdown(
    val nextPrayerName: String,
    val nextPrayerTimeFormatted: String,
    val nextPrayerType: PrayerType,
    val remainingFormatted: String,
    val progressFraction: Float,
    val isTomorrow: Boolean
)

data class SystemPermissionStatus(
    val hasNotificationPermission: Boolean = true,
    val canScheduleExactAlarms: Boolean = true,
    val isIgnoringBatteryOptimization: Boolean = false
)

data class CompassState(
    val azimuthDegrees: Float = 0f,
    val hasSensor: Boolean = true,
    val qiblaInfo: QiblaInfo = QiblaInfo(0.0, 0.0),
    val isAligned: Boolean = false
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as MawaqitApp
    private val prefsRepo = app.preferencesRepository
    private val prayerRepo = app.prayerRepository
    private val adhkarRepo = app.adhkarRepository
    private val locationHelper = app.locationHelper
    private val alarmScheduler = app.alarmScheduler
    private val audioPlayer = app.adhkarAudioPlayer

    val preferences: StateFlow<UserPreferencesState> = prefsRepo.preferencesFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, UserPreferencesState())

    private val _todaySchedule = MutableStateFlow<PrayerDaySchedule?>(null)
    val todaySchedule: StateFlow<PrayerDaySchedule?> = _todaySchedule.asStateFlow()

    private val _tomorrowSchedule = MutableStateFlow<PrayerDaySchedule?>(null)
    val tomorrowSchedule: StateFlow<PrayerDaySchedule?> = _tomorrowSchedule.asStateFlow()

    private val _nowMillis = MutableStateFlow(System.currentTimeMillis())

    val countdownState: StateFlow<NextPrayerCountdown?> = combine(
        _todaySchedule,
        _tomorrowSchedule,
        _nowMillis
    ) { today, tomorrow, now ->
        computeCountdown(today, tomorrow, now)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val adhkarCategories: StateFlow<List<AdhkarCategory>> = adhkarRepo.categoriesFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allAdhkar: StateFlow<List<DhikrWithProgress>> = adhkarRepo.allItemsWithProgressFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val adhkarPlaybackState: StateFlow<AdhkarPlaybackState> = audioPlayer.state
    val isAdhanPlaying: StateFlow<Boolean> = AdhanPlaybackService.isAdhanPlaying
    val playingMuezzinId: StateFlow<String?> = AdhanPlaybackService.playingMuezzinId

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _statusMessage = MutableStateFlow<String?>(null)
    val statusMessage: StateFlow<String?> = _statusMessage.asStateFlow()

    private val _citySearchResults = MutableStateFlow<List<ResolvedLocation>>(emptyList())
    val citySearchResults: StateFlow<List<ResolvedLocation>> = _citySearchResults.asStateFlow()

    private val _permissionStatus = MutableStateFlow(SystemPermissionStatus())
    val permissionStatus: StateFlow<SystemPermissionStatus> = _permissionStatus.asStateFlow()

    private val _compassAzimuth = MutableStateFlow(0f)
    private val _hasCompassSensor = MutableStateFlow(true)

    val compassState: StateFlow<CompassState> = combine(
        preferences,
        _compassAzimuth,
        _hasCompassSensor
    ) { prefs, azimuth, hasSensor ->
        val qibla = AdhanCalculator.calculateQibla(prefs.latitude, prefs.longitude)
        val delta = shortestAngleDiff(azimuth.toDouble(), qibla.bearingDegrees)
        CompassState(
            azimuthDegrees = azimuth,
            hasSensor = hasSensor,
            qiblaInfo = qibla,
            isAligned = hasSensor && abs(delta) <= 3.5
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), CompassState())

    private val sensorManager = application.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private var sensorListener: SensorEventListener? = null

    init {
        refreshPermissionStatus()
        viewModelScope.launch {
            preferences.collectLatest { prefs ->
                refreshSchedules(prefs, forceApi = false)
                alarmScheduler.rescheduleAll(prefs)
            }
        }
        viewModelScope.launch {
            while (true) {
                val now = System.currentTimeMillis()
                _nowMillis.value = now
                // Refresh day schedule automatically if midnight passed
                val prefs = preferences.value
                val tz = TimeZone.getTimeZone(prefs.timezoneId)
                val cal = Calendar.getInstance(tz).apply { timeInMillis = now }
                val expectedKey = String.format(
                    Locale.US,
                    "%04d-%02d-%02d",
                    cal.get(Calendar.YEAR),
                    cal.get(Calendar.MONTH) + 1,
                    cal.get(Calendar.DAY_OF_MONTH)
                )
                if (_todaySchedule.value != null && _todaySchedule.value?.dateKey != expectedKey) {
                    refreshSchedules(prefs, forceApi = false)
                    alarmScheduler.rescheduleAll(prefs)
                }
                delay(1000L)
            }
        }
    }

    fun refreshPermissionStatus() {
        _permissionStatus.value = SystemPermissionStatus(
            hasNotificationPermission = alarmScheduler.hasNotificationPermission(),
            canScheduleExactAlarms = alarmScheduler.canScheduleExactAlarms(),
            isIgnoringBatteryOptimization = alarmScheduler.isIgnoringBatteryOptimizations()
        )
    }

    fun refreshSchedulesFromApi() {
        viewModelScope.launch {
            refreshSchedules(preferences.value, forceApi = true)
        }
    }

    private suspend fun refreshSchedules(prefs: UserPreferencesState, forceApi: Boolean) {
        _isRefreshing.value = true
        try {
            val tz = TimeZone.getTimeZone(prefs.timezoneId)
            val todayCal = Calendar.getInstance(tz)
            val tomorrowCal = (todayCal.clone() as Calendar).apply { add(Calendar.DAY_OF_MONTH, 1) }

            val today = prayerRepo.getScheduleForDate(todayCal, prefs, forceRefreshApi = forceApi)
            val tomorrow = prayerRepo.getScheduleForDate(tomorrowCal, prefs, forceRefreshApi = forceApi)
            _todaySchedule.value = today
            _tomorrowSchedule.value = tomorrow
        } finally {
            _isRefreshing.value = false
        }
    }

    fun detectGpsLocation() {
        viewModelScope.launch {
            _isRefreshing.value = true
            val result = locationHelper.getCurrentGpsLocation()
            result.onSuccess { resolved ->
                prefsRepo.updateLocation(
                    latitude = resolved.latitude,
                    longitude = resolved.longitude,
                    cityName = resolved.cityName,
                    countryCode = resolved.countryCode,
                    timezoneId = resolved.timezoneId,
                    isGps = true
                )
                _statusMessage.value = "تم تحديث الموقع: ${resolved.cityName}"
            }.onFailure {
                _statusMessage.value = "تعذر تحديد موقع GPS، يمكنك اختيار المدينة يدوياً"
            }
            _isRefreshing.value = false
        }
    }

    fun selectPresetCity(city: CityPreset) {
        viewModelScope.launch {
            prefsRepo.updateLocation(
                latitude = city.latitude,
                longitude = city.longitude,
                cityName = city.arabicName,
                countryCode = city.countryCode,
                timezoneId = city.timezoneId,
                isGps = false
            )
            _statusMessage.value = "تم اختيار ${city.arabicName}"
        }
    }

    fun selectResolvedLocation(loc: ResolvedLocation) {
        viewModelScope.launch {
            prefsRepo.updateLocation(
                latitude = loc.latitude,
                longitude = loc.longitude,
                cityName = loc.cityName,
                countryCode = loc.countryCode,
                timezoneId = loc.timezoneId,
                isGps = false
            )
            _statusMessage.value = "تم اختيار ${loc.cityName}"
        }
    }

    fun searchCity(query: String) {
        viewModelScope.launch {
            _citySearchResults.value = if (query.trim().length >= 2) {
                locationHelper.searchCityByName(query)
            } else {
                emptyList()
            }
        }
    }

    fun setCalculationMethod(method: CalculationMethod) {
        viewModelScope.launch { prefsRepo.setCalculationMethod(method) }
    }

    fun setAutoMethodByCountry(enabled: Boolean) {
        viewModelScope.launch {
            prefsRepo.setAutoMethodByCountry(enabled, preferences.value.countryCode)
        }
    }

    fun setMadhab(madhab: Madhab) {
        viewModelScope.launch { prefsRepo.setMadhab(madhab) }
    }

    fun adjustManualOffset(type: PrayerType, deltaMinutes: Int) {
        viewModelScope.launch {
            val current = preferences.value.manualOffsets[type] ?: 0
            prefsRepo.setManualOffset(type, current + deltaMinutes)
        }
    }

    fun setHijriAdjustment(days: Int) {
        viewModelScope.launch { prefsRepo.setHijriAdjustment(days) }
    }

    fun setPreferApiSync(prefer: Boolean) {
        viewModelScope.launch { prefsRepo.setPreferApiSync(prefer) }
    }

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch { prefsRepo.setThemeMode(mode) }
    }

    fun setPrayerAlertEnabled(type: PrayerType, enabled: Boolean) {
        viewModelScope.launch { prefsRepo.setPrayerAlertEnabled(type, enabled) }
    }

    fun setPreAdhanMinutes(minutes: Int) {
        viewModelScope.launch { prefsRepo.setPreAdhanMinutes(minutes) }
    }

    fun selectMuezzin(option: MuezzinOption) {
        viewModelScope.launch { prefsRepo.setSelectedMuezzin(option) }
    }

    fun previewOrStopMuezzin(option: MuezzinOption) {
        if (isAdhanPlaying.value && playingMuezzinId.value == option.id) {
            AdhanPlaybackService.stopAdhan(app)
            return
        }
        val intent = Intent(app, AdhanPlaybackService::class.java).apply {
            action = AdhanPlaybackService.ACTION_PLAY_ADHAN
            putExtra(AdhanPlaybackService.EXTRA_MUEZZIN_ID, option.id)
            putExtra(AdhanPlaybackService.EXTRA_IS_PREVIEW, true)
        }
        ContextCompat.startForegroundService(app, intent)
    }

    fun setMorningAdhkarReminder(enabled: Boolean, hour: Int, minute: Int) {
        viewModelScope.launch { prefsRepo.setMorningAdhkarReminder(enabled, hour, minute) }
    }

    fun setEveningAdhkarReminder(enabled: Boolean, hour: Int, minute: Int) {
        viewModelScope.launch { prefsRepo.setEveningAdhkarReminder(enabled, hour, minute) }
    }

    fun observeAdhkarCategory(categoryId: String): Flow<List<DhikrWithProgress>> =
        adhkarRepo.observeCategoryItems(categoryId)

    fun incrementDhikr(item: DhikrWithProgress) {
        viewModelScope.launch { adhkarRepo.incrementCount(item.id, item.targetCount) }
    }

    fun resetDhikr(dhikrId: String) {
        viewModelScope.launch { adhkarRepo.resetItemCount(dhikrId) }
    }

    fun toggleDhikrFavorite(dhikrId: String) {
        viewModelScope.launch { adhkarRepo.toggleFavorite(dhikrId) }
    }

    fun resetAdhkarCategory(categoryId: String) {
        viewModelScope.launch { adhkarRepo.resetCategory(categoryId) }
    }

    fun incrementTasbih() {
        viewModelScope.launch {
            val prefs = preferences.value
            prefsRepo.updateTasbih(prefs.tasbihCount + 1, prefs.tasbihPhraseIndex)
        }
    }

    fun resetTasbih() {
        viewModelScope.launch {
            prefsRepo.updateTasbih(0, preferences.value.tasbihPhraseIndex)
        }
    }

    fun selectTasbihPhrase(index: Int) {
        viewModelScope.launch {
            prefsRepo.updateTasbih(preferences.value.tasbihCount, index)
        }
    }

    fun playDhikr(item: DhikrWithProgress, queue: List<DhikrWithProgress>) {
        audioPlayer.playDhikr(item, queue)
    }

    fun toggleAdhkarPlayPause(queue: List<DhikrWithProgress>) {
        audioPlayer.togglePlayPause(queue)
    }

    fun playNextDhikr() = audioPlayer.playNext()
    fun playPreviousDhikr() = audioPlayer.playPrevious()
    fun toggleAdhkarAutoAdvance() = audioPlayer.toggleAutoAdvance()
    fun stopAdhkarAudio() = audioPlayer.stop()

    fun clearStatusMessage() {
        _statusMessage.value = null
    }

    fun startCompass() {
        if (sensorListener != null) return
        val rotationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
        val accelSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        val magnetSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

        if (rotationSensor == null && (accelSensor == null || magnetSensor == null)) {
            _hasCompassSensor.value = false
            return
        }
        _hasCompassSensor.value = true

        val gravity = FloatArray(3)
        val geomagnetic = FloatArray(3)
        var hasGravity = false
        var hasGeomagnetic = false

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                var rawAzimuthDeg: Float? = null
                when (event.sensor.type) {
                    Sensor.TYPE_ROTATION_VECTOR -> {
                        val rotMatrix = FloatArray(9)
                        val orientation = FloatArray(3)
                        SensorManager.getRotationMatrixFromVector(rotMatrix, event.values)
                        SensorManager.getOrientation(rotMatrix, orientation)
                        rawAzimuthDeg = Math.toDegrees(orientation[0].toDouble()).toFloat()
                    }
                    Sensor.TYPE_ACCELEROMETER -> {
                        System.arraycopy(event.values, 0, gravity, 0, 3)
                        hasGravity = true
                    }
                    Sensor.TYPE_MAGNETIC_FIELD -> {
                        System.arraycopy(event.values, 0, geomagnetic, 0, 3)
                        hasGeomagnetic = true
                    }
                }

                if (rawAzimuthDeg == null && hasGravity && hasGeomagnetic) {
                    val rMat = FloatArray(9)
                    val iMat = FloatArray(9)
                    if (SensorManager.getRotationMatrix(rMat, iMat, gravity, geomagnetic)) {
                        val orientation = FloatArray(3)
                        SensorManager.getOrientation(rMat, orientation)
                        rawAzimuthDeg = Math.toDegrees(orientation[0].toDouble()).toFloat()
                    }
                }

                rawAzimuthDeg?.let { deg ->
                    val prefs = preferences.value
                    val declination = runCatching {
                        GeomagneticField(
                            prefs.latitude.toFloat(),
                            prefs.longitude.toFloat(),
                            0f,
                            System.currentTimeMillis()
                        ).declination
                    }.getOrDefault(0f)

                    val trueHeading = ((deg + declination) % 360f + 360f) % 360f
                    val current = _compassAzimuth.value
                    val diff = shortestAngleDiff(current.toDouble(), trueHeading.toDouble()).toFloat()
                    _compassAzimuth.value = ((current + diff * 0.18f) % 360f + 360f) % 360f
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }

        sensorListener = listener
        if (rotationSensor != null) {
            sensorManager.registerListener(listener, rotationSensor, SensorManager.SENSOR_DELAY_UI)
        } else {
            sensorManager.registerListener(listener, accelSensor, SensorManager.SENSOR_DELAY_UI)
            sensorManager.registerListener(listener, magnetSensor, SensorManager.SENSOR_DELAY_UI)
        }
    }

    fun stopCompass() {
        sensorListener?.let { sensorManager.unregisterListener(it) }
        sensorListener = null
    }

    override fun onCleared() {
        stopCompass()
        super.onCleared()
    }

    private fun computeCountdown(
        today: PrayerDaySchedule?,
        tomorrow: PrayerDaySchedule?,
        now: Long
    ): NextPrayerCountdown? {
        if (today == null) return null
        val todayEntries = today.entries
        val nextTodayIndex = todayEntries.indexOfFirst { it.epochMillis > now }

        val targetEntry: PrayerTimeEntry
        val prevMillis: Long
        val isTomorrow: Boolean

        if (nextTodayIndex >= 0) {
            targetEntry = todayEntries[nextTodayIndex]
            prevMillis = if (nextTodayIndex > 0) {
                todayEntries[nextTodayIndex - 1].epochMillis
            } else {
                targetEntry.epochMillis - 6 * 3600_000L
            }
            isTomorrow = false
        } else {
            val tomorrowFajr = tomorrow?.entries?.firstOrNull() ?: return null
            targetEntry = tomorrowFajr
            prevMillis = todayEntries.lastOrNull()?.epochMillis ?: (now - 3600_000L)
            isTomorrow = true
        }

        val diffMillis = (targetEntry.epochMillis - now).coerceAtLeast(0L)
        val totalSeconds = diffMillis / 1000L
        val hours = totalSeconds / 3600L
        val minutes = (totalSeconds % 3600L) / 60L
        val seconds = totalSeconds % 60L
        val formatted = String.format(Locale.US, "%02d:%02d:%02d", hours, minutes, seconds)

        val span = (targetEntry.epochMillis - prevMillis).coerceAtLeast(1L)
        val elapsed = (now - prevMillis).coerceIn(0L, span)
        val fraction = (elapsed.toFloat() / span.toFloat()).coerceIn(0f, 1f)

        return NextPrayerCountdown(
            nextPrayerName = targetEntry.type.arabicName,
            nextPrayerTimeFormatted = targetEntry.formattedTime,
            nextPrayerType = targetEntry.type,
            remainingFormatted = formatted,
            progressFraction = fraction,
            isTomorrow = isTomorrow
        )
    }

    private fun shortestAngleDiff(fromDeg: Double, toDeg: Double): Double {
        return ((toDeg - fromDeg + 540.0) % 360.0) - 180.0
    }
}
