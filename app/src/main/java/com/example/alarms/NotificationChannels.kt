package com.example.alarms

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.media.AudioAttributes
import android.net.Uri
import android.os.Build
import com.example.R

object NotificationChannels {
    const val ADHAN_CHANNEL_ID = "adhan_alerts_channel"
    const val ADHKAR_CHANNEL_ID = "adhkar_reminders_channel"
    const val PLAYBACK_CHANNEL_ID = "adhan_playback_channel"

    fun createAll(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val adhanChannel = NotificationChannel(
            ADHAN_CHANNEL_ID,
            context.getString(R.string.channel_adhan_name),
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = context.getString(R.string.channel_adhan_desc)
            enableVibration(true)
            setSound(null, null) // Sound is handled cleanly by AdhanPlaybackService
        }

        val dhikrUri = Uri.parse("android.resource://${context.packageName}/${R.raw.dhikr_chime}")
        val dhikrAudioAttrs = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_NOTIFICATION)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        val adhkarChannel = NotificationChannel(
            ADHKAR_CHANNEL_ID,
            context.getString(R.string.channel_adhkar_name),
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = context.getString(R.string.channel_adhkar_desc)
            enableVibration(true)
            setSound(dhikrUri, dhikrAudioAttrs)
        }

        val playbackChannel = NotificationChannel(
            PLAYBACK_CHANNEL_ID,
            context.getString(R.string.channel_playback_name),
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = context.getString(R.string.channel_playback_desc)
            setSound(null, null)
        }

        manager.createNotificationChannels(listOf(adhanChannel, adhkarChannel, playbackChannel))
    }
}
