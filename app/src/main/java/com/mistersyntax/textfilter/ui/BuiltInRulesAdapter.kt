package com.mistersyntax.textfilter.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.mistersyntax.textfilter.databinding.ItemBuiltinRuleBinding
import com.mistersyntax.textfilter.filter.FilterRule

data class BuiltInRuleState(val rule: FilterRule, val enabled: Boolean)

class BuiltInRulesAdapter(
    private val onToggle: (FilterRule, Boolean) -> Unit,
) : ListAdapter<BuiltInRuleState, BuiltInRulesAdapter.ViewHolder>(DIFF) {

    inner class ViewHolder(private val binding: ItemBuiltinRuleBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: BuiltInRuleState) {
            binding.tvRuleName.text = item.rule.name
            binding.tvConfidence.text = "Confidence: %.0f%%".format(item.rule.confidence * 100)
            // Set without triggering the listener
            binding.switchEnabled.setOnCheckedChangeListener(null)
            binding.switchEnabled.isChecked = item.enabled
            binding.switchEnabled.setOnCheckedChangeListener { _, checked ->
                onToggle(item.rule, checked)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemBuiltinRuleBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(getItem(position))

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<BuiltInRuleState>() {
            override fun areItemsTheSame(a: BuiltInRuleState, b: BuiltInRuleState) =
                a.rule.name == b.rule.name
            override fun areContentsTheSame(a: BuiltInRuleState, b: BuiltInRuleState) = a == b
        }
    }
}
