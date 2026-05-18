package com.panini.wc26.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.panini.wc26.R

class NationStatAdapter : ListAdapter<NationStat, NationStatAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_nation_stat, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val name: TextView = view.findViewById(R.id.nationName)
        private val progressText: TextView = view.findViewById(R.id.nationProgressText)
        private val duplicatesText: TextView = view.findViewById(R.id.nationDuplicatesText)
        private val progressBar: ProgressBar = view.findViewById(R.id.nationProgressBar)

        fun bind(stat: NationStat) {
            name.text = stat.name
            val percent = if (stat.total > 0) (stat.owned * 100) / stat.total else 0
            progressText.text = "${stat.owned}/${stat.total} ($percent%)"
            duplicatesText.text = if (stat.duplicates > 0) "${stat.duplicates} duplicates" else ""
            progressBar.progress = percent
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<NationStat>() {
        override fun areItemsTheSame(oldItem: NationStat, newItem: NationStat) = oldItem.name == newItem.name
        override fun areContentsTheSame(oldItem: NationStat, newItem: NationStat) = oldItem == newItem
    }
}