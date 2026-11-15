package com.example.struna.cmp

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.struna.Palette

@Composable
fun NavBar(
    currentTab: Int,
    onTabSelected: (Int) -> Unit
) {
    Column(modifier = Modifier.background(Palette.SurfaceDark)) {
        HorizontalDivider(color = Palette.GridLine, thickness = 1.dp)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(68.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            NavItem(
                label = "Тюнер",
                isSelected = currentTab == 0,
                onClick = { onTabSelected(0) }
            ) { color ->
                TuningForkIcon(color)
            }

            NavItem(
                label = "Метроном",
                isSelected = currentTab == 1,
                onClick = { onTabSelected(1) }
            ) { color ->
                MetronomeIcon(color)
            }
        }
    }
}

@Composable
private fun RowScope.NavItem(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    icon: @Composable (Color) -> Unit
) {
    val color = if (isSelected) Palette.AccentGreen else Palette.TextSecondary
    Column(
        modifier = Modifier
            .weight(1f)
            .fillMaxHeight()
            .clickable { onClick() },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        icon(color)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            color = color,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}

@Composable
private fun TuningForkIcon(color: Color) {
    Canvas(modifier = Modifier.size(24.dp)) {
        val w = size.width
        val h = size.height
        val stroke = Stroke(width = 2.2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        val path = Path().apply {
            moveTo(w * 0.34f, h * 0.12f)
            lineTo(w * 0.34f, h * 0.48f)
            cubicTo(w * 0.34f, h * 0.64f, w * 0.66f, h * 0.64f, w * 0.66f, h * 0.48f)
            lineTo(w * 0.66f, h * 0.12f)
        }
        drawPath(path, color = color, style = stroke)
        drawLine(
            color = color,
            start = Offset(w * 0.50f, h * 0.60f),
            end = Offset(w * 0.50f, h * 0.90f),
            strokeWidth = 2.2.dp.toPx(),
            cap = StrokeCap.Round
        )
    }
}

@Composable
private fun MetronomeIcon(color: Color) {
    Canvas(modifier = Modifier.size(24.dp)) {
        val w = size.width
        val h = size.height
        val stroke = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        val body = Path().apply {
            moveTo(w * 0.36f, h * 0.15f)
            lineTo(w * 0.64f, h * 0.15f)
            lineTo(w * 0.80f, h * 0.88f)
            lineTo(w * 0.20f, h * 0.88f)
            close()
        }
        drawPath(body, color = color, style = stroke)
        drawLine(
            color = color,
            start = Offset(w * 0.50f, h * 0.80f),
            end = Offset(w * 0.74f, h * 0.28f),
            strokeWidth = 2.2.dp.toPx(),
            cap = StrokeCap.Round
        )
    }
}
