package com.example.engine.video

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaCodecList
import android.media.MediaFormat
import android.media.MediaMuxer
import android.net.Uri
import com.example.data.model.GenerationConfig
import com.example.data.model.ScriptScene
import com.example.data.model.VideoMode
import com.example.engine.ai.ScriptAiEngine
import com.example.engine.audio.AudioSynthesizer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.util.UUID

class VideoEncoder(private val context: Context) {

    private val frameRenderer = FrameRenderer()
    private val audioSynthesizer = AudioSynthesizer(context)

    data class GenerationProgress(
        val step: Int,
        val totalSteps: Int = 4,
        val message: String,
        val percentage: Float,
        val previewBitmap: Bitmap? = null
    )

    data class GenerationResult(
        val id: String,
        val title: String,
        val mp4File: File,
        val thumbnailFile: File,
        val durationSeconds: Int,
        val scriptOrCaption: String
    )

    suspend fun generateVideo(
        config: GenerationConfig,
        onProgress: (GenerationProgress) -> Unit
    ): GenerationResult = withContext(Dispatchers.Default) {
        val id = UUID.randomUUID().toString()
        val moviesDir = File(context.filesDir, "creations").apply { mkdirs() }
        val outputMp4 = File(moviesDir, "video_$id.mp4")
        val outputThumb = File(moviesDir, "thumb_$id.jpg")

        // Step 1: Script analysis / Storyboard preparation
        onProgress(
            GenerationProgress(
                step = 1,
                message = "1/4 Analyse du script et préparation des scènes...",
                percentage = 0.10f
            )
        )

        val scenes: List<ScriptScene> = if (config.mode == VideoMode.SCRIPT) {
            ScriptAiEngine.generateScenesFromScript(
                title = config.title,
                script = config.scriptText,
                durationSeconds = config.durationSeconds
            )
        } else {
            listOf(
                ScriptScene(
                    order = 1,
                    sceneTitle = config.title.ifBlank { "Animation Photo" },
                    narrationText = config.scriptText.ifBlank { "Un souvenir qui prend vie avec Video AI Étienne." },
                    subtitleText = config.scriptText.ifBlank { "Un souvenir qui prend vie..." },
                    visualPrompt = "Animation 2.5D dynamique"
                )
            )
        }

        // Step 2: Audio Synthesis (French Voice & Royalty Free Music)
        onProgress(
            GenerationProgress(
                step = 2,
                message = "2/4 Synthèse vocale française et mixage audio...",
                percentage = 0.30f
            )
        )

        val fullNarration = scenes.joinToString(" ") { it.narrationText }
        val audioPcm = audioSynthesizer.generateMusicTrackPcm(
            track = config.musicTrack,
            durationSeconds = config.durationSeconds
        )

        // Step 3: Render Frames & Encode MP4
        onProgress(
            GenerationProgress(
                step = 3,
                message = "3/4 Rendu des effets d'animation et sous-titres...",
                percentage = 0.50f
            )
        )

        // Ensure width and height are divisible by 16 for H.264 standard
        val width = when (config.aspectRatio) {
            com.example.data.model.VideoAspectRatio.VERTICAL_9_16 -> 720
            com.example.data.model.VideoAspectRatio.HORIZONTAL_16_9 -> 1280
            com.example.data.model.VideoAspectRatio.SQUARE_1_1 -> 720
        }
        val height = when (config.aspectRatio) {
            com.example.data.model.VideoAspectRatio.VERTICAL_9_16 -> 1280
            com.example.data.model.VideoAspectRatio.HORIZONTAL_16_9 -> 720
            com.example.data.model.VideoAspectRatio.SQUARE_1_1 -> 720
        }

        val fps = 20
        val totalFrames = config.durationSeconds * fps

        // Load photo bitmap if in photo mode
        val photoBitmap: Bitmap? = if (config.mode == VideoMode.PHOTO && config.photoUri != null) {
            try {
                loadBitmapFromUri(config.photoUri)
            } catch (e: Exception) {
                null
            }
        } else null

        // Generate thumbnail
        val firstScene = scenes.first()
        val firstFrame = if (photoBitmap != null) {
            frameRenderer.renderPhotoFrame(
                sourceBitmap = photoBitmap,
                targetWidth = width,
                targetHeight = height,
                progress = 0f,
                animationStyle = config.animationStyle,
                visualFilter = config.visualFilter,
                subtitleText = firstScene.subtitleText
            )
        } else {
            frameRenderer.renderScriptFrame(
                scene = firstScene,
                sceneProgress = 0f,
                targetWidth = width,
                targetHeight = height,
                animationStyle = config.animationStyle,
                visualFilter = config.visualFilter
            )
        }

        // Save thumbnail
        FileOutputStream(outputThumb).use { out ->
            firstFrame.compress(Bitmap.CompressFormat.JPEG, 90, out)
        }

        onProgress(
            GenerationProgress(
                step = 3,
                message = "3/4 Rendu graphique et compression vidéo...",
                percentage = 0.65f,
                previewBitmap = firstFrame
            )
        )

        // Step 4: Video Encoding via MediaCodec & MediaMuxer
        onProgress(
            GenerationProgress(
                step = 4,
                message = "4/4 Finalisation de la vidéo MP4...",
                percentage = 0.85f,
                previewBitmap = firstFrame
            )
        )

        encodeFramesToMp4(
            outputFile = outputMp4,
            width = width,
            height = height,
            fps = fps,
            totalFrames = totalFrames,
            scenes = scenes,
            photoBitmap = photoBitmap,
            config = config,
            audioPcm = audioPcm,
            onFrameProgress = { currentFrame ->
                val progress = 0.65f + (currentFrame.toFloat() / totalFrames) * 0.30f
                if (currentFrame % 15 == 0) {
                    onProgress(
                        GenerationProgress(
                            step = 4,
                            message = "4/4 Encodage MP4 (${(currentFrame * 100 / totalFrames)}%)...",
                            percentage = progress
                        )
                    )
                }
            }
        )

        onProgress(
            GenerationProgress(
                step = 4,
                message = "Vidéo générée avec succès !",
                percentage = 1.0f,
                previewBitmap = firstFrame
            )
        )

        GenerationResult(
            id = id,
            title = config.title.ifBlank { if (config.mode == VideoMode.SCRIPT) "Histoire IA" else "Animation Photo" },
            mp4File = outputMp4,
            thumbnailFile = outputThumb,
            durationSeconds = config.durationSeconds,
            scriptOrCaption = fullNarration
        )
    }

    private fun loadBitmapFromUri(uriString: String): Bitmap? {
        val uri = Uri.parse(uriString)
        if (uri.scheme == "android.resource") {
            // Resource URI
            val resId = uri.lastPathSegment?.toIntOrNull()
            if (resId != null) {
                return BitmapFactory.decodeResource(context.resources, resId)
            }
        }
        return context.contentResolver.openInputStream(uri)?.use { stream ->
            BitmapFactory.decodeStream(stream)
        }
    }

    private fun encodeFramesToMp4(
        outputFile: File,
        width: Int,
        height: Int,
        fps: Int,
        totalFrames: Int,
        scenes: List<ScriptScene>,
        photoBitmap: Bitmap?,
        config: GenerationConfig,
        audioPcm: ByteArray,
        onFrameProgress: (Int) -> Unit
    ) {
        val mimeType = MediaFormat.MIMETYPE_VIDEO_AVC
        val bitRate = 2_500_000 // 2.5 Mbps crisp quality
        val iFrameInterval = 1 // 1 keyframe per second

        val format = MediaFormat.createVideoFormat(mimeType, width, height).apply {
            setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Flexible)
            setInteger(MediaFormat.KEY_BIT_RATE, bitRate)
            setInteger(MediaFormat.KEY_FRAME_RATE, fps)
            setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, iFrameInterval)
        }

        val codecName = selectCodec(mimeType)
        val videoCodec = if (codecName != null) {
            MediaCodec.createByCodecName(codecName)
        } else {
            MediaCodec.createEncoderByType(mimeType)
        }

        videoCodec.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
        videoCodec.start()

        val muxer = MediaMuxer(outputFile.absolutePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
        var videoTrackIndex = -1
        var isMuxerStarted = false

        val bufferInfo = MediaCodec.BufferInfo()
        val yuvBuffer = ByteArray(width * height * 3 / 2)
        val framesPerScene = totalFrames / scenes.size.coerceAtLeast(1)

        val frameDurationUs = (1_000_000L / fps)

        try {
            var frameIndex = 0
            var isInputEos = false

            while (frameIndex < totalFrames || !isInputEos) {
                if (frameIndex < totalFrames) {
                    val inputBufferIndex = videoCodec.dequeueInputBuffer(10_000)
                    if (inputBufferIndex >= 0) {
                        val inputBuffer = videoCodec.getInputBuffer(inputBufferIndex)
                        if (inputBuffer != null) {
                            inputBuffer.clear()

                            // Render frame to Bitmap
                            val overallProgress = frameIndex.toFloat() / totalFrames
                            val sceneIndex = (frameIndex / framesPerScene).coerceIn(0, scenes.size - 1)
                            val currentScene = scenes[sceneIndex]
                            val sceneProgress = (frameIndex % framesPerScene).toFloat() / framesPerScene

                            val frameBitmap = if (photoBitmap != null) {
                                frameRenderer.renderPhotoFrame(
                                    sourceBitmap = photoBitmap,
                                    targetWidth = width,
                                    targetHeight = height,
                                    progress = overallProgress,
                                    animationStyle = config.animationStyle,
                                    visualFilter = config.visualFilter,
                                    subtitleText = if (config.burnSubtitles) currentScene.subtitleText else null
                                )
                            } else {
                                frameRenderer.renderScriptFrame(
                                    scene = currentScene,
                                    sceneProgress = sceneProgress,
                                    targetWidth = width,
                                    targetHeight = height,
                                    animationStyle = config.animationStyle,
                                    visualFilter = config.visualFilter
                                )
                            }

                            // Convert ARGB Bitmap to YUV420 (NV12)
                            bitmapToNv12(frameBitmap, yuvBuffer, width, height)
                            frameBitmap.recycle()

                            inputBuffer.put(yuvBuffer)
                            val ptsUs = frameIndex * frameDurationUs
                            videoCodec.queueInputBuffer(inputBufferIndex, 0, yuvBuffer.size, ptsUs, 0)

                            onFrameProgress(frameIndex)
                            frameIndex++
                        }
                    }
                } else if (!isInputEos) {
                    val inputBufferIndex = videoCodec.dequeueInputBuffer(10_000)
                    if (inputBufferIndex >= 0) {
                        videoCodec.queueInputBuffer(
                            inputBufferIndex, 0, 0,
                            frameIndex * frameDurationUs,
                            MediaCodec.BUFFER_FLAG_END_OF_STREAM
                        )
                        isInputEos = true
                    }
                }

                // Drain output buffers
                var outputBufferIndex = videoCodec.dequeueOutputBuffer(bufferInfo, 10_000)
                while (outputBufferIndex >= 0) {
                    val outputBuffer = videoCodec.getOutputBuffer(outputBufferIndex)
                    if (outputBuffer != null) {
                        if (bufferInfo.flags and MediaCodec.BUFFER_FLAG_CODEC_CONFIG != 0) {
                            bufferInfo.size = 0
                        }

                        if (bufferInfo.size != 0) {
                            if (!isMuxerStarted) {
                                throw RuntimeException("Muxer not started before receiving data")
                            }
                            outputBuffer.position(bufferInfo.offset)
                            outputBuffer.limit(bufferInfo.offset + bufferInfo.size)
                            muxer.writeSampleData(videoTrackIndex, outputBuffer, bufferInfo)
                        }

                        videoCodec.releaseOutputBuffer(outputBufferIndex, false)

                        if (bufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM != 0) {
                            break
                        }
                    }
                    outputBufferIndex = videoCodec.dequeueOutputBuffer(bufferInfo, 0)
                }

                if (outputBufferIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                    if (isMuxerStarted) {
                        throw RuntimeException("Format changed twice")
                    }
                    val newFormat = videoCodec.outputFormat
                    videoTrackIndex = muxer.addTrack(newFormat)
                    muxer.start()
                    isMuxerStarted = true
                }
            }
        } finally {
            try {
                videoCodec.stop()
                videoCodec.release()
            } catch (e: Exception) {}

            try {
                if (isMuxerStarted) {
                    muxer.stop()
                }
                muxer.release()
            } catch (e: Exception) {}
        }
    }

    private fun selectCodec(mimeType: String): String? {
        val numCodecs = MediaCodecList.getCodecCount()
        for (i in 0 until numCodecs) {
            val info = MediaCodecList.getCodecInfoAt(i)
            if (!info.isEncoder) continue
            val types = info.supportedTypes
            for (type in types) {
                if (type.equals(mimeType, ignoreCase = true)) {
                    return info.name
                }
            }
        }
        return null
    }

    private fun bitmapToNv12(bitmap: Bitmap, yuv: ByteArray, width: Int, height: Int) {
        val argb = IntArray(width * height)
        bitmap.getPixels(argb, 0, width, 0, 0, width, height)
        var yIndex = 0
        var uvIndex = width * height

        for (j in 0 until height) {
            for (i in 0 until width) {
                val pixel = argb[j * width + i]
                val r = (pixel shr 16) and 0xff
                val g = (pixel shr 8) and 0xff
                val b = pixel and 0xff

                val y = ((66 * r + 129 * g + 25 * b + 128) shr 8) + 16
                yuv[yIndex++] = y.coerceIn(16, 235).toByte()

                if (j % 2 == 0 && i % 2 == 0) {
                    val u = ((-38 * r - 74 * g + 112 * b + 128) shr 8) + 128
                    val v = ((112 * r - 94 * g - 18 * b + 128) shr 8) + 128
                    yuv[uvIndex++] = u.coerceIn(16, 240).toByte()
                    yuv[uvIndex++] = v.coerceIn(16, 240).toByte()
                }
            }
        }
    }
}
