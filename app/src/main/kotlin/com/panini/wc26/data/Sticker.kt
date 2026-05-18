package com.panini.wc26.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "stickers")
data class Sticker(
    @PrimaryKey val id: String,
    val name: String?,
    val country: String?,
    val group: String?,
    val ncopies: Int = 0
)
