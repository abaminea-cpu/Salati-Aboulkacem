package com.example.alarms

import android.Manifest
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.PowerManager
import androidx.core.content.ContextCompat
import com.example.data.repository.PrayerRepository
import com.example.domain.model.PrayerType
import com.example.domain.model.UserPreferencesState
import java.util.Calendar
import java.util.TimeZone

class AlarmScheduler(
    private val context: Context,
    private val prayerRepository: PrayerRepository
) {
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun canScheduleExactAlarms(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            alarmManager.canScheduleExactAlarms()
        } else {
            true
        }
    }

    fun hasNotificationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    fun isIgnoringBatteryOptimizations(): Boolean {
        val pm = context.getSystemService(Context.POWER_SERVICE) as? PowerManager ?: return true
        return pm.isIgnoringBatteryOptimizations(context.packageName)
    }

    suspend fun rescheduleAll(prefs: UserPreferencesState) {
        NotificationChannels.createAll(context)
        cancelAllPrayerAlarms()

        val tz = TimeZone.getTimeZone(prefs.timezoneId)
        val today = Calendar.getInstance(tz)
        val tomorrow = (today.clone() as Calendar).apply { add(Calendar.DAY_OF_MONTH, 1) }

        val schedules = listOf(
            0 to prayerRepository.getScheduleForDate(today, prefs),
            1 to prayerRepository.getScheduleForDate(tomorrow, prefs)
        )

        val now = System.currentTimeMillis()
        for ((dayOffset, schedule) in schedules) {
            val baseCode = if (dayOffset == 0) 100 else 200
            val preBaseCode = if (dayOffset == 0) 300 else 400

            for (entry in schedule.entries) {
                val enabled = prefs.prayerAlertsEnabled[entry.type] ?: false
                if (!enabled) continue

                if (entry.epochMillis > now + 2_000L) {
                    val intent = Intent(context, AdhanAlarmReceiver::class.java).apply {
                        putExtra(AdhanAlarmReceiver.EXTRA_PRAYER_ID, entry.type.id)
                        putExtra(AdhanAlarmReceiver.EXTRA_PRAYER_TIME, entry.formattedTime)
                        putExtra(AdhanAlarmReceiver.EXTRA_CITY_NAME, prefs.cityName)
                        putExtra(AdhanAlarmReceiver.EXTRA_IS_PRE_ADHAN, false)
                    }
                    scheduleExact(
                        requestCode = baseCode + entry.type.ordinal,
                        triggerAtMillis = entry.epochMillis,
                        intent = intent
                    )
                }

                if (prefs.preAdhanMinutes > 0 && entry.type.isObligatoryAdhan) {
                    val preMillis = entry.epochMillis - prefs.preAdhanMinutes * 60_000L
                    if (preMillis > now + 2_000L) {
                        val preIntent = Intent(context, AdhanAlarmReceiver::class.java).apply {
                            putExtra(AdhanAlarmReceiver.EXTRA_PRAYER_ID, entry.type.id)
                            putExtra(AdhanAlarmReceiver.EXTRA_PRAYER_TIME, entry.formattedTime)
                            putExtra(AdhanAlarmReceiver.EXTRA_CITY_NAME, prefs.cityName)
                            putExtra(AdhanAlarmReceiver.EXTRA_IS_PRE_ADHAN, true)
                            putExtra(AdhanAlarmReceiver.EXTRA_PRE_MINUTES, prefs.preAdhanMinutes)
                        }
                        scheduleExact(
                            requestCode = preBaseCode + entry.type.ordinal,
                            triggerAtMillis = preMillis,
                            intent = preIntent
                        )
                    }
                }
            }
        }

        scheduleAdhkarDailyReminder(
            requestCode = REQUEST_MORNING_ADHKAR,
            categoryId = "morning",
            title = "أذكار الصباح",
            body = "حان وقت أذكار الصباح — حصّن يومك بذكر الله",
            enabled = prefs.morningAdhkarEnabled,
            hour = prefs.morningAdhkarHour,
            minute = prefs.morningAdhkarMinute,
            tz = tz
        )

        scheduleAdhkarDailyReminder(
            requestCode = REQUEST_EVENING_ADHKAR,
            categoryId = "evening",
            title = "أذكار المساء",
            body = "حان وقت أذكار المساء — طمأنينة وسكينة لقلبك",
            enabled = prefs.eveningAdhkarEnabled,
            hour = prefs.eveningAdhkarHour,
            minute = prefs.eveningAdhkarMinute,
            tz = tz
        )
    }

    private fun scheduleAdhkarDailyReminder(
        requestCode: Int,
        categoryId: String,
        title: String,
        body: String,
        enabled: Boolean,
        hour: Int,
        minute: Int,
        tz: TimeZone
    ) {
        val intent = Intent(context, AdhkarReminderReceiver::class.java).apply {
            putExtra(AdhkarReminderReceiver.EXTRA_CATEGORY_ID, categoryId)
            putExtra(AdhkarReminderReceiver.EXTRA_TITLE, title)
            putExtra(AdhkarReminderReceiver.EXTRA_BODY, body)
        }
        val pi = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pi)
        if (!enabled) return

        val cal = Calendar.getInstance(tz).apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (timeInMillis <= System.currentTimeMillis() + 2_000L) {
                add(Calendar.DAY_OF_MONTH, 1)
            }
        }
        scheduleExact(requestCode, cal.timeInMillis, intent)
    }

    private fun scheduleExact(requestCode: Int, triggerAtMillis: Long, intent: Intent) {
        val pi = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        try {
            if (canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pi)
            } else {
                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pi)
            }
        } catch (_: SecurityException) {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pi)
        }
    }

    private fun cancelAllPrayerAlarms() {
        val bases = listOf(100, 200, 300, 400)
        for (base in bases) {
            for (type in PrayerType.entries) {
                val intent = Intent(context, AdhanAlarmReceiver::class.java)
                val pi = PendingIntent.getBroadcast(
                    context,
                    base + type.ordinal,
                    intent,
                    PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
                )
                if (pi != null) {
                    alarmManager.cancel(pi)
                    pi.cancel()
                }
            }
        }
    }

    companion object {
        private const val REQUEST_MORNING_ADHKAR = 501
        private const val REQUEST_EVENING_ADHKAR = 502
    }
}
