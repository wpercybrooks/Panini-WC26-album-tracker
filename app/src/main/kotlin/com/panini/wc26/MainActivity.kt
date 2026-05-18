package com.panini.wc26

import android.os.Bundle
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.panini.wc26.R
import com.panini.wc26.data.Sticker
import com.panini.wc26.ui.FilterStatus
import com.panini.wc26.ui.ListItem
import com.panini.wc26.ui.StickerAdapter
import com.panini.wc26.ui.StickerViewModel
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import android.view.Menu
import android.view.MenuItem
import android.view.View
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private val viewModel: StickerViewModel by viewModels()
    private lateinit var adapter: StickerAdapter

    private val exportLauncher = registerForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        uri?.let { viewModel.exportData(it, contentResolver) }
    }

    private val importLauncher = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let { selectedUri ->
            androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Import Backup")
                .setMessage("This will overwrite your current progress. This action cannot be undone. Do you want to proceed?")
                .setPositiveButton("Import") { _, _ ->
                    viewModel.importData(selectedUri, contentResolver)
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val recyclerView: RecyclerView = findViewById(R.id.recyclerView)
        val progressText: TextView = findViewById(R.id.progressText)
        val progressBar: ProgressBar = findViewById(R.id.progressBar)
        val filterGroup: ChipGroup = findViewById(R.id.filterGroup)
        val searchView: androidx.appcompat.widget.SearchView = findViewById(R.id.searchView)

        searchView.setOnQueryTextListener(object : androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = false
            override fun onQueryTextChange(newText: String?): Boolean {
                viewModel.setSearchQuery(newText ?: "")
                return true
            }
        })

        filterGroup.setOnCheckedStateChangeListener { group, checkedIds ->
            val status = when (checkedIds.firstOrNull()) {
                R.id.chipOwned -> FilterStatus.OWNED
                R.id.chipMissing -> FilterStatus.MISSING
                R.id.chipDuplicated -> FilterStatus.DUPLICATED
                else -> FilterStatus.ALL
            }
            viewModel.setFilterStatus(status)
        }

        adapter = StickerAdapter(
            onHeaderClick = { groupName ->
                viewModel.toggleGroup(groupName)
            },
            onUpdateCount = { sticker, newCount ->
                viewModel.updateNCopies(sticker, newCount)
            }
        )
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        androidx.core.view.ViewCompat.setOnApplyWindowInsetsListener(recyclerView) { v, insets ->
            val systemBars = insets.getInsets(androidx.core.view.WindowInsetsCompat.Type.systemBars())
            v.setPadding(v.paddingLeft, v.paddingTop, v.paddingRight, systemBars.bottom + 16)
            insets
        }

        lifecycleScope.launch {
            viewModel.items.collect { items ->
                adapter.submitList(items)
            }
        }

        lifecycleScope.launch {
            viewModel.progress.collect { (owned, total) ->
                updateProgress(owned, total, progressText, progressBar)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_export -> {
                exportLauncher.launch("panini_backup.json")
                true
            }
            R.id.action_import -> {
                importLauncher.launch(arrayOf("application/json"))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun updateProgress(owned: Int, total: Int, text: TextView, bar: ProgressBar) {
        if (total == 0) return
        val percent = (owned * 100) / total

        text.text = "$owned/$total - $percent%"
        bar.progress = percent
    }
}