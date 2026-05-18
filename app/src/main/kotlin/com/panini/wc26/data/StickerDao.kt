package com.panini.wc26.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface StickerDao {
    @Query("SELECT * FROM stickers")
    suspend fun getAllStickers(): List<Sticker>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStickers(stickers: List<Sticker>)
}
