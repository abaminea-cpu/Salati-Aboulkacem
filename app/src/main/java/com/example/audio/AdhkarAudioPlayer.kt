package com.example.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import com.example.R
import com.example.domain.model.DhikrWithProgress
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Locale

data class AdhkarPlaybackState(
    val currentDhikrId: String? = null,
    val currentTitle: String = "",
    val isPlaying: Boolean = false,
    val autoAdvance: Boolean = true,
    val isTtsReady: Boolean = false
)

class AdhkarAudioPlayer(private val context: Context) {

    private val scope = CoroutineScope(Dispatchers.Main.immediate)
    private var mediaPlayer: MediaPlayer? = null
    private var tts: TextToSpeech? = null
    private var ttsAvailableArabic = false
    private var queue: List<DhikrWithProgress> = emptyList()
    private var currentIndex: Int = -1
    private var fallbackTimerJob: Job? = null

    private val _state = MutableStateFlow(AdhkarPlaybackState())
    val state: StateFlow<AdhkarPlaybackState> = _state.asStateFlow()

    init {
        tts = TextToSpeech(context.applicationContext) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val res = tts?.setLanguage(Locale("ar"))
                ttsAvailableArabic = res != TextToSpeech.LANG_MISSING_DATA &&
                    res != TextToSpeech.LANG_NOT_SUPPORTED
                tts?.setSpeechRate(0.92f)
                tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {}
                    override fun onDone(utteranceId: String?) {
                        scope.launch { onTrackFinished() }
                    }

                    @Deprecated("Deprecated in Java")
                    override fun onError(utteranceId: String?) {
                        scope.launch { onTrackFinished() }
                    }
                })
                _state.value = _state.value.copy(isTtsReady = ttsAvailableArabic)
            }
        }
    }

    fun setQueue(items: List<DhikrWithProgress>) {
        queue = items
    }

    fun toggleAutoAdvance() {
        _state.value = _state.value.copy(autoAdvance = !_state.value.autoAdvance)
    }

    fun playDhikr(item: DhikrWithProgress, fullQueue: List<DhikrWithProgress>) {
        queue = fullQueue
        val idx = queue.indexOfFirst { it.id == item.id }
        currentIndex = if (idx >= 0) idx else 0
        playCurrentIndex()
    }

    fun togglePlayPause(fullQueue: List<DhikrWithProgress>) {
        queue = fullQueue
        if (_state.value.isPlaying) {
            stop()
            return
        }
        if (queue.isEmpty()) return
        if (currentIndex !in queue.indices) {
            currentIndex = 0
        }
        playCurrentIndex()
    }

    fun playNext() {
        if (queue.isEmpty()) return
        currentIndex = if (currentIndex + 1 < queue.size) currentIndex + 1 else 0
        playCurrentIndex()
    }

    fun playPrevious() {
        if (queue.isEmpty()) return
        currentIndex = if (currentIndex - 1 >= 0) currentIndex - 1 else queue.lastIndex
        playCurrentIndex()
    }

    fun stop() {
        fallbackTimerJob?.cancel()
        fallbackTimerJob = null
        try {
            mediaPlayer?.stop()
        } catch (_: Throwable) {
        }
        try {
            mediaPlayer?.release()
        } catch (_: Throwable) {
        }
        mediaPlayer = null
        try {
            tts?.stop()
        } catch (_: Throwable) {
        }
        _state.value = _state.value.copy(isPlaying = false)
    }

    private fun playCurrentIndex() {
        stop()
        val item = queue.getOrNull(currentIndex) ?: return
        _state.value = _state.value.copy(
            currentDhikrId = item.id,
            currentTitle = item.title,
            isPlaying = true
        )

        // 1. Prefer EveryAyah MP3 recitation when available and online
        val url = item.audioUrl
        if (!url.isNullOrBlank() && isOnline()) {
            try {
                mediaPlayer = MediaPlayer().apply {
                    setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                            .build()
                    )
                    setDataSource(url)
                    setOnPreparedListener { it.start() }
                    setOnCompletionListener { scope.launch { onTrackFinished() } }
                    setOnErrorListener { _, _, _ ->
                        scope.launch { speakOrChime(item) }
                        true
                    }
                    prepareAsync()
                }
                return
            } catch (_: Throwable) {
            }
        }
        speakOrChime(item)
    }

    private fun speakOrChime(item: DhikrWithProgress) {
        if (ttsAvailableArabic && tts != null) {
            val cleanText = item.arabicText.replace("۝", "، ")
            tts?.speak(cleanText, TextToSpeech.QUEUE_FLUSH, null, item.id)
            return
        }

        // Offline graceful chime + reading pause for devices without Arabic TTS voice pack
        try {
            val uri = Uri.parse("android.resource://${context.packageName}/${R.raw.dhikr_chime}")
            mediaPlayer = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                setDataSource(context, uri)
                setOnCompletionListener {
                    fallbackTimerJob = scope.launch {
                        delay(4500L)
                        onTrackFinished()
                    }
                }
                prepare()
                start()
            }
        } catch (_: Throwable) {
            fallbackTimerJob = scope.launch {
                delay(3500L)
                onTrackFinished()
            }
        }
    }

    private fun onTrackFinished() {
        if (!_state.value.isPlaying) return
        if (_state.value.autoAdvance && currentIndex + 1 < queue.size) {
            currentIndex += 1
            playCurrentIndex()
        } else {
            _state.value = _state.value.copy(isPlaying = false)
        }
    }

    private fun isOnline(): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager ?: return false
        val net = cm.activeNetwork ?: return false
        val caps = cm.getNetworkCapabilities(net) ?: return false
        return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    fun release() {
        stop()
        tts?.shutdown()
        tts = null
    }
}
