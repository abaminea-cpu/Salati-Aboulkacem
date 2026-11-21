package com.example.alarms

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.MawaqitApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.TimeZone

class BootAndTimeChangeReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        val relevant = action == Intent.ACTION_BOOT_COMPLETED ||
            action == Intent.ACTION_LOCKED_BOOT_COMPLETED ||
            action == Intent.ACTION_TIMEZONE_CHANGED ||
            action == Intent.ACTION_TIME_CHANGED ||
            action == Intent.ACTION_MY_PACKAGE_REPLACED ||
            action == "android.app.action.SCHEDULE_EXACT_ALARM_PERMISSION_STATE_CHANGED"

        if (!relevant) return

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val app = context.applicationContext as MawaqitApp
                val currentPrefs = app.preferencesRepository.preferencesFlow.first()

                // If GPS location mode is active and timezone changed, sync timezone ID
                val updatedPrefs = if (action == Intent.ACTION_TIMEZONE_CHANGED && currentPrefs.useGpsLocation) {
                    val sysTz = TimeZone.getDefault().id
                    app.preferencesRepository.updateLocation(
                        latitude = currentPrefs.latitude,
                        longitude = currentPrefs.longitude,
                        cityName = currentPrefs.cityName,
                        countryCode = currentPrefs.countryCode,
                        timezoneId = sysTz,
                        isGps = true
                    )
                    currentPrefs.copy(timezoneId = sysTz)
                } else {
                    currentPrefs
                }

                app.alarmScheduler.rescheduleAll(updatedPrefs)
            } finally {
                pendingResult.finish()
            }
        }
    }
}
