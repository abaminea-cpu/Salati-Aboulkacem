package com.example.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.WbTwilight
import androidx.compose.material.icons.outlined.NightsStay
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.domain.model.PrayerDataSource
import com.example.domain.model.PrayerDaySchedule
import com.example.domain.model.PrayerTimeEntry
import com.example.domain.model.PrayerType
import com.example.domain.model.UserPreferencesState
import com.example.ui.theme.AmiriFontFamily
import com.example.ui.theme.HeroDeepEmerald
import com.example.ui.theme.HeroMidnightTeal
import com.example.ui.theme.WarmGoldAccent
import com.example.ui.viewmodel.NextPrayerCountdown
import java.util.Calendar
import java.util.Locale
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrayersScreen(
    schedule: PrayerDaySchedule?,
    countdown: NextPrayerCountdown?,
    prefs: UserPreferencesState,
    qiblaBearingDeg: Double,
    isRefreshing: Boolean,
    onOpenCityPicker: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenQibla: () -> Unit,
    onOpenDailyAdhkar: (String) -> Unit,
    onRefreshSync: () -> Unit,
    onTogglePrayerAlert: (PrayerType, Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    val suggestedAdhkarCategory = if (currentHour in 3..14) "morning" else "evening"
    val suggestedAdhkarTitle = if (suggestedAdhkarCategory == "morning") "أذكار الصباح" else "أذكار المساء"

    val uriHandler = LocalUriHandler.current

    Column(modifier = modifier.fillMaxSize()) {
        TopAppBar(
            title = {
                Text(
                    text = stringResource(id = R.string.app_name),
                    style = MaterialTheme.typography.titleLarge.copy(
                        textDirection = TextDirection.Ltr
                    ),
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 1,
                    modifier = Modifier.testTag("app_header_title")
                )
            },
            actions = {
                if (isRefreshing) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .padding(horizontal = 12.dp)
                            .size(20.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    IconButton(
                        onClick = onRefreshSync,
                        modifier = Modifier.testTag("refresh_sync_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "تحديث المواقيت"
                        )
                    }
                }
                IconButton(
                    onClick = onOpenSettings,
                    modifier = Modifier.testTag("settings_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "الإعدادات"
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.Transparent
            )
        )

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.TopCenter
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 600.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Surface(
                        onClick = onOpenCityPicker,
                        shape = RoundedCornerShape(50),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.65f),
                        modifier = Modifier.testTag("city_selector_chip")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = "تغيير المدينة",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = prefs.cityName,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }

                item {
                    HeroCountdownCard(
                        schedule = schedule,
                        countdown = countdown,
                        prefs = prefs,
                        qiblaBearingDeg = qiblaBearingDeg,
                        onOpenQibla = onOpenQibla
                    )
                }

                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "مواقيت الصلاة اليوم",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        schedule?.let {
                            AssistChip(
                                onClick = onRefreshSync,
                                label = {
                                    Text(
                                        text = if (it.source == PrayerDataSource.ALADHAN_API) {
                                            "AlAdhan متصل"
                                        } else {
                                            "حساب محلي"
                                        },
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                },
                                colors = AssistChipDefaults.assistChipColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                                )
                            )
                        }
                    }
                }

                if (schedule != null) {
                    val nowMillis = System.currentTimeMillis()
                    items(schedule.entries, key = { it.type.id }) { entry ->
                        val isNext = countdown?.let {
                            !it.isTomorrow && it.nextPrayerType == entry.type
                        } ?: false
                        val isPast = entry.epochMillis < nowMillis && !isNext

                        PrayerTimeRowCard(
                            entry = entry,
                            isNext = isNext,
                            isPast = isPast,
                            onToggleAlert = { onTogglePrayerAlert(entry.type, !entry.isAlertEnabled) }
                        )
                    }
                }

                item {
                    DailyAdhkarShortcutCard(
                        title = suggestedAdhkarTitle,
                        onClick = { onOpenDailyAdhkar(suggestedAdhkarCategory) }
                    )
                }

                item {
                    DeveloperCreditFooter(
                        onClick = {
                            runCatching { uriHandler.openUri("https://abm-design.fr") }
                        }
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
private fun HeroCountdownCard(
    schedule: PrayerDaySchedule?,
    countdown: NextPrayerCountdown?,
    prefs: UserPreferencesState,
    qiblaBearingDeg: Double,
    onOpenQibla: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(28.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(248.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.img_prayer_hero_banner),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                HeroMidnightTeal.copy(alpha = 0.72f),
                                HeroDeepEmerald.copy(alpha = 0.88f)
                            )
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = schedule?.hijriDate?.formattedArabic ?: "جاري التحميل...",
                            style = MaterialTheme.typography.titleMedium.copy(fontFamily = AmiriFontFamily),
                            fontWeight = FontWeight.Bold,
                            color = WarmGoldAccent
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = schedule?.gregorianDateArabic ?: "",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                    }

                    Surface(
                        onClick = onOpenQibla,
                        shape = RoundedCornerShape(50),
                        color = Color.White.copy(alpha = 0.16f),
                        modifier = Modifier.testTag("hero_qibla_badge")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Explore,
                                contentDescription = "اتجاه القبلة",
                                tint = WarmGoldAccent,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "القبلة ${qiblaBearingDeg.roundToInt()}°",
                                style = MaterialTheme.typography.labelMedium,
                                color = Color.White
                            )
                        }
                    }
                }

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val labelPrefix = if (countdown?.isTomorrow == true) "الصلاة القادمة (غداً): " else "الصلاة القادمة: "
                    Text(
                        text = countdown?.let { "$labelPrefix${it.nextPrayerName} (${it.nextPrayerTimeFormatted})" } ?: "",
                        style = MaterialTheme.typography.titleSmall,
                        color = Color.White.copy(alpha = 0.92f)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = countdown?.remainingFormatted ?: "--:--:--",
                        style = MaterialTheme.typography.displayLarge.copy(
                            fontSize = 42.sp,
                            fontFeatureSettings = "tnum"
                        ),
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Column {
                    LinearProgressIndicator(
                        progress = { countdown?.progressFraction ?: 0f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(7.dp)
                            .clip(RoundedCornerShape(50)),
                        color = WarmGoldAccent,
                        trackColor = Color.White.copy(alpha = 0.22f),
                        strokeCap = StrokeCap.Round
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "${prefs.calculationMethod.arabicName} • ${prefs.madhab.arabicName.substringBefore(" (")}",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.78f),
                        maxLines = 1
                    )
                }
            }
        }
    }
}

@Composable
private fun PrayerTimeRowCard(
    entry: PrayerTimeEntry,
    isNext: Boolean,
    isPast: Boolean,
    onToggleAlert: () -> Unit
) {
    val containerColor by animateColorAsState(
        targetValue = when {
            isNext -> MaterialTheme.colorScheme.primaryContainer
            isPast -> MaterialTheme.colorScheme.surfaceContainerLow.copy(alpha = 0.75f)
            else -> MaterialTheme.colorScheme.surfaceContainer
        },
        label = "prayerRowColor"
    )
    val contentColor = when {
        isNext -> MaterialTheme.colorScheme.onPrimaryContainer
        isPast -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.60f)
        else -> MaterialTheme.colorScheme.onSurface
    }

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("prayer_card_${entry.type.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(
                            if (isNext) {
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.16f)
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = prayerIcon(entry.type),
                        contentDescription = null,
                        tint = if (isNext) MaterialTheme.colorScheme.primary else contentColor,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = entry.type.arabicName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = if (isNext) FontWeight.Bold else FontWeight.SemiBold,
                            color = contentColor
                        )
                        if (isNext) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = MaterialTheme.colorScheme.primary
                            ) {
                                Text(
                                    text = "القادمة",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                    if (entry.offsetMinutes != 0) {
                        val sign = if (entry.offsetMinutes > 0) "+" else ""
                        Text(
                            text = "تعديل يدوي: $sign${entry.offsetMinutes} د",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = entry.formattedTime,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontFeatureSettings = "tnum"
                    ),
                    fontWeight = FontWeight.Bold,
                    color = contentColor
                )
                Spacer(modifier = Modifier.width(6.dp))
                IconButton(
                    onClick = onToggleAlert,
                    modifier = Modifier.testTag("toggle_alert_${entry.type.id}")
                ) {
                    Icon(
                        imageVector = if (entry.isAlertEnabled) {
                            Icons.Default.NotificationsActive
                        } else {
                            Icons.Default.NotificationsOff
                        },
                        contentDescription = if (entry.isAlertEnabled) "إيقاف تنبيه ${entry.type.arabicName}" else "تفعيل تنبيه ${entry.type.arabicName}",
                        tint = if (entry.isAlertEnabled) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun DailyAdhkarShortcutCard(
    title: String,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.65f)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("daily_adhkar_shortcut")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "وِردك اليومي — $title",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "اضغط لفتح الأذكار مع عدّاد التكرار والتشغيل الصوتي المتتابع",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.85f)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = "فتح $title",
                tint = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }
}

@Composable
private fun DeveloperCreditFooter(
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("developer_credit_footer")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "Développé par Amine ABOULKACEM",
                style = MaterialTheme.typography.bodyMedium.copy(
                    textDirection = TextDirection.Ltr
                ),
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Language,
                    contentDescription = "abm-design.fr",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "abm-design.fr",
                    style = MaterialTheme.typography.labelLarge.copy(
                        textDirection = TextDirection.Ltr,
                        textDecoration = TextDecoration.Underline
                    ),
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

private fun prayerIcon(type: PrayerType): ImageVector = when (type) {
    PrayerType.FAJR -> Icons.Default.WbTwilight
    PrayerType.SUNRISE -> Icons.Default.WbSunny
    PrayerType.DHUHR -> Icons.Default.WbSunny
    PrayerType.ASR -> Icons.Default.WbTwilight
    PrayerType.MAGHRIB -> Icons.Outlined.NightsStay
    PrayerType.ISHA -> Icons.Default.Bedtime
}
