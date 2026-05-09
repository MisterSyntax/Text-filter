package com.mistersyntax.textfilter.filter

/**
 * Pre-built rules targeting political spam texts. Common pattern: rotating sender numbers
 * with consistent opt-out codes (Stop2End, End2End) and political call-to-action language.
 * Rules are scored and summed; see SpamDetector for threshold logic.
 */
object BuiltInRules {

    // --- High-confidence rules (≥0.90) — match alone is enough to flag spam ---

    val optOutCodes = FilterRule.RegexRule(
        name = "Political opt-out code",
        // Covers Stop2End, End2End, and common typos/variants seen in the wild
        pattern = Regex("""(?i)\b(stop2end|end2end|stpo2end|stop2vote|end2vote|txt2stop)\b"""),
        confidence = 0.95f,
    )

    val knownSpamDomains = FilterRule.RegexRule(
        name = "Known political spam domain",
        // Shortlink domains used exclusively by political mass-texting campaigns
        pattern = Regex("""(?i)(impnow\.io|imact\.io|actblue\.com|winred\.com|gop\.com|dccc\.org|dlcc\.org|move-on\.org|moveon\.org)"""),
        confidence = 0.90f,
    )

    // --- Medium-confidence rules — contribute to combined score ---

    val politicalCta = FilterRule.RegexRule(
        name = "Political call-to-action",
        pattern = Regex("""(?i)\b(stand with|fight back|donate now|act now|sign now|click to watch|click to listen|big news to share|i have big news)\b"""),
        confidence = 0.65f,
    )

    val politicalFigures = FilterRule.RegexRule(
        name = "Political figures",
        pattern = Regex("""(?i)\b(trump|obama|biden|harris|maga|democrat|republican|capitol police|supreme court|gerrymandering|jim crow|sycophant|treason)\b"""),
        confidence = 0.50f,
    )

    val urgentLanguage = FilterRule.RegexRule(
        name = "Urgent solicitation language",
        pattern = Regex("""(?i)\b(despicable|outrageous|shocking|breaking|urgent|emergency|lost his mind|threatens to|threatens?)\b"""),
        confidence = 0.45f,
    )

    val shortlinkPattern = FilterRule.RegexRule(
        name = "Generic political shortlink",
        // Matches t.<domain>/<path> and r.<domain>/<path> patterns used by campaign texters
        pattern = Regex("""(?i)\b[rt]\.[a-z0-9-]{3,30}\.[a-z]{2,6}/[a-zA-Z0-9]{4,}"""),
        confidence = 0.55f,
    )

    // --- Composite rule: political figure + action = stronger signal ---

    val politicalFigureWithCta = FilterRule.CompositeRule(
        name = "Political figure + call-to-action",
        primary = politicalFigures,
        secondary = politicalCta,
        confidence = 0.75f,
    )

    val all: List<FilterRule> = listOf(
        optOutCodes,
        knownSpamDomains,
        politicalCta,
        politicalFigures,
        urgentLanguage,
        shortlinkPattern,
        politicalFigureWithCta,
    )
}
