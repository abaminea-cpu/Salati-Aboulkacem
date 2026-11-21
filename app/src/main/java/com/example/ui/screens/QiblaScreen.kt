package com.example.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.WarmGoldAccent
import com.example.ui.viewmodel.CompassState
import java.util.Locale
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QiblaScreen(
    cityName: String,
    compassState: CompassState,
    onStartCompass: () -> Unit,
    onStopCompass: () -> Unit,
    modifier: Modifier = Modifier
) {
    DisposableEffect(Unit) {
        onStartCompass()
        onDispose { onStopCompass() }
    }

    val haptic = LocalHapticFeedback.current
    LaunchedEffect(compassState.isAligned) {
        if (compassState.isAligned) {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        }
    }

    val animatedDialRotation by animateFloatAsState(
        targetValue = -compassState.azimuthDegrees,
        animationSpec = spring(stiffness = 220f, dampingRatio = 0.85f),
        label = "compassDial"
    )
    val qiblaBearing = compassState.qiblaInfo.bearingDegrees.toFloat()

    Column(modifier = modifier.fillMaxSize()) {
        TopAppBar(
            title = {
                Text(
                    text = "اتجاه القبلة",
                    fontWeight = FontWeight.Bold
                )
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
        )

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 600.dp)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(50),
                    color = MaterialTheme.colorScheme.surfaceContainerHigh
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = cityName,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                if (compassState.isAligned) {
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "أنت متجه نحو القبلة الشريفة",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                val textMeasurer = rememberTextMeasurer()
                val primaryColor = MaterialTheme.colorScheme.primary
                val onSurfaceColor = MaterialTheme.colorScheme.onSurface
                val outlineColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                val ringBgColor = MaterialTheme.colorScheme.surfaceContainer

                Box(
                    modifier = Modifier
                        .size(300.dp)
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val center = Offset(size.width / 2f, size.height / 2f)
                        val radius = size.minDimension / 2f - 12.dp.toPx()

                        drawCircle(
                            color = ringBgColor,
                            radius = radius,
                            center = center
                        )
                        drawCircle(
                            color = if (compassState.isAligned) primaryColor else outlineColor,
                            radius = radius,
                            center = center,
                            style = Stroke(width = if (compassState.isAligned) 5.dp.toPx() else 2.dp.toPx())
                        )

                        // Rotate compass dial & Qibla arrow by -azimuth
                        val dialAngle = if (compassState.hasSensor) animatedDialRotation else 0f
                        rotate(degrees = dialAngle, pivot = center) {
                            for (deg in 0 until 360 step 15) {
                                val rad = Math.toRadians((deg - 90).toDouble())
                                val inner = if (deg % 90 == 0) radius - 18.dp.toPx() else radius - 10.dp.toPx()
                                val start = Offset(
                                    center.x + (inner * cos(rad)).toFloat(),
                                    center.y + (inner * sin(rad)).toFloat()
                                )
                                val end = Offset(
                                    center.x + (radius * cos(rad)).toFloat(),
                                    center.y + (radius * sin(rad)).toFloat()
                                )
                                drawLine(
                                    color = if (deg == 0) Color(0xFFD32F2F) else outlineColor,
                                    start = start,
                                    end = end,
                                    strokeWidth = if (deg % 90 == 0) 3.dp.toPx() else 1.5.dp.toPx(),
                                    cap = StrokeCap.Round
                                )
                            }

                            // Cardinal labels
                            drawCardinal(textMeasurer, "ش", center, radius - 32.dp.toPx(), -90.0, Color(0xFFD32F2F))
                            drawCardinal(textMeasurer, "شر", center, radius - 32.dp.toPx(), 0.0, onSurfaceColor)
                            drawCardinal(textMeasurer, "ج", center, radius - 32.dp.toPx(), 90.0, onSurfaceColor)
                            drawCardinal(textMeasurer, "غ", center, radius - 32.dp.toPx(), 180.0, onSurfaceColor)

                            // Qibla needle at qiblaBearing
                            rotate(degrees = qiblaBearing, pivot = center) {
                                val needlePath = Path().apply {
                                    moveTo(center.x, center.y - radius + 20.dp.toPx())
                                    lineTo(center.x - 13.dp.toPx(), center.y)
                                    lineTo(center.x, center.y - 12.dp.toPx())
                                    lineTo(center.x + 13.dp.toPx(), center.y)
                                    close()
                                }
                                drawPath(path = needlePath, color = WarmGoldAccent)

                                // Tail
                                drawLine(
                                    color = primaryColor.copy(alpha = 0.65f),
                                    start = center,
                                    end = Offset(center.x, center.y + radius * 0.48f),
                                    strokeWidth = 4.dp.toPx(),
                                    cap = StrokeCap.Round
                                )

                                // Kaaba golden badge near tip
                                drawCircle(
                                    color = Color(0xFF1A1A1A),
                                    radius = 11.dp.toPx(),
                                    center = Offset(center.x, center.y - radius + 44.dp.toPx())
                                )
                                drawCircle(
                                    color = WarmGoldAccent,
                                    radius = 11.dp.toPx(),
                                    center = Offset(center.x, center.y - radius + 44.dp.toPx()),
                                    style = Stroke(width = 2.dp.toPx())
                                )
                            }
                        }

                        // Fixed top phone-heading marker
                        drawLine(
                            color = primaryColor,
                            start = Offset(center.x, center.y - radius - 10.dp.toPx()),
                            end = Offset(center.x, center.y - radius + 4.dp.toPx()),
                            strokeWidth = 4.dp.toPx(),
                            cap = StrokeCap.Round
                        )
                        drawCircle(color = primaryColor, radius = 7.dp.toPx(), center = center)
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "زاوية القبلة",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${compassState.qiblaInfo.bearingDegrees.roundToInt()}°",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "من الشمال الحقيقي",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "المسافة إلى مكة",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = String.format(
                                    Locale.US,
                                    "%,d كم",
                                    compassState.qiblaInfo.distanceKm.roundToInt()
                                ),
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.secondary
                            )
                            Text(
                                text = "الكعبة المشرفة",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Explore,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = if (compassState.hasSensor) {
                                "أمسك الهاتف بشكل أفقي بعيداً عن المعادن، أو حرّكه على شكل رقم 8 لمعايرة البوصلة."
                            } else {
                                "هذا الجهاز لا يحتوي على مستشعر بوصلة مغناطيسية. اتجه بزاوية ${compassState.qiblaInfo.bearingDegrees.roundToInt()}° مع عقارب الساعة من جهة الشمال."
                            },
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Start
                        )
                    }
                }
            }
        }
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawCardinal(
    measurer: TextMeasurer,
    label: String,
    center: Offset,
    dist: Float,
    angleDeg: Double,
    color: Color
) {
    val rad = Math.toRadians(angleDeg)
    val result = measurer.measure(
        text = label,
        style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Bold, color = color)
    )
    val x = center.x + (dist * cos(rad)).toFloat() - result.size.width / 2f
    val y = center.y + (dist * sin(rad)).toFloat() - result.size.height / 2f
    drawText(result, topLeft = Offset(x, y))
}
