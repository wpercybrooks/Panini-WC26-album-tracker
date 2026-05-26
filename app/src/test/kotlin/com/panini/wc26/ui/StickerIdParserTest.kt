package com.panini.wc26.ui

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class StickerIdParserTest {

    @Test
    fun `test standard ID extraction`() {
        assertEquals("COL 1", StickerIdParser.extractStickerId("COL 1"))
        assertEquals("FWC 19", StickerIdParser.extractStickerId("FWC 19"))
        assertEquals("CC 14", StickerIdParser.extractStickerId("CC 14"))
    }

    @Test
    fun `test ID extraction without spaces`() {
        assertEquals("COL 1", StickerIdParser.extractStickerId("COL1"))
        assertEquals("FWC 19", StickerIdParser.extractStickerId("FWC19"))
    }

    @Test
    fun `test ID extraction with OCR errors`() {
        // O instead of 0
        assertEquals("COL 10", StickerIdParser.extractStickerId("COL 1O"))
        assertEquals("FWC 0", StickerIdParser.extractStickerId("FWC O"))
    }

    @Test
    fun `test case insensitivity`() {
        assertEquals("COL 12", StickerIdParser.extractStickerId("col 12"))
        assertEquals("FWC 5", StickerIdParser.extractStickerId("fwc 5"))
    }

    @Test
    fun `test no match`() {
        assertNull(StickerIdParser.extractStickerId("No sticker here"))
        assertNull(StickerIdParser.extractStickerId("123456"))
        assertNull(StickerIdParser.extractStickerId("ABC"))
    }
}