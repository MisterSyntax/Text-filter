package com.mistersyntax.textfilter.filter

import android.content.Context
import androidx.core.content.edit
import com.mistersyntax.textfilter.db.SpamDatabase

/**
 * Single source of truth for which rules are active.
 * Built-in rule on/off state lives in SharedPreferences; user keywords come from Room.
 */
class RuleRepository(context: Context) {

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val db = SpamDatabase.get(context)

    /** Whether filtering should be skipped for numbers that appear in the user's contacts. */
    val onlyUnknownSenders: Boolean
        get() = prefs.getBoolean(KEY_ONLY_UNKNOWN, true)

    fun setOnlyUnknownSenders(value: Boolean) {
        prefs.edit { putBoolean(KEY_ONLY_UNKNOWN, value) }
    }

    fun isBuiltInRuleEnabled(rule: FilterRule): Boolean =
        prefs.getBoolean(builtInKey(rule), true)

    fun setBuiltInRuleEnabled(rule: FilterRule, enabled: Boolean) {
        prefs.edit { putBoolean(builtInKey(rule), enabled) }
    }

    /** Returns the full active rule list to pass to [SpamDetector]. Suspending — call from IO. */
    suspend fun getActiveRules(): List<FilterRule> {
        val builtIns = BuiltInRules.all.filter { isBuiltInRuleEnabled(it) }

        val userRules = db.userKeywordDao().getAllEnabled().map { kw ->
            // User keywords always trigger spam on their own (confidence = 1.0)
            FilterRule.KeywordRule(
                name = "\"${kw.keyword}\"",
                keyword = kw.keyword,
                confidence = 1.0f,
            )
        }

        return builtIns + userRules
    }

    private fun builtInKey(rule: FilterRule) = "builtin_${rule.name}"

    companion object {
        private const val PREFS_NAME = "filter_prefs"
        private const val KEY_ONLY_UNKNOWN = "only_unknown_senders"
    }
}
