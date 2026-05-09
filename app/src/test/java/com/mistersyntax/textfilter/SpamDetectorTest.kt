package com.mistersyntax.textfilter

import com.mistersyntax.textfilter.filter.SpamDetector
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class SpamDetectorTest {

    private lateinit var detector: SpamDetector

    @Before
    fun setUp() {
        detector = SpamDetector()
    }

    // ── Real messages from the user's examples ────────────────────────────────

    @Test
    fun `trump treason message with Stop2End is spam`() {
        val body = "DESPICABLE: Trump is threatening to prosecute President Obama for TREASON. " +
                "Trump has lost his mind!! STAND WITH PRES. OBAMA: r.impnow.io/q7KxkcSI\n\nIN\nStop2End"
        val result = detector.analyze(body)
        assertTrue("Expected spam, score=${result.score}, rules=${result.matchedRules}", result.isSpam)
    }

    @Test
    fun `supreme court jim crow message with End2End is spam`() {
        val body = "\"The Supreme Court Resurrects Jim Crow.\" We discuss gerrymandering, " +
                "Trump's sycophants & more. Click to watch/listen >> https://imact.io/l/mgKIut\n\nIMCA\nEnd2End"
        val result = detector.analyze(body)
        assertTrue("Expected spam, score=${result.score}, rules=${result.matchedRules}", result.isSpam)
    }

    @Test
    fun `harry dunn big news message with Stop2End is spam`() {
        val body = "It's former Capitol Police Officer Harry Dunn. I have big news to share. " +
                "Please read >> t.harrydunnformd.com/8mA4cltg\n\nStop2End"
        val result = detector.analyze(body)
        assertTrue("Expected spam, score=${result.score}, rules=${result.matchedRules}", result.isSpam)
    }

    // ── Opt-out code variants ─────────────────────────────────────────────────

    @Test
    fun `End2End alone triggers high-confidence spam`() {
        val result = detector.analyze("Some message body. End2End")
        assertTrue(result.isSpam)
        assertTrue(result.matchedRules.contains("Political opt-out code"))
    }

    @Test
    fun `STPO2END typo variant is caught`() {
        val result = detector.analyze("Political message. STPO2END")
        assertTrue(result.isSpam)
    }

    @Test
    fun `stop2end is case-insensitive`() {
        val result = detector.analyze("Some message. STOP2END")
        assertTrue(result.isSpam)
    }

    // ── Known spam domains ────────────────────────────────────────────────────

    @Test
    fun `actblue domain alone triggers spam`() {
        val result = detector.analyze("Donate now at actblue.com/donate/xyz to support our candidate!")
        assertTrue(result.isSpam)
    }

    @Test
    fun `impnow io domain alone triggers spam`() {
        val result = detector.analyze("Click here: r.impnow.io/abc123")
        assertTrue(result.isSpam)
    }

    // ── Combined scoring ──────────────────────────────────────────────────────

    @Test
    fun `multiple medium signals combine to trigger spam`() {
        val body = "STAND WITH President Obama against Trump's outrageous attacks! " +
                "Sign now to fight back against these shocking lies."
        val result = detector.analyze(body)
        assertTrue("Expected combined score spam, score=${result.score}", result.isSpam)
        assertTrue(result.score >= SpamDetector.COMBINED_SCORE_THRESHOLD)
    }

    // ── Negative cases ────────────────────────────────────────────────────────

    @Test
    fun `plain delivery notification is not spam`() {
        val result = detector.analyze("Your package has been delivered. Tap here to confirm receipt.")
        assertFalse(result.isSpam)
        assertEquals(0f, result.score)
    }

    @Test
    fun `normal personal message is not spam`() {
        val result = detector.analyze("Hey, are you coming to dinner tonight? Let me know!")
        assertFalse(result.isSpam)
    }

    @Test
    fun `news headline mentioning trump without spam signals is not spam`() {
        val result = detector.analyze("Trump signs new trade deal with China.")
        assertFalse("Lone political figure mention should not trigger spam", result.isSpam)
    }

    @Test
    fun `bank alert is not spam`() {
        val result = detector.analyze("Your account ending in 4321 was charged \$12.50 at Starbucks.")
        assertFalse(result.isSpam)
    }

    // ── Score reporting ───────────────────────────────────────────────────────

    @Test
    fun `spam result includes matched rule names`() {
        val result = detector.analyze("Stop2End")
        assertTrue(result.matchedRules.isNotEmpty())
    }

    @Test
    fun `clean message has zero score and empty rules`() {
        val result = detector.analyze("See you tomorrow!")
        assertEquals(0f, result.score)
        assertTrue(result.matchedRules.isEmpty())
    }
}
