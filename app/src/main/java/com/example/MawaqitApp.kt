package com.example

import android.app.Application
import com.example.alarms.AlarmScheduler
import com.example.alarms.NotificationChannels
import com.example.audio.AdhkarAudioPlayer
import com.example.data.local.PreferencesRepository
import com.example.data.local.db.AppDatabase
import com.example.data.location.LocationHelper
import com.example.data.remote.AlAdhanApiService
import com.example.data.repository.AdhkarRepository
import com.example.data.repository.PrayerRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MawaqitApp : Application() {

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    val database: AppDatabase by lazy { AppDatabase.getInstance(this) }
    val preferencesRepository: PreferencesRepository by lazy { PreferencesRepository(this) }
    val apiService: AlAdhanApiService by lazy { AlAdhanApiService.create() }
    val prayerRepository: PrayerRepository by lazy {
        PrayerRepository(this, database.prayerCacheDao(), apiService)
    }
    val adhkarRepository: AdhkarRepository by lazy {
        AdhkarRepository(database.adhkarDao())
    }
    val locationHelper: LocationHelper by lazy { LocationHelper(this) }
    val alarmScheduler: AlarmScheduler by lazy { AlarmScheduler(this, prayerRepository) }
    val adhkarAudioPlayer: AdhkarAudioPlayer by lazy { AdhkarAudioPlayer(this) }

    override fun onCreate() {
        super.onCreate()
        NotificationChannels.createAll(this)
        appScope.launch {
            adhkarRepository.ensureSeeded()
            val prefs = preferencesRepository.preferencesFlow.first()
            alarmScheduler.rescheduleAll(prefs)
        }
    }
}
