package com.example.ui.components

import android.media.MediaPlayer
import android.net.Uri
import android.widget.VideoView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.ElectricPurple
import kotlinx.coroutines.delay
import java.io.File
import java.util.Locale

@Composable
fun VideoPlayerView(
    videoFile: File,
    aspectRatio: Float = 9f / 16f,
    modifier: Modifier = Modifier,
    autoPlay: Boolean = true
) {
    val context = LocalContext.current
    var isPlaying by remember { mutableStateOf(autoPlay) }
    var currentPosition by remember { mutableIntStateOf(0) }
    var totalDuration by remember { mutableIntStateOf(1) }
    var isMuted by remember { mutableStateOf(false) }
    var isCompleted by remember { mutableStateOf(false) }
    var showControls by remember { mutableStateOf(true) }

    var videoViewInstance by remember { mutableStateOf<VideoView?>(null) }
    var mediaPlayerInstance by remember { mutableStateOf<MediaPlayer?>(null) }

    // Auto-hide controls after 3 seconds of playing
    LaunchedEffect(showControls, isPlaying) {
        if (showControls && isPlaying) {
            delay(3000)
            showControls = false
        }
    }

    // Polling position
    LaunchedEffect(isPlaying) {
        while (isPlaying) {
            videoViewInstance?.let { vv ->
                if (vv.isPlaying) {
                    currentPosition = vv.currentPosition
                    if (vv.duration > 0) {
                        totalDuration = vv.duration
                    }
                }
            }
            delay(200)
        }
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(Color.Black)
            .clickable { showControls = !showControls },
        contentAlignment = Alignment.Center
    ) {
        AndroidView(
            factory = { ctx ->
                VideoView(ctx).apply {
                    setVideoURI(Uri.fromFile(videoFile))
                    setOnPreparedListener { mp ->
                        mediaPlayerInstance = mp
                        totalDuration = duration.coerceAtLeast(1000)
                        mp.isLooping = true
                        if (autoPlay) {
                            start()
                            isPlaying = true
                        }
                    }
                    setOnCompletionListener {
                        isCompleted = true
                        isPlaying = false
                    }
                    videoViewInstance = this
                }
            },
            modifier = Modifier
                .fillMaxSize()
                .testTag("video_surface_view")
        )

        // Overlay Controls
        AnimatedVisibility(
            visible = showControls || !isPlaying,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.45f))
            ) {
                // Center Play/Pause button
                IconButton(
                    onClick = {
                        videoViewInstance?.let { vv ->
                            if (vv.isPlaying) {
                                vv.pause()
                                isPlaying = false
                            } else {
                                if (isCompleted) {
                                    vv.seekTo(0)
                                    isCompleted = false
                                }
                                vv.start()
                                isPlaying = true
                            }
                        }
                    },
                    modifier = Modifier
                        .size(68.dp)
                        .align(Alignment.Center)
                        .clip(CircleShape)
                        .background(ElectricPurple.copy(alpha = 0.85f))
                        .testTag("play_pause_button")
                ) {
                    Icon(
                        imageVector = if (isCompleted) Icons.Default.Replay else if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isPlaying) "Pause" else "Lecture",
                        tint = Color.White,
                        modifier = Modifier.size(36.dp)
                    )
                }

                // Bottom bar (Slider, times, mute)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .background(Color.Black.copy(alpha = 0.6f))
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Slider(
                        value = (currentPosition.toFloat() / totalDuration.toFloat()).coerceIn(0f, 1f),
                        onValueChange = { frac ->
                            val target = (frac * totalDuration).toInt()
                            currentPosition = target
                            videoViewInstance?.seekTo(target)
                        },
                        colors = SliderDefaults.colors(
                            thumbColor = ElectricCyan,
                            activeTrackColor = ElectricCyan,
                            inactiveTrackColor = Color.White.copy(alpha = 0.3f)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("video_seek_slider")
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = formatMs(currentPosition) + " / " + formatMs(totalDuration),
                            color = Color.White,
                            fontSize = 12.sp
                        )

                        IconButton(
                            onClick = {
                                isMuted = !isMuted
                                mediaPlayerInstance?.let { mp ->
                                    val vol = if (isMuted) 0f else 1f
                                    mp.setVolume(vol, vol)
                                }
                            },
                            modifier = Modifier
                                .size(36.dp)
                                .testTag("mute_unmute_button")
                        ) {
                            Icon(
                                imageVector = if (isMuted) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
                                contentDescription = if (isMuted) "Activer le son" else "Couper le son",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            videoViewInstance?.stopPlayback()
        }
    }
}

private fun formatMs(ms: Int): String {
    val totalSeconds = (ms / 1000).coerceAtLeast(0)
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
}
