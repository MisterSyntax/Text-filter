package com.mistersyntax.textfilter.ui

import android.content.ContentValues
import android.database.ContentObserver
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Telephony
import android.telephony.SmsManager
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.mistersyntax.textfilter.databinding.ActivityConversationBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class Message(val id: Long, val body: String, val date: Long, val type: Int)

class ConversationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityConversationBinding
    private lateinit var adapter: MessagesAdapter
    private var address: String? = null

    private val smsObserver = object : ContentObserver(Handler(Looper.getMainLooper())) {
        override fun onChange(selfChange: Boolean) = loadMessages()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityConversationBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        address = intent.getStringExtra(EXTRA_ADDRESS)
        title = address ?: "New Message"

        binding.recipientRow.visibility = if (address == null) View.VISIBLE else View.GONE

        adapter = MessagesAdapter()
        binding.recyclerView.layoutManager = LinearLayoutManager(this).also {
            it.stackFromEnd = true
        }
        binding.recyclerView.adapter = adapter

        binding.btnSend.setOnClickListener { sendMessage() }
    }

    override fun onResume() {
        super.onResume()
        if (address != null) {
            contentResolver.registerContentObserver(Telephony.Sms.CONTENT_URI, true, smsObserver)
            markAsRead()
            loadMessages()
        }
    }

    override fun onPause() {
        super.onPause()
        contentResolver.unregisterContentObserver(smsObserver)
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    private fun sendMessage() {
        val to = address ?: binding.etRecipient.text.toString().trim()
        val text = binding.etMessage.text.toString().trim()
        if (to.isBlank() || text.isBlank()) return

        try {
            @Suppress("DEPRECATION")
            val smsManager = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                getSystemService(SmsManager::class.java)
            } else {
                SmsManager.getDefault()
            }
            smsManager.sendTextMessage(to, null, text, null, null)

            ContentValues().also { cv ->
                cv.put(Telephony.Sms.ADDRESS, to)
                cv.put(Telephony.Sms.BODY, text)
                cv.put(Telephony.Sms.DATE, System.currentTimeMillis())
                cv.put(Telephony.Sms.READ, 1)
                cv.put(Telephony.Sms.TYPE, Telephony.Sms.MESSAGE_TYPE_SENT)
                contentResolver.insert(Telephony.Sms.CONTENT_URI, cv)
            }

            binding.etMessage.text?.clear()

            if (address == null) {
                address = to
                title = to
                binding.recipientRow.visibility = View.GONE
                contentResolver.registerContentObserver(Telephony.Sms.CONTENT_URI, true, smsObserver)
                loadMessages()
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Failed to send: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadMessages() {
        val addr = address ?: return
        lifecycleScope.launch {
            val messages = withContext(Dispatchers.IO) { queryMessages(addr) }
            adapter.submitList(messages)
            if (messages.isNotEmpty()) {
                binding.recyclerView.scrollToPosition(messages.size - 1)
            }
        }
    }

    private fun queryMessages(addr: String): List<Message> {
        val messages = mutableListOf<Message>()
        contentResolver.query(
            Telephony.Sms.CONTENT_URI,
            arrayOf(Telephony.Sms._ID, Telephony.Sms.BODY, Telephony.Sms.DATE, Telephony.Sms.TYPE),
            "${Telephony.Sms.ADDRESS} = ?",
            arrayOf(addr),
            "${Telephony.Sms.DATE} ASC",
        )?.use { cursor ->
            val idIdx = cursor.getColumnIndex(Telephony.Sms._ID)
            val bodyIdx = cursor.getColumnIndex(Telephony.Sms.BODY)
            val dateIdx = cursor.getColumnIndex(Telephony.Sms.DATE)
            val typeIdx = cursor.getColumnIndex(Telephony.Sms.TYPE)
            while (cursor.moveToNext()) {
                messages.add(
                    Message(
                        id = cursor.getLong(idIdx),
                        body = cursor.getString(bodyIdx) ?: "",
                        date = cursor.getLong(dateIdx),
                        type = cursor.getInt(typeIdx),
                    )
                )
            }
        }
        return messages
    }

    private fun markAsRead() {
        val addr = address ?: return
        lifecycleScope.launch(Dispatchers.IO) {
            val cv = ContentValues().apply { put(Telephony.Sms.READ, 1) }
            contentResolver.update(
                Telephony.Sms.CONTENT_URI, cv,
                "${Telephony.Sms.ADDRESS} = ? AND ${Telephony.Sms.READ} = 0",
                arrayOf(addr),
            )
        }
    }

    companion object {
        const val EXTRA_ADDRESS = "extra_address"
    }
}
