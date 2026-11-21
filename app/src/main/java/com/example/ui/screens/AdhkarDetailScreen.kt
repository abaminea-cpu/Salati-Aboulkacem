package com.example.ui.screens

import android.content.Intent
import androidx.compose.animation.animateColorAsState
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.AdhkarPlaybackState
import com.example.data.local.AdhkarSeedData
import com.example.data.repository.AdhkarRepository
import com.example.domain.model.DhikrWithProgress
import com.example.ui.theme.AmiriFontFamily

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdhkarDetailScreen(
    categoryId: String,
    items: List<DhikrWithProgress>,
    playbackState: AdhkarPlaybackState,
    onBack: () -> Unit,
    onIncrement: (DhikrWithProgress) -> Unit,
    onResetItem: (String) -> Unit,
    onResetCategory: () -> Unit,
    onToggleFavorite: (String) -> Unit,
    onPlayItem: (DhikrWithProgress) -> Unit,
    onTogglePlayPause: () -> Unit,
    onPlayNext: () -> Unit,
    onPlayPrevious: () -> Unit,
    onToggleAutoAdvance: () -> Unit,
    onStopAudio: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val categoryTitle = rememberCategoryTitle(categoryId)

    DisposableEffect(Unit) {
        onDispose { onStopAudio() }
    }

    Column(modifier = modifier.fillMaxSize()) {
        TopAppBar(
            title = {
                Text(
                    text = categoryTitle,
                    fontWeight = FontWeight.Bold
                )
            },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "رجوع"
                    )
                }
            },
            actions = {
                if (categoryId != AdhkarRepository.FAVORITES_CATEGORY_ID) {
                    IconButton(
                        onClick = onResetCategory,
                        modifier = Modifier.testTag("reset_category_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "إعادة ضبط العدادات"
                        )
                    }
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
        )

        // Sequential Audio Player Bar
        if (items.isNotEmpty()) {
            SequentialAudioPlayerBar(
                playbackState = playbackState,
                onTogglePlayPause = onTogglePlayPause,
                onPlayNext = onPlayNext,
                onPlayPrevious = onPlayPrevious,
                onToggleAutoAdvance = onToggleAutoAdvance,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
            )
        }

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.TopCenter
        ) {
            if (items.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.FavoriteBorder,
                        contentDescription = null,
                        modifier = Modifier.size(56.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "لا توجد أذكار في المفضلة حالياً",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "اضغط على أيقونة القلب في أي ذكر لإضافته هنا",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .widthIn(max = 600.dp),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(items, key = { it.id }) { item ->
                        val isCurrentlyPlaying = playbackState.isPlaying && playbackState.currentDhikrId == item.id
                        DhikrItemCard(
                            item = item,
                            isPlaying = isCurrentlyPlaying,
                            onTapCounter = { onIncrement(item) },
                            onResetItem = { onResetItem(item.id) },
                            onToggleFavorite = { onToggleFavorite(item.id) },
                            onPlay = { onPlayItem(item) },
                            onShare = {
                                val shareText = "${item.title}\n\n${item.arabicText}\n\n(${item.referenceText})\n— تطبيق Salati ABOULKACEM"
                                val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_TEXT, shareText)
                                }
                                context.startActivity(Intent.createChooser(sendIntent, "مشاركة الذكر"))
                            }
                        )
                    }
                    item { Spacer(modifier = Modifier.height(16.dp)) }
                }
            }
        }
    }
}

@Composable
private fun SequentialAudioPlayerBar(
    playbackState: AdhkarPlaybackState,
    onTogglePlayPause: () -> Unit,
    onPlayNext: () -> Unit,
    onPlayPrevious: () -> Unit,
    onToggleAutoAdvance: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.primaryContainer,
        tonalElevation = 2.dp,
        modifier = modifier
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = Icons.Default.VolumeUp,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = if (playbackState.currentTitle.isNotBlank()) {
                            playbackState.currentTitle
                        } else {
                            "الاستماع للأذكار صوتياً"
                        },
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = if (playbackState.autoAdvance) "تشغيل متتابع تلقائي" else "ذكر واحد فقط",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.78f)
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                FilterChip(
                    selected = playbackState.autoAdvance,
                    onClick = onToggleAutoAdvance,
                    label = {
                        Icon(
                            imageVector = Icons.Default.Repeat,
                            contentDescription = "تشغيل متتابع تلقائي",
                            modifier = Modifier.size(16.dp)
                        )
                    }
                )
                IconButton(onClick = onPlayPrevious) {
                    Icon(Icons.Default.SkipPrevious, contentDescription = "السابق")
                }
                FilledTonalIconButton(
                    onClick = onTogglePlayPause,
                    modifier = Modifier.testTag("adhkar_audio_play_pause")
                ) {
                    Icon(
                        imageVector = if (playbackState.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (playbackState.isPlaying) "إيقاف مؤقت" else "تشغيل"
                    )
                }
                IconButton(onClick = onPlayNext) {
                    Icon(Icons.Default.SkipNext, contentDescription = "التالي")
                }
            }
        }
    }
}

@Composable
private fun DhikrItemCard(
    item: DhikrWithProgress,
    isPlaying: Boolean,
    onTapCounter: () -> Unit,
    onResetItem: () -> Unit,
    onToggleFavorite: () -> Unit,
    onPlay: () -> Unit,
    onShare: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val containerColor by animateColorAsState(
        targetValue = when {
            isPlaying -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f)
            item.isCompleted -> MaterialTheme.colorScheme.surfaceContainerLow
            else -> MaterialTheme.colorScheme.surfaceContainer
        },
        label = "dhikrCardColor"
    )
    val progress = if (item.targetCount > 0) {
        item.currentCount.toFloat() / item.targetCount.toFloat()
    } else 1f

    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("dhikr_card_${item.id}")
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )
                Row {
                    IconButton(onClick = onPlay) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.VolumeUp else Icons.Default.PlayArrow,
                            contentDescription = "استماع",
                            tint = if (isPlaying) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = onToggleFavorite) {
                        Icon(
                            imageVector = if (item.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "مفضلة",
                            tint = if (item.isFavorite) Color(0xFFD32F2F) else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = onShare) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "مشاركة",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = item.arabicText,
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontFamily = AmiriFontFamily,
                    fontSize = 22.sp,
                    lineHeight = 38.sp
                ),
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = item.referenceText,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onTapCounter()
                    },
                    enabled = !item.isCompleted,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (item.isCompleted) {
                            MaterialTheme.colorScheme.secondary
                        } else {
                            MaterialTheme.colorScheme.primary
                        }
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("dhikr_count_button_${item.id}")
                ) {
                    if (item.isCompleted) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("تم الذكر (${item.targetCount}/${item.targetCount})")
                    } else {
                        Box(contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(
                                progress = { progress },
                                modifier = Modifier.size(22.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                trackColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.25f),
                                strokeWidth = 2.5.dp
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("التكرار: ${item.currentCount} / ${item.targetCount} (متبقي ${item.remainingCount})")
                    }
                }

                if (item.currentCount > 0) {
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(onClick = onResetItem) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "إعادة العد"
                        )
                    }
                }
            }
        }
    }
}

private fun rememberCategoryTitle(categoryId: String): String {
    if (categoryId == AdhkarRepository.FAVORITES_CATEGORY_ID) return "الأذكار المفضلة"
    return AdhkarSeedData.categories.firstOrNull { it.id == categoryId }?.title ?: "الأذكار"
}
