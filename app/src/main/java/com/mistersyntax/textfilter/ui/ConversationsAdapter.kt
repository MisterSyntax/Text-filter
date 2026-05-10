package com.mistersyntax.textfilter.ui

import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.mistersyntax.textfilter.databinding.ItemConversationBinding

class ConversationsAdapter(
    private val onClick: (ConversationThread) -> Unit,
) : ListAdapter<ConversationThread, ConversationsAdapter.ViewHolder>(DIFF) {

    inner class ViewHolder(private val binding: ItemConversationBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(thread: ConversationThread) {
            val bold = thread.unreadCount > 0
            binding.tvAddress.text = thread.address
            binding.tvAddress.setTypeface(null, if (bold) Typeface.BOLD else Typeface.NORMAL)
            binding.tvSnippet.text = thread.snippet
            binding.tvSnippet.setTypeface(null, if (bold) Typeface.BOLD else Typeface.NORMAL)
            binding.tvDate.text = thread.date.toDisplayDate()
            binding.tvUnread.visibility = if (bold) View.VISIBLE else View.GONE
            binding.tvUnread.text = thread.unreadCount.toString()
            binding.root.setOnClickListener { onClick(thread) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemConversationBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(getItem(position))

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<ConversationThread>() {
            override fun areItemsTheSame(a: ConversationThread, b: ConversationThread) =
                a.address == b.address
            override fun areContentsTheSame(a: ConversationThread, b: ConversationThread) = a == b
        }
    }
}
