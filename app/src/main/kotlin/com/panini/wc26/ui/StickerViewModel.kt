package com.panini.wc26.ui

import android.app.Application
import android.content.ContentResolver
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.panini.wc26.data.AppDatabase
import com.panini.wc26.data.Sticker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

enum class FilterStatus {
    ALL, OWNED, MISSING, DUPLICATED
}

class StickerViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getDatabase(application)
    private val stickerDao = db.stickerDao()

    private val _items = MutableStateFlow<List<ListItem>>(emptyList())
    val items: StateFlow<List<ListItem>> = _items

    private val _progress = MutableStateFlow(Pair(0, 0))
    val progress: StateFlow<Pair<Int, Int>> = _progress

    private val _filterStatus = MutableStateFlow(FilterStatus.ALL)
    val filterStatus: StateFlow<FilterStatus> = _filterStatus

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private var allStickers: List<Sticker> = emptyList()
    private val toggleStates = mutableSetOf<String>()

    init {
        toggleStates.add("Intro/Museum")
        toggleStates.add("Coca-Cola")
        loadStickers()
    }

    private fun loadStickers() {
        viewModelScope.launch {
            allStickers = stickerDao.getAllStickers()
            val total = allStickers.size
            val owned = allStickers.count { it.ncopies > 0 }
            _progress.value = Pair(owned, total)
            updateList()
        }
    }

    fun exportData(uri: Uri, contentResolver: ContentResolver) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    val ownedStickers = allStickers.filter { it.ncopies > 0 }
                    val jsonArray = JSONArray()
                    ownedStickers.forEach { sticker ->
                        val obj = JSONObject().apply {
                            put("id", sticker.id)
                            put("ncopies", sticker.ncopies)
                        }
                        jsonArray.put(obj)
                    }
                    
                    contentResolver.openOutputStream(uri)?.use { outputStream ->
                        outputStream.write(jsonArray.toString(2).toByteArray())
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    fun importData(uri: Uri, contentResolver: ContentResolver) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    val jsonString = contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
                    if (jsonString != null) {
                        val jsonArray = JSONArray(jsonString)
                        val importMap = mutableMapOf<String, Int>()
                        for (i in 0 until jsonArray.length()) {
                            val obj = jsonArray.getJSONObject(i)
                            importMap[obj.getString("id")] = obj.getInt("ncopies")
                        }

                        val updatedList = allStickers.map { sticker ->
                            sticker.copy(ncopies = importMap[sticker.id] ?: 0)
                        }

                        stickerDao.insertStickers(updatedList)
                        withContext(Dispatchers.Main) {
                            loadStickers()
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    fun setFilterStatus(status: FilterStatus) {
        _filterStatus.value = status
        updateList()
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
        updateList()
    }

    fun toggleGroup(key: String) {
        if (toggleStates.contains(key)) {
            toggleStates.remove(key)
        } else {
            toggleStates.add(key)
        }
        updateList()
    }

    fun updateNCopies(sticker: Sticker, count: Int) {
        viewModelScope.launch {
            val updated = sticker.copy(ncopies = count)
            stickerDao.insertStickers(listOf(updated))
            loadStickers()
        }
    }

    private fun updateList() {
        val listItems = mutableListOf<ListItem>()
        
        val statusFiltered = when (_filterStatus.value) {
            FilterStatus.ALL -> allStickers
            FilterStatus.OWNED -> allStickers.filter { it.ncopies > 0 }
            FilterStatus.MISSING -> allStickers.filter { it.ncopies == 0 }
            FilterStatus.DUPLICATED -> allStickers.filter { it.ncopies > 1 }
        }

        val query = _searchQuery.value.lowercase()
        val filteredStickers = if (query.isEmpty()) {
            statusFiltered
        } else {
            statusFiltered.filter { 
                it.id.lowercase().contains(query) || 
                (it.name?.lowercase()?.contains(query) ?: false) ||
                (it.country?.lowercase()?.contains(query) ?: false)
            }
        }

        val sortedGroups = filteredStickers.groupBy { it.group ?: "Unknown" }
            .toList()
            .sortedBy { (name, _) ->
                when {
                    name == "Intro/Museum" -> 0
                    name.startsWith("Group") -> 1
                    name == "Coca-Cola" -> 2
                    else -> 3
                }
            }
        
        for ((groupName, stickersInGroup) in sortedGroups) {
            val isExpanded = !toggleStates.contains(groupName)
            listItems.add(ListItem.Header(groupName, 1, isExpanded, groupName))
            
            if (isExpanded) {
                if (groupName == "Intro/Museum" || groupName == "Coca-Cola") {
                    // Flatten: Sort by numeric ID directly
                    val sortedStickers = stickersInGroup.sortedBy { s ->
                        s.id.split(" ").lastOrNull()?.toIntOrNull() ?: 0
                    }
                    for (sticker in sortedStickers) {
                        listItems.add(ListItem.StickerItem(sticker))
                    }
                } else {
                    val byNation = stickersInGroup.groupBy { it.country ?: "Unknown" }
                    for ((nationName, stickersInNation) in byNation) {
                        val nationKey = "$groupName:$nationName"
                        val isNationExpanded = toggleStates.contains(nationKey)
                        listItems.add(ListItem.Header(nationName, 2, isNationExpanded, nationKey))
                        
                        if (isNationExpanded) {
                            val sortedStickers = stickersInNation.sortedBy { s ->
                                s.id.split(" ").lastOrNull()?.toIntOrNull() ?: 0
                            }
                            for (sticker in sortedStickers) {
                                listItems.add(ListItem.StickerItem(sticker))
                            }
                        }
                    }
                }
            }
        }
        _items.value = listItems
    }
}
