package com.mistersyntax.textfilter.filter

data class DetectionResult(
    val isSpam: Boolean,
    val score: Float,
    val matchedRules: List<String>,
)

class SpamDetector(private val rules: List<FilterRule> = BuiltInRules.all) {

    companion object {
        /** A single rule at or above this confidence marks the message as spam on its own. */
        const val HIGH_CONFIDENCE_THRESHOLD = 0.90f

        /** Combined score at or above this threshold marks the message as spam. */
        const val COMBINED_SCORE_THRESHOLD = 1.20f
    }

    fun analyze(body: String): DetectionResult {
        val matched = mutableListOf<String>()
        var totalScore = 0f

        for (rule in rules) {
            if (matches(rule, body)) {
                matched.add(rule.name)
                totalScore += rule.confidence
            }
        }

        val isSpam = matched.any { name ->
            rules.first { it.name == name }.confidence >= HIGH_CONFIDENCE_THRESHOLD
        } || totalScore >= COMBINED_SCORE_THRESHOLD

        return DetectionResult(isSpam, totalScore, matched)
    }

    private fun matches(rule: FilterRule, body: String): Boolean = when (rule) {
        is FilterRule.KeywordRule -> body.contains(rule.keyword, ignoreCase = true)
        is FilterRule.RegexRule -> rule.pattern.containsMatchIn(body)
        is FilterRule.CompositeRule -> matches(rule.primary, body) && matches(rule.secondary, body)
    }
}
