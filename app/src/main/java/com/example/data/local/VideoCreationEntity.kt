package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "video_creations")
data class VideoCreationEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val mode: String, // "SCRIPT" or "PHOTO"
    val aspectRatio: String, // "9:16", "16:9", "1:1"
    val durationSeconds: Int,
    val videoFilePath: String,
    val thumbnailFilePath: String,
    val scriptOrCaption: String,
    val styleName: String,
    val musicTrackName: String,
    val createdAt: Long = System.currentTimeMillis()
)
