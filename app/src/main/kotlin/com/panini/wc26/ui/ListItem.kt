package com.panini.wc26.ui

import com.panini.wc26.data.Sticker

sealed class ListItem {
    data class Header(val title: String, val level: Int, var isExpanded: Boolean = true, val key: String = title) : ListItem()
    data class StickerItem(val sticker: Sticker) : ListItem()
}
