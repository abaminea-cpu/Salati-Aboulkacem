package com.example.ui.screens

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.BatteryAlert
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RadioButtonChecked
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.domain.model.MuezzinOption
import com.example.domain.model.PrayerType
import com.example.domain.model.UserPreferencesState
import com.example.ui.viewmodel.SystemPermissionStatus
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlertsScreen(
    prefs: UserPreferencesState,
    permissionStatus: SystemPermissionStatus,
    isAdhanPlaying: Boolean,
    playingMuezzinId: String?,
    onRefreshPermissions: () -> Unit,
    onTogglePrayerAlert: (PrayerType, Boolean) -> Unit,
    onSelectPreAdhanMinutes: (Int) -> Unit,
    onSelectMuezzin: (MuezzinOption) -> Unit,
    onPreviewOrStopMuezzin: (MuezzinOption) -> Unit,
    onUpdateMorningAdhkar: (Boolean, Int, Int) -> Unit,
    onUpdateEveningAdhkar: (Boolean, Int, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                onRefreshPermissions()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { onRefreshPermissions() }

    var showMorningTimePicker by remember { mutableStateOf(false) }
    var showEveningTimePicker by remember { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxSize()) {
        TopAppBar(
            title = {
                Text(
                    text = "تنبيهات الأذان والأذكار",
                    fontWeight = FontWeight.Bold
                )
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
        )

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.TopCenter
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 600.dp),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 1. System Readiness / Exact Alarm / Battery Optimization
                item {
                    SystemPermissionsCard(
                        status = permissionStatus,
                        onRequestNotifications = {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            }
                        },
                        onOpenExactAlarmSettings = {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                                    data = Uri.parse("package:${context.packageName}")
                                }
                                runCatching { context.startActivity(intent) }
                            }
                        },
                        onOpenBatterySettings = {
                            val intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
                            runCatching { context.startActivity(intent) }
                        }
                    )
                }

                // 2. Per-Prayer Alert Toggles & Pre-Adhan Reminder
                item {
                    Card(
                        shape = RoundedCornerShape(22.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "تفعيل الأذان لكل صلاة",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "تعمل التنبيهات بدقة حتى عند إغلاق التطبيق أو إعادة تشغيل الهاتف",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            PrayerType.entries.forEachIndexed { idx, prayer ->
                                val enabled = prefs.prayerAlertsEnabled[prayer] ?: false
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = prayer.arabicName,
                                            style = MaterialTheme.typography.bodyLarge,
                                            fontWeight = FontWeight.Medium
                                        )
                                        if (!prayer.isObligatoryAdhan) {
                                            Text(
                                                text = "تنبيه صامت بدون أذان",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                    Switch(
                                        checked = enabled,
                                        onCheckedChange = { onTogglePrayerAlert(prayer, it) },
                                        modifier = Modifier.testTag("alert_switch_${prayer.id}")
                                    )
                                }
                                if (idx < PrayerType.entries.lastIndex) {
                                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "تنبيه قبل الأذان",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                listOf(0 to "بدون", 5 to "5 د", 10 to "10 د", 15 to "15 د").forEach { (mins, label) ->
                                    FilterChip(
                                        selected = prefs.preAdhanMinutes == mins,
                                        onClick = { onSelectPreAdhanMinutes(mins) },
                                        label = { Text(label) }
                                    )
                                }
                            }
                        }
                    }
                }

                // 3. Muezzin Selection + Preview
                item {
                    Card(
                        shape = RoundedCornerShape(22.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "اختيار صوت المؤذن",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            MuezzinOption.entries.forEachIndexed { idx, option ->
                                val isSelected = prefs.selectedMuezzin == option
                                val isThisPlaying = isAdhanPlaying && playingMuezzinId == option.id
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { onSelectMuezzin(option) }
                                        .padding(vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(
                                            imageVector = if (isSelected) {
                                                Icons.Default.RadioButtonChecked
                                            } else {
                                                Icons.Default.RadioButtonUnchecked
                                            },
                                            contentDescription = null,
                                            tint = if (isSelected) {
                                                MaterialTheme.colorScheme.primary
                                            } else {
                                                MaterialTheme.colorScheme.onSurfaceVariant
                                            }
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(
                                                text = option.arabicName,
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                            )
                                            Text(
                                                text = option.subtitle,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }

                                    IconButton(
                                        onClick = { onPreviewOrStopMuezzin(option) },
                                        modifier = Modifier.testTag("preview_muezzin_${option.id}")
                                    ) {
                                        Icon(
                                            imageVector = if (isThisPlaying) Icons.Default.Stop else Icons.Default.PlayArrow,
                                            contentDescription = if (isThisPlaying) "إيقاف" else "تجربة الصوت",
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                                if (idx < MuezzinOption.entries.lastIndex) {
                                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                                }
                            }
                        }
                    }
                }

                // 4. Morning & Evening Adhkar Reminders
                item {
                    Card(
                        shape = RoundedCornerShape(22.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "تنبيهات الأذكار اليومية",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )

                            AdhkarReminderRow(
                                title = "تذكير أذكار الصباح",
                                hour = prefs.morningAdhkarHour,
                                minute = prefs.morningAdhkarMinute,
                                enabled = prefs.morningAdhkarEnabled,
                                onToggle = { enabled ->
                                    onUpdateMorningAdhkar(enabled, prefs.morningAdhkarHour, prefs.morningAdhkarMinute)
                                },
                                onEditTime = { showMorningTimePicker = true }
                            )

                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                            AdhkarReminderRow(
                                title = "تذكير أذكار المساء",
                                hour = prefs.eveningAdhkarHour,
                                minute = prefs.eveningAdhkarMinute,
                                enabled = prefs.eveningAdhkarEnabled,
                                onToggle = { enabled ->
                                    onUpdateEveningAdhkar(enabled, prefs.eveningAdhkarHour, prefs.eveningAdhkarMinute)
                                },
                                onEditTime = { showEveningTimePicker = true }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }

    if (showMorningTimePicker) {
        AdhkarTimePickerDialog(
            title = "وقت تذكير أذكار الصباح",
            initialHour = prefs.morningAdhkarHour,
            initialMinute = prefs.morningAdhkarMinute,
            onDismiss = { showMorningTimePicker = false },
            onConfirm = { h, m ->
                onUpdateMorningAdhkar(prefs.morningAdhkarEnabled, h, m)
                showMorningTimePicker = false
            }
        )
    }

    if (showEveningTimePicker) {
        AdhkarTimePickerDialog(
            title = "وقت تذكير أذكار المساء",
            initialHour = prefs.eveningAdhkarHour,
            initialMinute = prefs.eveningAdhkarMinute,
            onDismiss = { showEveningTimePicker = false },
            onConfirm = { h, m ->
                onUpdateEveningAdhkar(prefs.eveningAdhkarEnabled, h, m)
                showEveningTimePicker = false
            }
        )
    }
}

@Composable
private fun SystemPermissionsCard(
    status: SystemPermissionStatus,
    onRequestNotifications: () -> Unit,
    onOpenExactAlarmSettings: () -> Unit,
    onOpenBatterySettings: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "جاهزية النظام للأذان الدقيق",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )

            PermissionStatusRow(
                icon = Icons.Default.Notifications,
                label = "إذن الإشعارات (أندرويد 13+)",
                granted = status.hasNotificationPermission,
                actionLabel = "منح الإذن",
                onAction = onRequestNotifications
            )

            PermissionStatusRow(
                icon = Icons.Default.Alarm,
                label = "إذن المنبه الدقيق (SCHEDULE_EXACT_ALARM)",
                granted = status.canScheduleExactAlarms,
                actionLabel = "تفعيل",
                onAction = onOpenExactAlarmSettings
            )

            PermissionStatusRow(
                icon = Icons.Default.BatteryAlert,
                label = "استثناء توفير البطارية للأذان بالخلفية",
                granted = status.isIgnoringBatteryOptimization,
                actionLabel = "إعدادات البطارية",
                onAction = onOpenBatterySettings
            )
        }
    }
}

@Composable
private fun PermissionStatusRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    granted: Boolean,
    actionLabel: String,
    onAction: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                imageVector = if (granted) Icons.Default.CheckCircle else icon,
                contentDescription = null,
                tint = if (granted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
        if (granted) {
            Text(
                text = "مفعّل",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        } else {
            OutlinedButton(onClick = onAction) {
                Text(actionLabel, style = MaterialTheme.typography.labelMedium)
            }
        }
    }
}

@Composable
private fun AdhkarReminderRow(
    title: String,
    hour: Int,
    minute: Int,
    enabled: Boolean,
    onToggle: (Boolean) -> Unit,
    onEditTime: () -> Unit
) {
    val period = if (hour < 12) "ص" else "م"
    val h12 = when {
        hour == 0 -> 12
        hour > 12 -> hour - 12
        else -> hour
    }
    val timeText = String.format(Locale.US, "%d:%02d %s", h12, minute, period)

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            TextButton(
                onClick = onEditTime,
                contentPadding = PaddingValues(0.dp)
            ) {
                Icon(Icons.Default.Schedule, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("الوقت: $timeText (تعديل)")
            }
        }
        Switch(
            checked = enabled,
            onCheckedChange = onToggle
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AdhkarTimePickerDialog(
    title: String,
    initialHour: Int,
    initialMinute: Int,
    onDismiss: () -> Unit,
    onConfirm: (Int, Int) -> Unit
) {
    val state = rememberTimePickerState(
        initialHour = initialHour,
        initialMinute = initialMinute,
        is24Hour = false
    )
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, fontWeight = FontWeight.Bold) },
        text = {
            TimePicker(state = state)
        },
        confirmButton = {
            Button(onClick = { onConfirm(state.hour, state.minute) }) {
                Text("حفظ")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء")
            }
        }
    )
}
