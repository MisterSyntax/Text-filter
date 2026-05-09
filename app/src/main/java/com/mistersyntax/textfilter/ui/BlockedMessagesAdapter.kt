package com.mistersyntax.textfilter.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.mistersyntax.textfilter.db.BlockedMessage
import com.mistersyntax.textfilter.databinding.ItemBlockedMessageBinding

class BlockedMessagesAdapter(
    private val onDeleteClick: (BlockedMessage) -> Unit,
) : ListAdapter<BlockedMessage, BlockedMessagesAdapter.ViewHolder>(DIFF) {

    inner class ViewHolder(private val binding: ItemBlockedMessageBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(message: BlockedMessage) {
            binding.tvSender.text = message.sender
            binding.tvBody.text = message.body
            binding.tvDate.text = message.receivedAt.toDisplayDate()
            binding.tvScore.text = "Score: %.2f".format(message.score)
            binding.tvRules.text = message.matchedRules.replace(",", " · ")
            binding.tvDropped.text = if (message.wasSilentlyDropped) "Silently dropped" else "Flagged (monitor mode)"
            binding.btnDelete.setOnClickListener { onDeleteClick(message) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemBlockedMessageBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<BlockedMessage>() {
            override fun areItemsTheSame(a: BlockedMessage, b: BlockedMessage) = a.id == b.id
            override fun areContentsTheSame(a: BlockedMessage, b: BlockedMessage) = a == b
        }
    }
}
