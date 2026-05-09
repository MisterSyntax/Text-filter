package com.mistersyntax.textfilter.filter

sealed class FilterRule(
    val name: String,
    val confidence: Float,
) {
    /** Triggers when the message body contains [keyword] (case-insensitive). */
    class KeywordRule(
        name: String,
        val keyword: String,
        confidence: Float,
    ) : FilterRule(name, confidence)

    /** Triggers when [pattern] finds a match anywhere in the message body. */
    class RegexRule(
        name: String,
        val pattern: Regex,
        confidence: Float,
    ) : FilterRule(name, confidence)

    /**
     * Triggers when BOTH [primary] and [secondary] match the message body.
     * Combined confidence is the product of the two rules' confidence values.
     */
    class CompositeRule(
        name: String,
        val primary: FilterRule,
        val secondary: FilterRule,
        confidence: Float,
    ) : FilterRule(name, confidence)
}
