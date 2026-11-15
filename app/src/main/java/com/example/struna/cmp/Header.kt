package com.example.struna.cmp

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.struna.Palette

@Composable
fun Header(
    isAutoMode: Boolean,
    onAutoModeChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Стилизованный логотип "struna" в левом верхнем углу
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "stru",
                color = Palette.TextPrimary,
                fontSize = 24.sp,
                fontWeight = FontWeight.Light,
                letterSpacing = (-0.5).sp
            )
            Text(
                text = "na",
                color = Palette.AccentGreen,
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = (-0.5).sp
            )
        }

        // Переключатель режима "Автомат"
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "Автомат",
                color = if (isAutoMode) Palette.TextPrimary else Palette.TextSecondary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.width(8.dp))
            Switch(
                checked = isAutoMode,
                onCheckedChange = onAutoModeChange,
                modifier = Modifier.scale(0.85f),
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.Black,
                    checkedTrackColor = Palette.AccentGreen,
                    uncheckedThumbColor = Palette.TextSecondary,
                    uncheckedTrackColor = Palette.CardDark,
                    uncheckedBorderColor = Color.Transparent
                )
            )
        }
    }
}
