package com.mistersyntax.textfilter.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.mistersyntax.textfilter.databinding.ItemUserKeywordBinding
import com.mistersyntax.textfilter.db.UserKeyword

class UserKeywordsAdapter(
    private val onToggle: (UserKeyword, Boolean) -> Unit,
    private val onDelete: (UserKeyword) -> Unit,
) : ListAdapter<UserKeyword, UserKeywordsAdapter.ViewHolder>(DIFF) {

    inner class ViewHolder(private val binding: ItemUserKeywordBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: UserKeyword) {
            binding.tvKeyword.text = item.keyword
            binding.switchEnabled.setOnCheckedChangeListener(null)
            binding.switchEnabled.isChecked = item.enabled
            binding.switchEnabled.setOnCheckedChangeListener { _, checked ->
                onToggle(item, checked)
            }
            binding.btnDelete.setOnClickListener { onDelete(item) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemUserKeywordBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(getItem(position))

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<UserKeyword>() {
            override fun areItemsTheSame(a: UserKeyword, b: UserKeyword) = a.id == b.id
            override fun areContentsTheSame(a: UserKeyword, b: UserKeyword) = a == b
        }
    }
}
