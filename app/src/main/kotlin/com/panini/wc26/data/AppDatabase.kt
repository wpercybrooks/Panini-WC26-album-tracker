package com.panini.wc26.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [Sticker::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun stickerDao(): StickerDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: android.content.Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = androidx.room.Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "panini_wc26.db"
                )
                .createFromAsset("catalog.db")
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
