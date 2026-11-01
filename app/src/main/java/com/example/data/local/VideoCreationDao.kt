package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface VideoCreationDao {
    @Query("SELECT * FROM video_creations ORDER BY createdAt DESC")
    fun getAllCreations(): Flow<List<VideoCreationEntity>>

    @Query("SELECT * FROM video_creations WHERE id = :id LIMIT 1")
    suspend fun getCreationById(id: String): VideoCreationEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCreation(creation: VideoCreationEntity)

    @Delete
    suspend fun deleteCreation(creation: VideoCreationEntity)

    @Query("DELETE FROM video_creations WHERE id = :id")
    suspend fun deleteById(id: String)
}
