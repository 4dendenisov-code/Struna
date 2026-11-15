package com.example.struna.tun

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.struna.Palette
import java.util.Locale
import kotlin.math.abs
import kotlin.math.roundToInt

@Composable
fun Meter(
    centsOffset: Float,
    targetNote: GuitarString,
    currentFreq: Double,
    isSoundActive: Boolean
) {
    // Плавное движение ползунка без эффекта отскока пружины
    val animatedCents by animateFloatAsState(
        targetValue = if (isSoundActive) centsOffset.coerceIn(-50f, 50f) else 0f,
        animationSpec = tween(durationMillis = 150),
        label = "meterCents"
    )

    // Коридор точной настройки +-6 центов
    val isTuned = isSoundActive && abs(animatedCents) <= 6f

    // Цвет ползунка: нейтральный темный в тишине, зеленый при попадании, красный при отклонении
    val indicatorColor by animateColorAsState(
        targetValue = when {
            !isSoundActive -> Palette.NeutralMeter
            isTuned -> Palette.AccentGreen
            else -> Palette.AlertRed
        },
        animationSpec = tween(durationMillis = 150),
        label = "indicatorColor"
    )

    // Фон кнопки-плашки статуса: нейтральный черный в тишине, подсвечивается только при звуке
    val badgeBgColor by animateColorAsState(
        targetValue = when {
            !isSoundActive -> Palette.HeadstockBg
            isTuned -> Palette.AccentGreen.copy(alpha = 0.16f)
            else -> Palette.AlertRed.copy(alpha = 0.16f)
        },
        animationSpec = tween(durationMillis = 150),
        label = "badgeBg"
    )

    val badgeTextColor by animateColorAsState(
        targetValue = when {
            !isSoundActive -> Palette.TextSecondary
            isTuned -> Palette.AccentGreen
            else -> Palette.AlertRed
        },
        animationSpec = tween(durationMillis = 150),
        label = "badgeText"
    )

    val statusText = when {
        !isSoundActive -> "НАСТРОЕНО"
        isTuned -> "НАСТРОЕНО"
        animatedCents < 0 -> "НИЖЕ (${animatedCents.roundToInt()} ц.)"
        else -> "ВЫШЕ (+${animatedCents.roundToInt()} ц.)"
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Нейтральная черная кнопка-плашка статуса
        Box(
            modifier = Modifier
                .background(badgeBgColor, RoundedCornerShape(20.dp))
                .border(
                    width = 1.dp,
                    color = if (isSoundActive) badgeTextColor.copy(alpha = 0.5f) else Color(0xFF2C2C2C),
                    shape = RoundedCornerShape(20.dp)
                )
                .padding(horizontal = 16.dp, vertical = 6.dp)
        ) {
            Text(
                text = statusText,
                color = badgeTextColor,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "${targetNote.noteName} • ${String.format(Locale.US, "%.1f", currentFreq)} Гц",
            color = Palette.TextSecondary,
            fontSize = 13.sp
        )

        Spacer(modifier = Modifier.height(12.dp))

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)
        ) {
            val width = size.width
            val height = size.height
            val centerX = width / 2f
            val centerY = height / 2f

            // Деления шкалы
            val tickCount = 20
            val step = width / tickCount
            for (i in 0..tickCount) {
                if (i == tickCount / 2) continue
                val x = i * step
                val isMajor = i % 5 == 0
                val tickHeight = if (isMajor) 24.dp.toPx() else 12.dp.toPx()
                drawLine(
                    color = Palette.TextSecondary.copy(alpha = 0.4f),
                    start = Offset(x, centerY - tickHeight / 2f),
                    end = Offset(x, centerY + tickHeight / 2f),
                    strokeWidth = if (isMajor) 2.dp.toPx() else 1.2.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }

            // Центральная вертикальная риска
            drawLine(
                color = if (isTuned) Palette.AccentGreen else Palette.TextPrimary.copy(alpha = 0.6f),
                start = Offset(centerX, 6.dp.toPx()),
                end = Offset(centerX, height - 6.dp.toPx()),
                strokeWidth = 3.dp.toPx(),
                cap = StrokeCap.Round
            )

            // Позиция ползунка
            val normalizedOffset = animatedCents / 50f
            val sliderX = centerX + normalizedOffset * (width * 0.44f)

            // Свечение вокруг ползунка только при наличии звука
            if (isSoundActive) {
                drawCircle(
                    color = indicatorColor.copy(alpha = 0.25f),
                    radius = 22.dp.toPx(),
                    center = Offset(sliderX, centerY)
                )
            }

            // Ползунок
            val pillWidth = 14.dp.toPx()
            val pillHeight = 44.dp.toPx()
            drawRoundRect(
                color = indicatorColor,
                topLeft = Offset(sliderX - pillWidth / 2f, centerY - pillHeight / 2f),
                size = Size(pillWidth, pillHeight),
                cornerRadius = CornerRadius(7.dp.toPx(), 7.dp.toPx())
            )
        }
    }
}
