package com.example.struna.met

import android.media.AudioManager
import android.media.ToneGenerator
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.struna.Palette
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

@Composable
fun MetronomeScreen() {
    var bpm by remember { mutableFloatStateOf(120f) }
    var isPlaying by remember { mutableStateOf(false) }
    var isBeatFlash by remember { mutableStateOf(false) }
    var beatCount by remember { mutableIntStateOf(0) }

    val toneGenerator = remember {
        ToneGenerator(AudioManager.STREAM_MUSIC, 90)
    }

    DisposableEffect(Unit) {
        onDispose {
            toneGenerator.release()
        }
    }

    LaunchedEffect(isPlaying, bpm) {
        if (isPlaying) {
            while (isPlaying) {
                val intervalMs = (60_000.0 / bpm.roundToInt()).toLong()
                beatCount = (beatCount % 4) + 1
                isBeatFlash = true

                val tone = if (beatCount == 1) {
                    ToneGenerator.TONE_PROP_BEEP2
                } else {
                    ToneGenerator.TONE_PROP_BEEP
                }
                toneGenerator.startTone(tone, 45)

                delay(90L)
                isBeatFlash = false

                val remainingDelay = (intervalMs - 90L).coerceAtLeast(20L)
                delay(remainingDelay)
            }
        } else {
            isBeatFlash = false
            beatCount = 0
        }
    }

    val circleScale by animateFloatAsState(
        targetValue = if (isBeatFlash) 1.16f else 1.0f,
        animationSpec = tween(durationMillis = 80),
        label = "beatScale"
    )
    val circleColor by animateColorAsState(
        targetValue = if (isBeatFlash) Palette.AccentGreen else Palette.CardDark,
        animationSpec = tween(durationMillis = 80),
        label = "beatColor"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = 24.dp, vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "МЕТРОНОМ",
            color = Palette.TextPrimary,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 2.sp
        )

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(160.dp)
                    .scale(circleScale)
                    .background(circleColor, CircleShape)
                    .border(
                        width = 2.dp,
                        color = if (isBeatFlash) Palette.AccentGreen else Color(0xFF3A3A3A),
                        shape = CircleShape
                    )
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${bpm.roundToInt()}",
                        color = if (isBeatFlash) Color.Black else Palette.TextPrimary,
                        fontSize = 48.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = "BPM",
                        color = if (isBeatFlash) Color.Black.copy(alpha = 0.7f) else Palette.AccentGreen,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                for (i in 1..4) {
                    val activeDot = isPlaying && beatCount == i
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(
                                color = if (activeDot) Palette.AccentGreen else Palette.CardDark,
                                shape = CircleShape
                            )
                    )
                }
            }
        }

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                StepButton(text = "−") {
                    bpm = (bpm - 1f).coerceIn(40f, 240f)
                }

                Slider(
                    value = bpm,
                    onValueChange = { bpm = it },
                    valueRange = 40f..240f,
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 12.dp),
                    colors = SliderDefaults.colors(
                        thumbColor = Palette.AccentGreen,
                        activeTrackColor = Palette.AccentGreen,
                        inactiveTrackColor = Palette.CardDark
                    )
                )

                StepButton(text = "+") {
                    bpm = (bpm + 1f).coerceIn(40f, 240f)
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 52.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "40", color = Palette.TextSecondary, fontSize = 12.sp)
                Text(text = "240", color = Palette.TextSecondary, fontSize = 12.sp)
            }

            Spacer(modifier = Modifier.height(28.dp))

            Button(
                onClick = { isPlaying = !isPlaying },
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isPlaying) Palette.AlertRed else Palette.AccentGreen,
                    contentColor = if (isPlaying) Color.White else Color.Black
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp)
            ) {
                PlayPauseGlyph(isPlaying = isPlaying, tint = if (isPlaying) Color.White else Color.Black)
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = if (isPlaying) "СТОП" else "СТАРТ",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }
        }
    }
}

@Composable
private fun StepButton(text: String, onClick: () -> Unit) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(Palette.CardDark)
            .clickable { onClick() }
    ) {
        Text(
            text = text,
            color = Palette.TextPrimary,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun PlayPauseGlyph(isPlaying: Boolean, tint: Color) {
    Canvas(modifier = Modifier.size(20.dp)) {
        val w = size.width
        val h = size.height
        if (isPlaying) {
            // Иконка паузы (две вертикальные полоски)
            val barW = w * 0.26f
            drawRect(color = tint, topLeft = Offset(w * 0.18f, h * 0.15f), size = Size(barW, h * 0.7f))
            drawRect(color = tint, topLeft = Offset(w * 0.56f, h * 0.15f), size = Size(barW, h * 0.7f))
        } else {
            // Иконка Play (треугольник)
            val triangle = Path().apply {
                moveTo(w * 0.25f, h * 0.15f)
                lineTo(w * 0.85f, h * 0.50f)
                lineTo(w * 0.25f, h * 0.85f)
                close()
            }
            drawPath(triangle, color = tint)
        }
    }
}
