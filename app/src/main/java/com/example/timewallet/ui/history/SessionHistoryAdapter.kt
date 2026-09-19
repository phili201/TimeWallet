package com.example.timewallet.ui.history

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.timewallet.data.session.SessionEntry
import com.example.timewallet.databinding.ItemSessionHistoryBinding

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
        holder.binding.minutesText.text = "${entry.minutes} min"
        holder.binding.scoreText.text = "Score: ${entry.score}"
        holder.binding.validText.text = if (entry.valid) "✔ gültig" else "❌ ungültig"
        holder.binding.timeText.text = entry.timestamp.toString()
    }
}
