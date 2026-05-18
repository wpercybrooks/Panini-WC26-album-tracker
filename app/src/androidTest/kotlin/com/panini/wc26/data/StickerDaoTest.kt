package com.panini.wc26.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class StickerDaoTest {
    private lateinit var stickerDao: StickerDao
    private lateinit var db: AppDatabase

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        stickerDao = db.stickerDao()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    @Test
    @Throws(Exception::class)
    fun writeAndReadStickers() = runBlocking {
        val stickers = listOf(
            Sticker("COL 1", "James", "Colombia", "Group K", 0),
            Sticker("BRA 10", "Neymar", "Brazil", "Group A", 2)
        )
        stickerDao.insertStickers(stickers)
        val allStickers = stickerDao.getAllStickers()
        assertEquals(2, allStickers.size)
        assertEquals("James", allStickers.find { it.id == "COL 1" }?.name)
        assertEquals(2, allStickers.find { it.id == "BRA 10" }?.ncopies)
    }

    @Test
    @Throws(Exception::class)
    fun updateStickerCopies() = runBlocking {
        val sticker = Sticker("COL 1", "James", "Colombia", "Group K", 0)
        stickerDao.insertStickers(listOf(sticker))
        
        val updated = sticker.copy(ncopies = 5)
        stickerDao.insertStickers(listOf(updated))
        
        val allStickers = stickerDao.getAllStickers()
        assertEquals(1, allStickers.size)
        assertEquals(5, allStickers[0].ncopies)
    }
}
