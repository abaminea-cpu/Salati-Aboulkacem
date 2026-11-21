package com.example.alarms

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.MawaqitApp
import com.example.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class AdhkarReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        NotificationChannels.createAll(context)

        val categoryId = intent.getStringExtra(EXTRA_CATEGORY_ID) ?: "morning"
        val title = intent.getStringExtra(EXTRA_TITLE) ?: "تذكير الأذكار"
        val body = intent.getStringExtra(EXTRA_BODY) ?: "حافظ على أذكارك اليومية"

        val openIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(MainActivity.EXTRA_OPEN_ADHKAR_CATEGORY, categoryId)
        }
        val pi = PendingIntent.getActivity(
            context,
            categoryId.hashCode(),
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notification = NotificationCompat.Builder(context, NotificationChannels.ADHKAR_CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pi)
            .build()

        nm.notify(3000 + categoryId.hashCode(), notification)

        // Reschedule for tomorrow
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val app = context.applicationContext as MawaqitApp
                val prefs = app.preferencesRepository.preferencesFlow.first()
                app.alarmScheduler.rescheduleAll(prefs)
            } finally {
                pendingResult.finish()
            }
        }
    }

    companion object {
        const val EXTRA_CATEGORY_ID = "extra_category_id"
        const val EXTRA_TITLE = "extra_title"
        const val EXTRA_BODY = "extra_body"
    }
}
