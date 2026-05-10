package com.mistersyntax.textfilter.ui

import android.provider.Telephony
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.mistersyntax.textfilter.databinding.ItemMessageReceivedBinding
import com.mistersyntax.textfilter.databinding.ItemMessageSentBinding

class MessagesAdapter : ListAdapter<Message, RecyclerView.ViewHolder>(DIFF) {

    override fun getItemViewType(position: Int) =
        if (getItem(position).type == Telephony.Sms.MESSAGE_TYPE_SENT) TYPE_SENT else TYPE_RECEIVED

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if (viewType == TYPE_SENT) {
            SentHolder(ItemMessageSentBinding.inflate(inflater, parent, false))
        } else {
            ReceivedHolder(ItemMessageReceivedBinding.inflate(inflater, parent, false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val msg = getItem(position)
        when (holder) {
            is SentHolder -> holder.bind(msg)
            is ReceivedHolder -> holder.bind(msg)
        }
    }

    inner class SentHolder(private val b: ItemMessageSentBinding) :
        RecyclerView.ViewHolder(b.root) {
        fun bind(msg: Message) {
            b.tvBody.text = msg.body
            b.tvDate.text = msg.date.toDisplayDate()
        }
    }

    inner class ReceivedHolder(private val b: ItemMessageReceivedBinding) :
        RecyclerView.ViewHolder(b.root) {
        fun bind(msg: Message) {
            b.tvBody.text = msg.body
            b.tvDate.text = msg.date.toDisplayDate()
        }
    }

    companion object {
        private const val TYPE_SENT = 1
        private const val TYPE_RECEIVED = 0

        private val DIFF = object : DiffUtil.ItemCallback<Message>() {
            override fun areItemsTheSame(a: Message, b: Message) = a.id == b.id
            override fun areContentsTheSame(a: Message, b: Message) = a == b
        }
    }
}
