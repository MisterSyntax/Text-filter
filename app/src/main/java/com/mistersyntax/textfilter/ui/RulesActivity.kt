package com.mistersyntax.textfilter.ui

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.mistersyntax.textfilter.TextFilterApp
import com.mistersyntax.textfilter.databinding.ActivityRulesBinding
import com.mistersyntax.textfilter.db.UserKeyword
import com.mistersyntax.textfilter.filter.BuiltInRules
import com.mistersyntax.textfilter.filter.RuleRepository
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class RulesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRulesBinding
    private lateinit var repo: RuleRepository
    private lateinit var builtInAdapter: BuiltInRulesAdapter
    private lateinit var userKeywordsAdapter: UserKeywordsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRulesBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        title = "Manage Filters"

        repo = RuleRepository(this)

        setupUnknownSendersToggle()
        setupBuiltInRules()
        setupUserKeywords()
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    private fun setupUnknownSendersToggle() {
        binding.switchUnknownOnly.isChecked = repo.onlyUnknownSenders
        binding.switchUnknownOnly.setOnCheckedChangeListener { _, checked ->
            repo.setOnlyUnknownSenders(checked)
        }
    }

    private fun setupBuiltInRules() {
        builtInAdapter = BuiltInRulesAdapter { rule, enabled ->
            repo.setBuiltInRuleEnabled(rule, enabled)
            // Refresh list to reflect change
            builtInAdapter.submitList(currentBuiltInStates())
        }

        binding.rvBuiltinRules.apply {
            layoutManager = LinearLayoutManager(this@RulesActivity)
            adapter = builtInAdapter
            isNestedScrollingEnabled = false
        }

        builtInAdapter.submitList(currentBuiltInStates())
    }

    private fun currentBuiltInStates() = BuiltInRules.all.map { rule ->
        BuiltInRuleState(rule, repo.isBuiltInRuleEnabled(rule))
    }

    private fun setupUserKeywords() {
        userKeywordsAdapter = UserKeywordsAdapter(
            onToggle = { keyword, enabled ->
                lifecycleScope.launch {
                    (application as TextFilterApp).database.userKeywordDao()
                        .update(keyword.copy(enabled = enabled))
                }
            },
            onDelete = { keyword ->
                lifecycleScope.launch {
                    (application as TextFilterApp).database.userKeywordDao().delete(keyword.id)
                }
            },
        )

        binding.rvUserKeywords.apply {
            layoutManager = LinearLayoutManager(this@RulesActivity)
            adapter = userKeywordsAdapter
            isNestedScrollingEnabled = false
        }

        binding.btnAddKeyword.setOnClickListener {
            val text = binding.etNewKeyword.text.toString().trim()
            if (text.isBlank()) {
                Toast.makeText(this, "Enter a keyword first", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            lifecycleScope.launch {
                (application as TextFilterApp).database.userKeywordDao()
                    .insert(UserKeyword(keyword = text))
                binding.etNewKeyword.text?.clear()
            }
        }

        lifecycleScope.launch {
            (application as TextFilterApp).database.userKeywordDao()
                .observeAll()
                .collectLatest { keywords ->
                    userKeywordsAdapter.submitList(keywords)
                    binding.tvEmptyKeywords.visibility =
                        if (keywords.isEmpty()) android.view.View.VISIBLE else android.view.View.GONE
                }
        }
    }
}
