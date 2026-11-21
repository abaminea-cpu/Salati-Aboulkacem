package com.example.audio

import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.alarms.NotificationChannels
import com.example.domain.model.MuezzinOption
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AdhanPlaybackService : Service() {

    private var mediaPlayer: MediaPlayer? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        NotificationChannels.createAll(this)

        when (intent?.action) {
            ACTION_STOP_ADHAN -> {
                stopPlayback()
                stopSelf()
                return START_NOT_STICKY
            }
            ACTION_PLAY_ADHAN -> {
                val prayerName = intent.getStringExtra(EXTRA_PRAYER_NAME) ?: "الصلاة"
                val prayerTime = intent.getStringExtra(EXTRA_PRAYER_TIME) ?: ""
                val cityName = intent.getStringExtra(EXTRA_CITY_NAME) ?: ""
                val muezzinId = intent.getStringExtra(EXTRA_MUEZZIN_ID) ?: MuezzinOption.OFFLINE_CHIME.id
                val isPreview = intent.getBooleanExtra(EXTRA_IS_PREVIEW, false)

                val stopIntent = Intent(this, AdhanPlaybackService::class.java).apply {
                    action = ACTION_STOP_ADHAN
                }
                val stopPendingIntent = PendingIntent.getService(
                    this,
                    901,
                    stopIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )

                val openIntent = Intent(this, MainActivity::class.java).apply {
                    this.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                }
                val openPendingIntent = PendingIntent.getActivity(
                    this,
                    902,
                    openIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )

                val title = if (isPreview) "تجربة صوت الأذان" else "حان الآن موعد أذان $prayerName"
                val subtitle = if (isPreview) MuezzinOption.fromId(muezzinId).arabicName else "$prayerTime — $cityName"

                val notification = NotificationCompat.Builder(this, NotificationChannels.ADHAN_CHANNEL_ID)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle(title)
                    .setContentText(subtitle)
                    .setPriority(NotificationCompat.PRIORITY_MAX)
                    .setCategory(NotificationCompat.CATEGORY_ALARM)
                    .setOngoing(true)
                    .setContentIntent(openPendingIntent)
                    .addAction(0, "إيقاف الأذان", stopPendingIntent)
                    .build()

                startForeground(NOTIFICATION_ID, notification)
                startAudio(MuezzinOption.fromId(muezzinId))
            }
        }
        return START_NOT_STICKY
    }

    private fun startAudio(muezzin: MuezzinOption) {
        stopPlayback()
        _isAdhanPlaying.value = true
        _playingMuezzinId.value = muezzin.id

        val streamUrl = muezzin.streamUrl
        if (streamUrl != null && isOnline()) {
            try {
                val player = MediaPlayer().apply {
                    setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_ALARM)
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .build()
                    )
                    setDataSource(streamUrl)
                    setOnPreparedListener { it.start() }
                    setOnCompletionListener {
                        stopPlayback()
                        stopSelf()
                    }
                    setOnErrorListener { _, _, _ ->
                        playOfflineFallback()
                        true
                    }
                    prepareAsync()
                }
                mediaPlayer = player
                return
            } catch (_: Throwable) {
                // Fall back to offline audio seamlessly
            }
        }
        playOfflineFallback()
    }

    private fun playOfflineFallback() {
        try {
            mediaPlayer?.release()
            val uri = Uri.parse("android.resource://$packageName/${R.raw.adhan_chime}")
            mediaPlayer = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                setDataSource(this@AdhanPlaybackService, uri)
                setOnCompletionListener {
                    stopPlayback()
                    stopSelf()
                }
                setOnErrorListener { _, _, _ ->
                    stopPlayback()
                    stopSelf()
                    true
                }
                prepare()
                start()
            }
        } catch (_: Throwable) {
            stopPlayback()
            stopSelf()
        }
    }

    private fun stopPlayback() {
        try {
            mediaPlayer?.stop()
        } catch (_: Throwable) {
        }
        try {
            mediaPlayer?.release()
        } catch (_: Throwable) {
        }
        mediaPlayer = null
        _isAdhanPlaying.value = false
        _playingMuezzinId.value = null
    }

    private fun isOnline(): Boolean {
        val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager ?: return false
        val net = cm.activeNetwork ?: return false
        val caps = cm.getNetworkCapabilities(net) ?: return false
        return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    override fun onDestroy() {
        stopPlayback()
        super.onDestroy()
    }

    companion object {
        const val ACTION_PLAY_ADHAN = "com.example.audio.ACTION_PLAY_ADHAN"
        const val ACTION_STOP_ADHAN = "com.example.audio.ACTION_STOP_ADHAN"
        const val EXTRA_PRAYER_NAME = "extra_prayer_name"
        const val EXTRA_PRAYER_TIME = "extra_prayer_time"
        const val EXTRA_CITY_NAME = "extra_city_name"
        const val EXTRA_MUEZZIN_ID = "extra_muezzin_id"
        const val EXTRA_IS_PREVIEW = "extra_is_preview"
        private const val NOTIFICATION_ID = 4401

        private val _isAdhanPlaying = MutableStateFlow(false)
        val isAdhanPlaying: StateFlow<Boolean> = _isAdhanPlaying.asStateFlow()

        private val _playingMuezzinId = MutableStateFlow<String?>(null)
        val playingMuezzinId: StateFlow<String?> = _playingMuezzinId.asStateFlow()

        fun stopAdhan(context: Context) {
            val intent = Intent(context, AdhanPlaybackService::class.java).apply {
                action = ACTION_STOP_ADHAN
            }
            context.startService(intent)
        }
    }
}
