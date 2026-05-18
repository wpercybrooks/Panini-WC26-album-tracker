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
import com.panini.wc26.ui.NationStatAdapter
import com.panini.wc26.ui.StatsSortMode
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.bottomnavigation.BottomNavigationView
import android.view.Menu
import android.view.MenuItem
import android.view.View
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private val viewModel: StickerViewModel by viewModels()
    private lateinit var adapter: StickerAdapter
    private lateinit var nationStatAdapter: NationStatAdapter

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

        // Collection Views
        val collectionContainer: View = findViewById(R.id.collectionContainer)
        val recyclerView: RecyclerView = findViewById(R.id.recyclerView)
        val progressText: TextView = findViewById(R.id.progressText)
        val progressBar: ProgressBar = findViewById(R.id.progressBar)
        val filterGroup: ChipGroup = findViewById(R.id.filterGroup)
        val searchView: androidx.appcompat.widget.SearchView = findViewById(R.id.searchView)

        // Stats Views
        val statsContainer: View = findViewById(R.id.statsContainer)
        val statsGlobalProgressText: TextView = findViewById(R.id.statsGlobalProgressText)
        val statsGlobalProgressBar: ProgressBar = findViewById(R.id.statsGlobalProgressBar)
        val statsTotalSwapsText: TextView = findViewById(R.id.statsTotalSwapsText)
        val statsTopRepeatedText: TextView = findViewById(R.id.statsTopRepeatedText)
        val statsSortGroup: ChipGroup = findViewById(R.id.statsSortGroup)
        val nationStatsRecyclerView: RecyclerView = findViewById(R.id.nationStatsRecyclerView)

        // Navigation
        val bottomNavigation: BottomNavigationView = findViewById(R.id.bottomNavigation)

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

        statsSortGroup.setOnCheckedStateChangeListener { group, checkedIds ->
            val mode = when (checkedIds.firstOrNull()) {
                R.id.chipSortName -> StatsSortMode.NAME
                else -> StatsSortMode.PERCENTAGE
            }
            viewModel.setStatsSortMode(mode)
        }

        // Setup Collection Adapter
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

        // Setup Stats Adapter
        nationStatAdapter = NationStatAdapter()
        nationStatsRecyclerView.layoutManager = LinearLayoutManager(this)
        nationStatsRecyclerView.adapter = nationStatAdapter

        // Bottom Navigation Logic
        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_collection -> {
                    collectionContainer.visibility = View.VISIBLE
                    statsContainer.visibility = View.GONE
                    true
                }
                R.id.nav_stats -> {
                    collectionContainer.visibility = View.GONE
                    statsContainer.visibility = View.VISIBLE
                    true
                }
                else -> false
            }
        }

        androidx.core.view.ViewCompat.setOnApplyWindowInsetsListener(recyclerView) { v, insets ->
            val systemBars = insets.getInsets(androidx.core.view.WindowInsetsCompat.Type.systemBars())
            v.setPadding(v.paddingLeft, v.paddingTop, v.paddingRight, systemBars.bottom + 16)
            insets
        }

        // Observe ViewModel Flows
        lifecycleScope.launch {
            viewModel.items.collect { items ->
                adapter.submitList(items)
            }
        }

        lifecycleScope.launch {
            viewModel.progress.collect { (owned, total) ->
                updateProgress(owned, total, progressText, progressBar)
                updateProgress(owned, total, statsGlobalProgressText, statsGlobalProgressBar)
            }
        }

        lifecycleScope.launch {
            viewModel.totalSwaps.collect { total ->
                statsTotalSwapsText.text = "$total stickers"
            }
        }

        lifecycleScope.launch {
            viewModel.topRepeated.collect { stickers ->
                val text = stickers.joinToString("\n") { sticker ->
                    "${sticker.id} (${sticker.ncopies} copies)"
                }.ifEmpty { "No duplicates yet." }
                statsTopRepeatedText.text = text
            }
        }

        lifecycleScope.launch {
            viewModel.nationStats.collect { stats ->
                nationStatAdapter.submitList(stats)
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