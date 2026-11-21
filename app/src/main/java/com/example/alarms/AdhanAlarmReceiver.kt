package com.example.alarms

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.MainActivity
import com.example.MawaqitApp
import com.example.R
import com.example.audio.AdhanPlaybackService
import com.example.domain.model.PrayerType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class AdhanAlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        NotificationChannels.createAll(context)

        val prayerId = intent.getStringExtra(EXTRA_PRAYER_ID) ?: PrayerType.FAJR.id
        val prayerTime = intent.getStringExtra(EXTRA_PRAYER_TIME) ?: ""
        val cityName = intent.getStringExtra(EXTRA_CITY_NAME) ?: ""
        val isPreAdhan = intent.getBooleanExtra(EXTRA_IS_PRE_ADHAN, false)
        val preMinutes = intent.getIntExtra(EXTRA_PRE_MINUTES, 10)
        val prayer = PrayerType.fromId(prayerId)

        val openIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val openPendingIntent = PendingIntent.getActivity(
            context,
            prayer.ordinal,
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        if (isPreAdhan) {
            val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val notification = NotificationCompat.Builder(context, NotificationChannels.ADHAN_CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("اقترب وقت صلاة ${prayer.arabicName}")
                .setContentText("تبقى $preMinutes دقائق على أذان ${prayer.arabicName} ($prayerTime) في $cityName")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(openPendingIntent)
                .build()
            nm.notify(2000 + prayer.ordinal, notification)
            return
        }

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val app = context.applicationContext as MawaqitApp
                val prefs = app.preferencesRepository.preferencesFlow.first()

                if (prayer.isObligatoryAdhan) {
                    val serviceIntent = Intent(context, AdhanPlaybackService::class.java).apply {
                        action = AdhanPlaybackService.ACTION_PLAY_ADHAN
                        putExtra(AdhanPlaybackService.EXTRA_PRAYER_NAME, prayer.arabicName)
                        putExtra(AdhanPlaybackService.EXTRA_PRAYER_TIME, prayerTime)
                        putExtra(AdhanPlaybackService.EXTRA_CITY_NAME, cityName)
                        putExtra(AdhanPlaybackService.EXTRA_MUEZZIN_ID, prefs.selectedMuezzin.id)
                    }
                    ContextCompat.startForegroundService(context, serviceIntent)
                } else {
                    val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                    val notification = NotificationCompat.Builder(context, NotificationChannels.ADHAN_CHANNEL_ID)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle("وقت ${prayer.arabicName}")
                        .setContentText("حان الآن موعد ${prayer.arabicName} ($prayerTime) في $cityName")
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setAutoCancel(true)
                        .setContentIntent(openPendingIntent)
                        .build()
                    nm.notify(1000 + prayer.ordinal, notification)
                }

                // Ensure subsequent day's alarms remain scheduled
                app.alarmScheduler.rescheduleAll(prefs)
            } finally {
                pendingResult.finish()
            }
        }
    }

    companion object {
        const val EXTRA_PRAYER_ID = "extra_prayer_id"
        const val EXTRA_PRAYER_TIME = "extra_prayer_time"
        const val EXTRA_CITY_NAME = "extra_city_name"
        const val EXTRA_IS_PRE_ADHAN = "extra_is_pre_adhan"
        const val EXTRA_PRE_MINUTES = "extra_pre_minutes"
    }
}
