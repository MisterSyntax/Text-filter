package com.mistersyntax.textfilter.receiver

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import androidx.core.app.NotificationCompat
import com.mistersyntax.textfilter.R
import com.mistersyntax.textfilter.db.BlockedMessage
import com.mistersyntax.textfilter.db.SpamDatabase
import com.mistersyntax.textfilter.filter.SpamDetector
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SmsReceiver : BroadcastReceiver() {

    private val detector = SpamDetector()

    override fun onReceive(context: Context, intent: Intent) {
        val isDefaultAppDelivery = intent.action == Telephony.Sms.Intents.SMS_DELIVER_ACTION
        val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent) ?: return

        val sender = messages.firstOrNull()?.originatingAddress ?: "Unknown"
        val body = messages.joinToString("") { it.messageBody ?: "" }

        if (body.isBlank()) return

        val result = detector.analyze(body)
        if (!result.isSpam) return

        // Consume the broadcast so the system SMS inbox never sees this message.
        // abortBroadcast() is only effective when we are the default SMS app receiving
        // SMS_DELIVER (ordered broadcast). For SMS_RECEIVED (monitor mode) it still
        // suppresses delivery to lower-priority receivers but does NOT prevent the
        // system from writing the message to the SMS content provider.
        abortBroadcast()

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                SpamDatabase.get(context).blockedMessageDao().insert(
                    BlockedMessage(
                        sender = sender,
                        body = body,
                        score = result.score,
                        matchedRules = result.matchedRules.joinToString(","),
                        wasSilentlyDropped = isDefaultAppDelivery,
                    )
                )
                showNotification(context, sender, result.matchedRules.first())
            } finally {
                pendingResult.finish()
            }
        }
    }

    private fun showNotification(context: Context, sender: String, topRule: String) {
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val channel = NotificationChannel(
            CHANNEL_ID,
            "Spam blocked",
            NotificationManager.IMPORTANCE_LOW,
        ).apply { description = "Silent alerts when political spam texts are blocked" }
        nm.createNotificationChannel(channel)

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_block)
            .setContentTitle("Spam text blocked")
            .setContentText("From $sender — matched: $topRule")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setAutoCancel(true)
            .build()

        nm.notify(System.currentTimeMillis().toInt(), notification)
    }

    companion object {
        private const val CHANNEL_ID = "spam_blocked"
    }
}
