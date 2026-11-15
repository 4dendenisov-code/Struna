package com.example.struna.cmp

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.struna.Palette
import com.example.struna.R
import com.example.struna.tun.GuitarString

@Composable
fun Headstock(
    selectedStringIndex: Int,
    onStringSelect: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    // Слева сверху вниз: D (2), A (1), E (0)
    // Справа сверху вниз: G (3), B (4), E (5)
    val leftIndices = listOf(2, 1, 0)
    val rightIndices = listOf(3, 4, 5)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.End
        ) {
            leftIndices.forEachIndexed { i, index ->
                PegButton(
                    label = GuitarString.standardTuning[index].label,
                    isSelected = selectedStringIndex == index,
                    onClick = { onStringSelect(index) }
                )
                if (i < leftIndices.lastIndex) {
                    Spacer(modifier = Modifier.height(28.dp))
                }
            }
        }

        Image(
            painter = painterResource(id = R.drawable.gitar),
            contentDescription = "Голова грифа",
            contentScale = ContentScale.FillHeight,
            modifier = Modifier
                .height(300.dp)
                .padding(horizontal = 10.dp)
        )

        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.Start
        ) {
            rightIndices.forEachIndexed { i, index ->
                PegButton(
                    label = GuitarString.standardTuning[index].label,
                    isSelected = selectedStringIndex == index,
                    onClick = { onStringSelect(index) }
                )
                if (i < rightIndices.lastIndex) {
                    Spacer(modifier = Modifier.height(28.dp))
                }
            }
        }
    }
}

@Composable
private fun PegButton(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val bgColor by animateColorAsState(
        targetValue = if (isSelected) Color.White else Palette.CardDark,
        animationSpec = tween(180),
        label = "pegBg"
    )
    val textColor by animateColorAsState(
        targetValue = if (isSelected) Color.Black else Palette.TextPrimary,
        animationSpec = tween(180),
        label = "pegText"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(bgColor)
            .border(
                width = 1.dp,
                color = if (isSelected) Color.White else Color(0xFF3A3A3A),
                shape = CircleShape
            )
            .clickable { onClick() }
    ) {
        Text(
            text = label,
            color = textColor,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
