package com.panini.wc26.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.panini.wc26.R
import com.panini.wc26.data.Sticker

class StickerAdapter(
    private val onHeaderClick: (String) -> Unit,
    private val onUpdateCount: (Sticker, Int) -> Unit
) : ListAdapter<ListItem, RecyclerView.ViewHolder>(DiffCallback()) {

    companion object {
        private const val TYPE_HEADER = 0
        private const val TYPE_STICKER = 1
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is ListItem.Header -> TYPE_HEADER
            is ListItem.StickerItem -> TYPE_STICKER
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            TYPE_HEADER -> HeaderViewHolder(inflater.inflate(R.layout.item_header, parent, false), onHeaderClick)
            TYPE_STICKER -> StickerViewHolder(inflater.inflate(R.layout.item_sticker, parent, false), onUpdateCount)
            else -> throw IllegalArgumentException("Unknown view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = getItem(position)
        when (holder) {
            is HeaderViewHolder -> holder.bind(item as ListItem.Header)
            is StickerViewHolder -> holder.bind(item as ListItem.StickerItem)
        }
    }

    class HeaderViewHolder(view: View, private val onClick: (String) -> Unit) : RecyclerView.ViewHolder(view) {
        private val title: TextView = view.findViewById(R.id.headerTitle)
        private val icon: android.widget.ImageView = view.findViewById(R.id.chevronIcon)

        fun bind(header: ListItem.Header) {
            title.text = header.title
            itemView.setPadding(header.level * 24, 12, 12, 12)
            
            icon.rotation = if (header.isExpanded) 0f else -90f
            itemView.setOnClickListener { onClick(header.key) }
        }
    }

    class StickerViewHolder(view: View, private val onUpdateCount: (Sticker, Int) -> Unit) : RecyclerView.ViewHolder(view) {
        private val container: View = view.findViewById(R.id.stickerContainer)
        private val code: TextView = view.findViewById(R.id.stickerCode)
        private val name: TextView = view.findViewById(R.id.stickerName)
        private val ncopies: TextView = view.findViewById(R.id.ncopies)
        private val btnMinus: ImageButton = view.findViewById(R.id.btnMinus)
        private val btnPlus: ImageButton = view.findViewById(R.id.btnPlus)

        fun bind(item: ListItem.StickerItem) {
            val s = item.sticker
            val context = itemView.context
            
            code.text = s.id
            name.text = s.name ?: "Unknown"
            ncopies.text = s.ncopies.toString()
            
            // Toggle visibility of +/- buttons when touching ncopies
            ncopies.setOnClickListener {
                val newVisibility = if (btnPlus.visibility == View.VISIBLE) View.GONE else View.VISIBLE
                btnMinus.visibility = newVisibility
                btnPlus.visibility = newVisibility
            }
            
            btnMinus.setOnClickListener {
                if (s.ncopies > 0) {
                    onUpdateCount(s, s.ncopies - 1)
                }
            }
            
            btnPlus.setOnClickListener {
                onUpdateCount(s, s.ncopies + 1)
            }
            
            val (bgDrawable, textColor, badgeColor) = when {
                s.ncopies >= 2 -> Triple(R.drawable.sticker_bg_owned, R.color.black, R.color.duplicate_orange)
                s.ncopies == 1 -> Triple(R.drawable.sticker_bg_owned, R.color.black, R.color.fifa_green)
                else -> Triple(R.drawable.sticker_bg_missing, R.color.gray, R.color.missing_red)
            }
            
            container.setBackgroundResource(bgDrawable)
            code.setTextColor(context.getColor(textColor))
            name.setTextColor(context.getColor(textColor))
            
            val background = ncopies.background as android.graphics.drawable.GradientDrawable
            background.setColor(context.getColor(badgeColor))
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<ListItem>() {
        override fun areItemsTheSame(oldItem: ListItem, newItem: ListItem): Boolean {
            return if (oldItem is ListItem.Header && newItem is ListItem.Header) {
                oldItem.title == newItem.title
            } else if (oldItem is ListItem.StickerItem && newItem is ListItem.StickerItem) {
                oldItem.sticker.id == newItem.sticker.id
            } else false
        }

        override fun areContentsTheSame(oldItem: ListItem, newItem: ListItem): Boolean {
            return oldItem == newItem
        }
    }
}