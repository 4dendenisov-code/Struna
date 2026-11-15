package com.example.struna

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.dp
import com.example.struna.cmp.NavBar
import com.example.struna.met.MetronomeScreen
import com.example.struna.tun.TunerScreen
import com.example.struna.ui.theme.StrunaTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            StrunaTheme {
                GuitarTunerProApp()
            }
        }
    }
}

@Composable
fun GuitarTunerProApp() {
    var selectedTab by remember { mutableIntStateOf(0) }

    Scaffold(
        containerColor = Palette.Background,
        bottomBar = {
            NavBar(
                currentTab = selectedTab,
                onTabSelected = { selectedTab = it }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Palette.Background)
                .padding(innerPadding)
        ) {
            GridBackground()

            when (selectedTab) {
                0 -> TunerScreen()
                1 -> MetronomeScreen()
            }
        }
    }
}

@Composable
fun GridBackground() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val step = 36.dp.toPx()
        val strokeWidth = 1.dp.toPx()

        var x = 0f
        while (x < size.width) {
            drawLine(
                color = Palette.GridLine,
                start = Offset(x, 0f),
                end = Offset(x, size.height),
                strokeWidth = strokeWidth
            )
            x += step
        }

        var y = 0f
        while (y < size.height) {
            drawLine(
                color = Palette.GridLine,
                start = Offset(0f, y),
                end = Offset(size.width, y),
                strokeWidth = strokeWidth
            )
            y += step
        }
    }
}
