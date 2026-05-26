package com.panini.wc26.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

import kotlinx.coroutines.flow.Flow

@Dao
interface StickerDao {
    @Query("SELECT * FROM stickers")
    fun getAllStickersFlow(): Flow<List<Sticker>>

    @Query("SELECT * FROM stickers")
    suspend fun getAllStickers(): List<Sticker>

    @Query("SELECT * FROM stickers WHERE id = :id")
    suspend fun getStickerById(id: String): Sticker?

    @Query("SELECT id FROM stickers")
    suspend fun getAllStickerIds(): List<String>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStickers(stickers: List<Sticker>)
}
