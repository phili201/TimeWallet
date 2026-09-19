package com.example.timewallet.ui.history

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.timewallet.data.coins.CoinEntry
import com.example.timewallet.databinding.ItemCoinHistoryBinding

class CoinHistoryAdapter :
    RecyclerView.Adapter<CoinHistoryAdapter.ViewHolder>() {

    private var items = listOf<CoinEntry>()

    fun submitList(list: List<CoinEntry>) {
        items = list
        notifyDataSetChanged()
    }

    class ViewHolder(val binding: ItemCoinHistoryBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemCoinHistoryBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val entry = items[position]
        holder.binding.amountText.text = "${entry.amount} Coins"
        holder.binding.reasonText.text = entry.reason
        holder.binding.timeText.text = entry.timestamp.toString()
    }
}
