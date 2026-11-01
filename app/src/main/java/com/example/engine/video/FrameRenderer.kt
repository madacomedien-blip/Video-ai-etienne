package com.example.engine.video

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RadialGradient
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import com.example.data.model.AnimationStyle
import com.example.data.model.ScriptScene
import com.example.data.model.VisualFilter
import kotlin.math.cos
import kotlin.math.sin

class FrameRenderer {

    private val basePaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        textAlign = Paint.Align.CENTER
    }
    private val cardPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.argb(200, 15, 12, 28)
        style = Paint.Style.FILL
    }
    private val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.argb(120, 142, 90, 255)
        style = Paint.Style.STROKE
        strokeWidth = 3f
    }
    private val particlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    /**
     * Renders a single frame for Photo Animation Mode
     */
    fun renderPhotoFrame(
        sourceBitmap: Bitmap,
        targetWidth: Int,
        targetHeight: Int,
        progress: Float, // 0.0f to 1.0f
        animationStyle: AnimationStyle,
        visualFilter: VisualFilter,
        subtitleText: String?,
        watermarkText: String = "Video AI Étienne"
    ): Bitmap {
        val output = Bitmap.createBitmap(targetWidth, targetHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)

        // 1. Calculate transformation (Ken Burns Zoom & Pan)
        val baseScale = maxOf(
            targetWidth.toFloat() / sourceBitmap.width,
            targetHeight.toFloat() / sourceBitmap.height
        )

        var zoomFactor = when (animationStyle) {
            AnimationStyle.KEN_BURNS_ZOOM -> 1.0f + 0.22f * progress
            AnimationStyle.PARALLAX_BREATHING -> 1.05f + 0.05f * sin(progress * Math.PI.toFloat() * 4).toFloat()
            AnimationStyle.ANIME_SPEED_LINES -> 1.0f + 0.15f * (progress % 0.5f * 2)
            AnimationStyle.CINEMATIC_GLOW -> 1.02f + 0.12f * progress
            AnimationStyle.CYBERPUNK_PULSE -> 1.0f + 0.08f * sin(progress * Math.PI.toFloat() * 6).toFloat()
        }

        val totalScale = baseScale * zoomFactor
        val scaledW = sourceBitmap.width * totalScale
        val scaledH = sourceBitmap.height * totalScale

        // Subtle pan
        val panX = (targetWidth - scaledW) * 0.5f + (sin(progress * Math.PI.toFloat()) * targetWidth * 0.04f)
        val panY = (targetHeight - scaledH) * 0.5f + (cos(progress * Math.PI.toFloat()) * targetHeight * 0.03f)

        canvas.save()
        canvas.translate(panX, panY)
        canvas.scale(totalScale, totalScale)
        canvas.drawBitmap(sourceBitmap, 0f, 0f, basePaint)
        canvas.restore()

        // 2. Draw Animation Overlays
        drawEffectOverlays(canvas, targetWidth, targetHeight, progress, animationStyle)

        // 3. Draw Visual Filter Tint & Vignette
        applyVisualFilter(canvas, targetWidth, targetHeight, visualFilter)

        // 4. Draw Subtitle & Watermark
        drawSubtitlesAndBranding(canvas, targetWidth, targetHeight, subtitleText, watermarkText)

        return output
    }

    /**
     * Renders a single frame for Script Storyboard Mode
     */
    fun renderScriptFrame(
        scene: ScriptScene,
        sceneProgress: Float, // 0f to 1f within this scene
        targetWidth: Int,
        targetHeight: Int,
        animationStyle: AnimationStyle,
        visualFilter: VisualFilter,
        watermarkText: String = "Video AI Étienne"
    ): Bitmap {
        val output = Bitmap.createBitmap(targetWidth, targetHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)

        // Draw animated artistic background for this scene
        drawArtisticSceneBackdrop(canvas, targetWidth, targetHeight, scene, sceneProgress)

        // Draw dynamic effects
        drawEffectOverlays(canvas, targetWidth, targetHeight, sceneProgress, animationStyle)

        // Apply filter
        applyVisualFilter(canvas, targetWidth, targetHeight, visualFilter)

        // Draw scene title & subtitles
        drawSceneScriptCaptions(canvas, targetWidth, targetHeight, scene, sceneProgress, watermarkText)

        return output
    }

    private fun drawArtisticSceneBackdrop(
        canvas: Canvas,
        w: Int,
        h: Int,
        scene: ScriptScene,
        progress: Float
    ) {
        val p = Paint(Paint.ANTI_ALIAS_FLAG)

        // Dynamic multi-stop gradient sky
        val topColor = when (scene.order % 4) {
            0 -> Color.rgb(15, 8, 38)
            1 -> Color.rgb(4, 20, 48)
            2 -> Color.rgb(38, 12, 28)
            else -> Color.rgb(8, 28, 36)
        }
        val bottomColor = when (scene.order % 4) {
            0 -> Color.rgb(65, 30, 115)
            1 -> Color.rgb(10, 60, 110)
            2 -> Color.rgb(115, 35, 60)
            else -> Color.rgb(20, 85, 95)
        }

        p.shader = LinearGradient(0f, 0f, 0f, h.toFloat(), topColor, bottomColor, Shader.TileMode.CLAMP)
        canvas.drawRect(0f, 0f, w.toFloat(), h.toFloat(), p)

        // Animated glowing celestial body (Sun/Moon/Core)
        val moonX = w * (0.3f + 0.4f * progress)
        val moonY = h * 0.28f
        val moonRadius = w * 0.22f

        val glowColor = Color.argb(120, 255, 230, 180)
        p.shader = RadialGradient(
            moonX, moonY, moonRadius * 1.8f,
            intArrayOf(glowColor, Color.argb(50, 142, 90, 255), Color.TRANSPARENT),
            floatArrayOf(0f, 0.5f, 1f),
            Shader.TileMode.CLAMP
        )
        canvas.drawCircle(moonX, moonY, moonRadius * 1.8f, p)

        // Soft moon disc
        p.shader = null
        p.color = Color.argb(220, 255, 245, 225)
        canvas.drawCircle(moonX, moonY, moonRadius * 0.55f, p)

        // Mountains / Landscape silhouettes
        drawMountainSilhouettes(canvas, w, h, progress, scene.order)
    }

    private fun drawMountainSilhouettes(canvas: Canvas, w: Int, h: Int, progress: Float, sceneIndex: Int) {
        val p = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }

        // Distant mountains
        p.color = Color.argb(140, 25, 18, 55)
        val path1 = Path()
        path1.moveTo(0f, h * 0.65f)
        path1.lineTo(w * 0.25f, h * 0.52f)
        path1.lineTo(w * 0.55f, h * 0.62f)
        path1.lineTo(w * 0.85f, h * 0.48f)
        path1.lineTo(w.toFloat(), h * 0.60f)
        path1.lineTo(w.toFloat(), h.toFloat())
        path1.lineTo(0f, h.toFloat())
        path1.close()
        canvas.drawPath(path1, p)

        // Foreground mountain silhouette with parallax pan
        val offset = (progress - 0.5f) * w * 0.08f
        p.color = Color.argb(230, 12, 9, 28)
        val path2 = Path()
        path2.moveTo(-w * 0.1f + offset, h.toFloat())
        path2.lineTo(0f + offset, h * 0.72f)
        path2.lineTo(w * 0.35f + offset, h * 0.60f)
        path2.lineTo(w * 0.70f + offset, h * 0.75f)
        path2.lineTo(w * 1.1f + offset, h * 0.58f)
        path2.lineTo(w * 1.2f + offset, h.toFloat())
        path2.close()
        canvas.drawPath(path2, p)
    }

    private fun drawEffectOverlays(
        canvas: Canvas,
        w: Int,
        h: Int,
        progress: Float,
        style: AnimationStyle
    ) {
        when (style) {
            AnimationStyle.ANIME_SPEED_LINES -> {
                val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = Color.argb(80, 255, 255, 255)
                    strokeWidth = 2.5f
                    this.style = Paint.Style.STROKE
                }
                val centerX = w * 0.5f
                val centerY = h * 0.5f
                val count = 28
                for (i in 0 until count) {
                    val angle = (i.toDouble() / count) * 2 * Math.PI + (progress * 0.2)
                    val rInner = w * 0.28f + (i % 3) * 15f
                    val rOuter = w * 0.95f
                    val x1 = centerX + (cos(angle) * rInner).toFloat()
                    val y1 = centerY + (sin(angle) * rInner).toFloat()
                    val x2 = centerX + (cos(angle) * rOuter).toFloat()
                    val y2 = centerY + (sin(angle) * rOuter).toFloat()
                    canvas.drawLine(x1, y1, x2, y2, linePaint)
                }
            }
            AnimationStyle.CINEMATIC_GLOW, AnimationStyle.PARALLAX_BREATHING -> {
                // Floating glowing embers
                val particleCount = 20
                for (i in 0 until particleCount) {
                    val seed = i * 137.5
                    val px = ((seed % w) + sin(progress * 4 + i) * 30).toFloat()
                    val py = ((h - ((progress * 1.4f * h + seed) % h))).toFloat()
                    val radius = 3f + (i % 5) * 2f
                    val alpha = (120 + 100 * sin(progress * 6 + i)).toInt().coerceIn(30, 220)

                    particlePaint.color = if (i % 2 == 0) {
                        Color.argb(alpha, 0, 229, 255) // Cyan
                    } else {
                        Color.argb(alpha, 255, 183, 3) // Gold
                    }
                    canvas.drawCircle(px, py, radius, particlePaint)
                }
            }
            AnimationStyle.CYBERPUNK_PULSE -> {
                // Horizontal scanline pulse
                val scanY = (progress * h * 1.2f) % h
                val scanPaint = Paint().apply {
                    color = Color.argb(70, 0, 229, 255)
                    strokeWidth = 4f
                }
                canvas.drawLine(0f, scanY, w.toFloat(), scanY, scanPaint)
            }
            else -> {}
        }
    }

    private fun applyVisualFilter(canvas: Canvas, w: Int, h: Int, filter: VisualFilter) {
        val filterPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        when (filter) {
            VisualFilter.ANIME_VIBRANT -> {
                filterPaint.color = Color.argb(25, 255, 100, 200)
                canvas.drawRect(0f, 0f, w.toFloat(), h.toFloat(), filterPaint)
            }
            VisualFilter.WARM_CINEMA -> {
                filterPaint.color = Color.argb(30, 255, 180, 50)
                canvas.drawRect(0f, 0f, w.toFloat(), h.toFloat(), filterPaint)
            }
            VisualFilter.CYBERPUNK -> {
                filterPaint.color = Color.argb(35, 142, 90, 255)
                canvas.drawRect(0f, 0f, w.toFloat(), h.toFloat(), filterPaint)
            }
            VisualFilter.DREAMY_FANTASY -> {
                filterPaint.color = Color.argb(30, 100, 220, 255)
                canvas.drawRect(0f, 0f, w.toFloat(), h.toFloat(), filterPaint)
            }
            VisualFilter.VINTAGE_CARTOON -> {
                filterPaint.color = Color.argb(40, 200, 170, 120)
                canvas.drawRect(0f, 0f, w.toFloat(), h.toFloat(), filterPaint)
            }
        }

        // Cinematic vignette around edges
        val vignettePaint = Paint(Paint.ANTI_ALIAS_FLAG)
        val cx = w * 0.5f
        val cy = h * 0.5f
        val radius = maxOf(w, h) * 0.75f
        vignettePaint.shader = RadialGradient(
            cx, cy, radius,
            intArrayOf(Color.TRANSPARENT, Color.argb(160, 5, 4, 12)),
            floatArrayOf(0.55f, 1.0f),
            Shader.TileMode.CLAMP
        )
        canvas.drawRect(0f, 0f, w.toFloat(), h.toFloat(), vignettePaint)
    }

    private fun drawSubtitlesAndBranding(
        canvas: Canvas,
        w: Int,
        h: Int,
        subtitleText: String?,
        watermarkText: String
    ) {
        // Watermark top left
        textPaint.textSize = (w * 0.032f).coerceIn(16f, 32f)
        textPaint.textAlign = Paint.Align.LEFT
        textPaint.color = Color.argb(200, 255, 255, 255)

        val brandPadding = w * 0.04f
        canvas.drawText("⚡ $watermarkText", brandPadding, brandPadding + textPaint.textSize, textPaint)

        // Subtitles bottom
        if (!subtitleText.isNullOrBlank()) {
            drawSubtitleCard(canvas, w, h, subtitleText)
        }
    }

    private fun drawSceneScriptCaptions(
        canvas: Canvas,
        w: Int,
        h: Int,
        scene: ScriptScene,
        progress: Float,
        watermarkText: String
    ) {
        // Watermark top left
        textPaint.textSize = (w * 0.032f).coerceIn(16f, 30f)
        textPaint.textAlign = Paint.Align.LEFT
        textPaint.color = Color.argb(200, 255, 255, 255)
        val brandPadding = w * 0.04f
        canvas.drawText("⚡ $watermarkText", brandPadding, brandPadding + textPaint.textSize, textPaint)

        // Scene Act badge top center
        textPaint.textAlign = Paint.Align.CENTER
        textPaint.textSize = (w * 0.035f).coerceIn(18f, 34f)
        textPaint.color = Color.rgb(0, 229, 255)
        canvas.drawText(scene.sceneTitle, w * 0.5f, brandPadding + textPaint.textSize * 1.2f, textPaint)

        // Subtitles bottom
        val sub = scene.subtitleText.ifBlank { scene.narrationText }
        drawSubtitleCard(canvas, w, h, sub)
    }

    private fun drawSubtitleCard(canvas: Canvas, w: Int, h: Int, text: String) {
        val cardWidth = w * 0.90f
        val cardHeight = h * 0.12f
        val left = (w - cardWidth) * 0.5f
        val top = h * 0.83f
        val rect = RectF(left, top, left + cardWidth, top + cardHeight)

        canvas.drawRoundRect(rect, 24f, 24f, cardPaint)
        canvas.drawRoundRect(rect, 24f, 24f, borderPaint)

        textPaint.textSize = (w * 0.038f).coerceIn(18f, 36f)
        textPaint.textAlign = Paint.Align.CENTER
        textPaint.color = Color.WHITE

        // Wrap into 2 lines if needed
        val words = text.split(" ")
        if (words.size > 8) {
            val mid = words.size / 2
            val line1 = words.subList(0, mid).joinToString(" ")
            val line2 = words.subList(mid, words.size).joinToString(" ")

            val textY1 = top + cardHeight * 0.42f
            val textY2 = top + cardHeight * 0.80f
            canvas.drawText(line1, w * 0.5f, textY1, textPaint)
            canvas.drawText(line2, w * 0.5f, textY2, textPaint)
        } else {
            val textY = top + cardHeight * 0.62f
            canvas.drawText(text, w * 0.5f, textY, textPaint)
        }
    }
}
