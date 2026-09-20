package com.example.timewallet.ui.history

import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.recyclerview.widget.RecyclerView
import com.example.timewallet.data.session.SessionEntry
import com.example.timewallet.databinding.ItemSessionHistoryBinding
import com.example.timewallet.R

class SessionHistoryAdapter :
    RecyclerView.Adapter<SessionHistoryAdapter.ViewHolder>() {

    private var items = listOf<SessionEntry>()

    fun submitList(list: List<SessionEntry>) {
        items = list
        notifyDataSetChanged()
    }

    class ViewHolder(val binding: ItemSessionHistoryBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemSessionHistoryBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val entry = items[position]
        holder.binding.minutesText.text = "${entry.minutes} Minuten"
        holder.binding.scoreText.text = "Score: ${entry.score}"
        holder.binding.validText.text = if (entry.valid) "✔" else "❌"
        holder.binding.timeText.text = entry.timestamp.toString()

        holder.itemView.startAnimation(
            AnimationUtils.loadAnimation(holder.itemView.context, R.anim.item_fade_in)
        )
    }
}
