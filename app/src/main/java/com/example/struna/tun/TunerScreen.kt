package com.example.struna.tun

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.struna.Palette
import com.example.struna.cmp.Header
import com.example.struna.cmp.Headstock

@Composable
fun TunerScreen() {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val tunerEngine = remember { TunerEngine() }

    var hasMicPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasMicPermission = granted
    }

    LaunchedEffect(Unit) {
        if (!hasMicPermission) {
            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    var isAutoMode by remember { mutableStateOf(true) }
    var selectedStringIndex by remember { mutableIntStateOf(0) }
    var centsOffset by remember { mutableFloatStateOf(0f) }
    var currentFreq by remember { mutableDoubleStateOf(GuitarString.standardTuning.first().frequency) }
    var isSoundActive by remember { mutableStateOf(false) }

    DisposableEffect(hasMicPermission) {
        tunerEngine.startListening(
            scope = coroutineScope,
            hasMicPermission = hasMicPermission,
            isAutoMode = { isAutoMode },
            getTargetStringIndex = { selectedStringIndex },
            onResult = { detectedIdx, cents, freq, soundActive ->
                if (isAutoMode && soundActive) {
                    selectedStringIndex = detectedIdx
                }
                centsOffset = cents
                currentFreq = freq
                isSoundActive = soundActive
            }
        )
        onDispose {
            tunerEngine.stopListening()
        }
    }

    val activeString = GuitarString.standardTuning[selectedStringIndex]

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Верхняя панель и шкала на фоне с сеткой
        Header(
            isAutoMode = isAutoMode,
            onAutoModeChange = { isAutoMode = it }
        )

        Spacer(modifier = Modifier.height(6.dp))

        Meter(
            centsOffset = centsOffset,
            targetNote = activeString,
            currentFreq = currentFreq,
            isSoundActive = isSoundActive
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Тонкая разделительная линия перед нижней зоной грифа
        HorizontalDivider(color = Palette.GridLine, thickness = 1.dp)

        // Нижняя часть экрана: сплошной черный фон (#111111), совпадающий с фоном картинки gitar.png
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(Palette.HeadstockBg)
                .padding(bottom = 8.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            Headstock(
                selectedStringIndex = selectedStringIndex,
                onStringSelect = { index ->
                    isAutoMode = false
                    selectedStringIndex = index
                    centsOffset = 0f
                    currentFreq = GuitarString.standardTuning[index].frequency
                    isSoundActive = true
                    tunerEngine.playReferenceTone(
                        scope = coroutineScope,
                        frequency = GuitarString.standardTuning[index].frequency
                    )
                }
            )
        }
    }
}
