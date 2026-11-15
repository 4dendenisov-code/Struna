package com.example.struna.tun

import android.annotation.SuppressLint
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.AudioTrack
import android.media.MediaRecorder
import kotlinx.coroutines.*
import kotlin.math.*

data class GuitarString(
    val label: String,
    val noteName: String,
    val frequency: Double
) {
    companion object {
        val standardTuning = listOf(
            GuitarString("E", "E2", 82.41),
            GuitarString("A", "A2", 110.00),
            GuitarString("D", "D3", 146.83),
            GuitarString("G", "G3", 196.00),
            GuitarString("B", "B3", 246.94),
            GuitarString("E", "E4", 329.63)
        )
    }
}

class TunerEngine {
    private var listenJob: Job? = null
    private var toneJob: Job? = null

    @Volatile
    private var lastPluckTimeMs: Long = 0L

    @SuppressLint("MissingPermission")
    fun startListening(
        scope: CoroutineScope,
        hasMicPermission: Boolean,
        isAutoMode: () -> Boolean,
        getTargetStringIndex: () -> Int,
        onResult: (detectedStringIndex: Int, centsOffset: Float, currentFreq: Double, isSoundActive: Boolean) -> Unit
    ) {
        stopListening()
        listenJob = scope.launch(Dispatchers.Default) {
            if (hasMicPermission) {
                runMicrophoneLoop(isAutoMode, getTargetStringIndex, onResult)
            } else {
                runSimulationLoop(getTargetStringIndex, onResult)
            }
        }
    }

    @SuppressLint("MissingPermission")
    private suspend fun runMicrophoneLoop(
        isAutoMode: () -> Boolean,
        getTargetStringIndex: () -> Int,
        onResult: (Int, Float, Double, Boolean) -> Unit
    ) {
        val sampleRate = 44100
        val bufferSize = AudioRecord.getMinBufferSize(
            sampleRate,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        ).coerceAtLeast(4096)

        val recorder = try {
            AudioRecord(
                MediaRecorder.AudioSource.MIC,
                sampleRate,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                bufferSize
            )
        } catch (_: Exception) {
            runSimulationLoop(getTargetStringIndex, onResult)
            return
        }

        if (recorder.state != AudioRecord.STATE_INITIALIZED) {
            recorder.release()
            runSimulationLoop(getTargetStringIndex, onResult)
            return
        }

        val buffer = ShortArray(bufferSize / 2)
        recorder.startRecording()

        var smoothedCents = 0f
        var smoothedFreq = 0.0
        var silenceFrames = 0
        var lastDetectedIdx = getTargetStringIndex()

        try {
            while (currentCoroutineContext().isActive) {
                val readCount = recorder.read(buffer, 0, buffer.size)
                if (readCount > 0) {
                    val freq = detectFrequency(buffer, readCount, sampleRate)
                    if (freq in 65.0..400.0) {
                        val stringIdx = if (isAutoMode()) {
                            findClosestStringIndex(freq)
                        } else {
                            getTargetStringIndex()
                        }

                        val targetFreq = GuitarString.standardTuning[stringIdx].frequency
                        val rawCents = (1200.0 * log2(freq / targetFreq)).toFloat().coerceIn(-50f, 50f)

                        if (silenceFrames > 5 || stringIdx != lastDetectedIdx) {
                            smoothedCents = rawCents
                            smoothedFreq = freq
                        } else {
                            smoothedCents = smoothedCents * 0.78f + rawCents * 0.22f
                            smoothedFreq = smoothedFreq * 0.78 + freq * 0.22
                        }

                        lastDetectedIdx = stringIdx
                        silenceFrames = 0

                        withContext(Dispatchers.Main) {
                            onResult(stringIdx, smoothedCents, smoothedFreq, true)
                        }
                    } else {
                        silenceFrames++
                        if (silenceFrames > 6) {
                            val idx = getTargetStringIndex()
                            val targetFreq = GuitarString.standardTuning[idx].frequency
                            smoothedCents = 0f
                            withContext(Dispatchers.Main) {
                                onResult(idx, 0f, targetFreq, false)
                            }
                        }
                    }
                }
                delay(45)
            }
        } finally {
            try {
                recorder.stop()
                recorder.release()
            } catch (_: Exception) {
            }
        }
    }

    private suspend fun runSimulationLoop(
        getTargetStringIndex: () -> Int,
        onResult: (Int, Float, Double, Boolean) -> Unit
    ) {
        while (currentCoroutineContext().isActive) {
            val idx = getTargetStringIndex()
            val targetFreq = GuitarString.standardTuning[idx].frequency
            val elapsed = System.currentTimeMillis() - lastPluckTimeMs

            if (elapsed in 0..1800) {
                val progress = elapsed / 1000.0
                val simulatedCents = (sin(progress * 6.0) * 8.0 * exp(-progress * 1.8)).toFloat()
                val simulatedFreq = targetFreq * 2.0.pow(simulatedCents / 1200.0)
                withContext(Dispatchers.Main) {
                    onResult(idx, simulatedCents, simulatedFreq, true)
                }
            } else {
                withContext(Dispatchers.Main) {
                    onResult(idx, 0f, targetFreq, false)
                }
            }
            delay(50)
        }
    }

    fun playReferenceTone(scope: CoroutineScope, frequency: Double) {
        lastPluckTimeMs = System.currentTimeMillis()
        toneJob?.cancel()
        toneJob = scope.launch(Dispatchers.Default) {
            val sampleRate = 44100
            val durationSec = 1.2
            val numSamples = (sampleRate * durationSec).toInt()
            val samples = ShortArray(numSamples)

            for (i in 0 until numSamples) {
                val time = i.toDouble() / sampleRate
                val envelope = exp(-2.8 * time)
                val wave = 0.7 * sin(2.0 * PI * frequency * time) +
                        0.3 * sin(2.0 * PI * frequency * 2.0 * time)
                samples[i] = (wave * envelope * Short.MAX_VALUE * 0.5).toInt().toShort()
            }

            val audioTrack = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(samples.size * 2)
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()

            audioTrack.write(samples, 0, samples.size)
            audioTrack.play()
            delay((durationSec * 1000).toLong())
            audioTrack.release()
        }
    }

    fun stopListening() {
        listenJob?.cancel()
        toneJob?.cancel()
    }

    private fun findClosestStringIndex(freq: Double): Int {
        var bestIdx = 0
        var minDiff = Double.MAX_VALUE
        GuitarString.standardTuning.forEachIndexed { index, guitarString ->
            val diff = abs(1200.0 * log2(freq / guitarString.frequency))
            if (diff < minDiff) {
                minDiff = diff
                bestIdx = index
            }
        }
        return bestIdx
    }

    private fun detectFrequency(buffer: ShortArray, size: Int, sampleRate: Int): Double {
        var rms = 0.0
        for (i in 0 until size) {
            val v = buffer[i] / 32768.0
            rms += v * v
        }
        // Возвращена исходная высокая чувствительность (0.015)
        if (sqrt(rms / size) < 0.015) return -1.0

        val minLag = sampleRate / 420
        val maxLag = sampleRate / 65
        var bestLag = -1
        var maxNormCorr = 0.0

        for (lag in minLag..maxLag) {
            var corr = 0.0
            var energy1 = 0.0
            var energy2 = 0.0
            val count = size - lag

            for (i in 0 until count) {
                val s1 = buffer[i].toDouble()
                val s2 = buffer[i + lag].toDouble()
                corr += s1 * s2
                energy1 += s1 * s1
                energy2 += s2 * s2
            }

            val denom = sqrt(energy1 * energy2)
            if (denom > 0.0) {
                val normCorr = corr / denom
                if (normCorr > maxNormCorr) {
                    maxNormCorr = normCorr
                    bestLag = lag
                }
            }
        }

        // Мягкий порог для улавливания даже затухающей струны
        if (maxNormCorr < 0.35 || bestLag <= 0) return -1.0

        return sampleRate.toDouble() / bestLag
    }
}
