package com.example.engine.audio

import android.content.Context
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import com.example.data.model.MusicTrack
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.RandomAccessFile
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.Locale
import java.util.UUID
import kotlin.coroutines.resume
import kotlin.math.sin

class AudioSynthesizer(private val context: Context) {

    private var tts: TextToSpeech? = null
    private var isTtsReady = false

    fun initTts(onReady: (Boolean) -> Unit = {}) {
        if (tts != null && isTtsReady) {
            onReady(true)
            return
        }
        tts = TextToSpeech(context.applicationContext) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val result = tts?.setLanguage(Locale.FRENCH)
                isTtsReady = result != TextToSpeech.LANG_MISSING_DATA &&
                        result != TextToSpeech.LANG_NOT_SUPPORTED
                tts?.setPitch(1.0f)
                tts?.setSpeechRate(1.0f)
                onReady(isTtsReady)
            } else {
                isTtsReady = false
                onReady(false)
            }
        }
    }

    fun speakPreview(text: String, pitch: Float = 1.0f, speed: Float = 1.0f) {
        tts?.setPitch(pitch)
        tts?.setSpeechRate(speed)
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "preview_${System.currentTimeMillis()}")
    }

    fun stopSpeaking() {
        tts?.stop()
    }

    suspend fun synthesizeSpeechToFile(
        text: String,
        outputWav: File,
        pitch: Float = 1.0f,
        speed: Float = 1.0f
    ): Boolean = withContext(Dispatchers.IO) {
        if (!isTtsReady) {
            return@withContext false
        }

        suspendCancellableCoroutine { continuation ->
            val utteranceId = "utt_${UUID.randomUUID()}"
            tts?.setPitch(pitch)
            tts?.setSpeechRate(speed)

            val params = Bundle().apply {
                putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, utteranceId)
            }

            tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(id: String?) {}
                override fun onDone(id: String?) {
                    if (id == utteranceId && continuation.isActive) {
                        continuation.resume(true)
                    }
                }
                override fun onError(id: String?) {
                    if (id == utteranceId && continuation.isActive) {
                        continuation.resume(false)
                    }
                }
            })

            val res = tts?.synthesizeToFile(text, params, outputWav, utteranceId)
            if (res != TextToSpeech.SUCCESS && continuation.isActive) {
                continuation.resume(false)
            }
        }
    }

    /**
     * Synthesizes background music PCM data (44.1kHz, 16-bit Mono)
     */
    fun generateMusicTrackPcm(
        track: MusicTrack,
        durationSeconds: Int,
        sampleRate: Int = 44100
    ): ByteArray {
        if (track == MusicTrack.NONE) {
            return ByteArray(0)
        }

        val totalSamples = durationSeconds * sampleRate
        val pcm = ShortArray(totalSamples)

        val chordProgression = when (track) {
            MusicTrack.ANIME_SHONEN -> listOf(
                // C - G - Am - F
                doubleArrayOf(261.63, 329.63, 392.00, 523.25),
                doubleArrayOf(196.00, 246.94, 293.66, 392.00),
                doubleArrayOf(220.00, 261.63, 329.63, 440.00),
                doubleArrayOf(174.61, 220.00, 261.63, 349.23)
            )
            MusicTrack.CINEMATIC_DREAM -> listOf(
                // Dm9 - G13 - Cmaj9 - Am
                doubleArrayOf(146.83, 220.00, 293.66, 349.23),
                doubleArrayOf(196.00, 246.94, 293.66, 392.00),
                doubleArrayOf(261.63, 329.63, 392.00, 493.88),
                doubleArrayOf(220.00, 261.63, 329.63, 440.00)
            )
            MusicTrack.CYBERPUNK_BEAT -> listOf(
                // Fm - Eb - Db - C7
                doubleArrayOf(174.61, 207.65, 261.63, 349.23),
                doubleArrayOf(155.56, 196.00, 233.08, 311.13),
                doubleArrayOf(138.59, 174.61, 207.65, 277.18),
                doubleArrayOf(130.81, 164.81, 196.00, 261.63)
            )
            MusicTrack.ACOUSTIC_CHILL -> listOf(
                // G - D - Em - C
                doubleArrayOf(196.00, 246.94, 293.66, 392.00),
                doubleArrayOf(146.83, 220.00, 293.66, 370.00),
                doubleArrayOf(164.81, 196.00, 246.94, 329.63),
                doubleArrayOf(130.81, 164.81, 196.00, 261.63)
            )
            MusicTrack.EPIC_ADVENTURE -> listOf(
                // Am - F - C - G
                doubleArrayOf(110.00, 220.00, 261.63, 329.63),
                doubleArrayOf(87.31, 174.61, 220.00, 261.63),
                doubleArrayOf(130.81, 261.63, 329.63, 392.00),
                doubleArrayOf(98.00, 196.00, 246.94, 293.66)
            )
            MusicTrack.NONE -> emptyList()
        }

        val chordDuration = sampleRate * 2 // 2 seconds per chord
        val beatInterval = (sampleRate * 0.25).toInt() // 120 bpm 16th notes

        for (i in 0 until totalSamples) {
            val t = i.toDouble() / sampleRate
            val chordIndex = ((i / chordDuration) % chordProgression.size)
            val currentNotes = chordProgression[chordIndex]

            var sampleVal = 0.0

            // Harmonic pad / chord notes
            for ((noteIndex, freq) in currentNotes.withIndex()) {
                val weight = 0.15 / (noteIndex + 1)
                sampleVal += weight * sin(2.0 * Math.PI * freq * t)
                // subtle chorus
                sampleVal += (weight * 0.5) * sin(2.0 * Math.PI * (freq + 0.8) * t)
            }

            // Rhythm pulse / beat for energetic genres
            if (track == MusicTrack.ANIME_SHONEN || track == MusicTrack.CYBERPUNK_BEAT) {
                val beatPos = i % beatInterval
                val kickDecay = Math.exp(-beatPos.toDouble() / (sampleRate * 0.06))
                val kickFreq = 60.0 + 80.0 * kickDecay
                sampleVal += 0.3 * sin(2.0 * Math.PI * kickFreq * t) * kickDecay
            }

            // Envelope fade in/out
            val fadeIn = (i.toDouble() / (sampleRate * 0.5)).coerceAtMost(1.0)
            val fadeOut = ((totalSamples - i).toDouble() / (sampleRate * 0.5)).coerceAtMost(1.0)
            val envelope = fadeIn * fadeOut

            val scaled = (sampleVal * envelope * 24000.0).toInt().coerceIn(-32767, 32767)
            pcm[i] = scaled.toShort()
        }

        val buffer = ByteBuffer.allocate(pcm.size * 2).order(ByteOrder.LITTLE_ENDIAN)
        for (sample in pcm) {
            buffer.putShort(sample)
        }
        return buffer.array()
    }

    /**
     * Writes raw 16-bit Mono PCM to a standard WAV file
     */
    fun writePcmToWav(pcmData: ByteArray, outputFile: File, sampleRate: Int = 44100) {
        val totalAudioLen = pcmData.size.toLong()
        val totalDataLen = totalAudioLen + 36
        val channels = 1
        val byteRate = sampleRate * channels * 2

        FileOutputStream(outputFile).use { out ->
            val header = ByteArray(44)
            val bb = ByteBuffer.wrap(header).order(ByteOrder.LITTLE_ENDIAN)

            bb.put("RIFF".toByteArray())
            bb.putInt((totalDataLen).toInt())
            bb.put("WAVE".toByteArray())
            bb.put("fmt ".toByteArray())
            bb.putInt(16) // SubChunk1Size (16 for PCM)
            bb.putShort(1.toShort()) // AudioFormat (1 for PCM)
            bb.putShort(channels.toShort())
            bb.putInt(sampleRate)
            bb.putInt(byteRate)
            bb.putShort((channels * 2).toShort()) // block align
            bb.putShort(16.toShort()) // bits per sample
            bb.put("data".toByteArray())
            bb.putInt(totalAudioLen.toInt())

            out.write(header)
            out.write(pcmData)
        }
    }

    fun release() {
        tts?.stop()
        tts?.shutdown()
        tts = null
        isTtsReady = false
    }
}
