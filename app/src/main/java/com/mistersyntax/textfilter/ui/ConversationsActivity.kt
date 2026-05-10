package com.mistersyntax.textfilter.ui

import android.content.Intent
import android.database.ContentObserver
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Telephony
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.mistersyntax.textfilter.databinding.ActivityConversationsBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class ConversationThread(
    val address: String,
    val snippet: String,
    val date: Long,
    val unreadCount: Int,
)

class ConversationsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityConversationsBinding
    private lateinit var adapter: ConversationsAdapter

    private val smsObserver = object : ContentObserver(Handler(Looper.getMainLooper())) {
        override fun onChange(selfChange: Boolean) = loadConversations()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityConversationsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        adapter = ConversationsAdapter { thread ->
            startActivity(
                Intent(this, ConversationActivity::class.java)
                    .putExtra(ConversationActivity.EXTRA_ADDRESS, thread.address)
            )
        }

        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter

        binding.fabNewMessage.setOnClickListener {
            startActivity(Intent(this, ConversationActivity::class.java))
        }

        binding.btnBlocked.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }

        binding.btnFilters.setOnClickListener {
            startActivity(Intent(this, RulesActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        contentResolver.registerContentObserver(Telephony.Sms.CONTENT_URI, true, smsObserver)
        loadConversations()
    }

    override fun onPause() {
        super.onPause()
        contentResolver.unregisterContentObserver(smsObserver)
    }

    private fun loadConversations() {
        lifecycleScope.launch {
            val threads = withContext(Dispatchers.IO) { queryConversations() }
            adapter.submitList(threads)
            binding.tvEmpty.visibility = if (threads.isEmpty()) View.VISIBLE else View.GONE
        }
    }

    private fun queryConversations(): List<ConversationThread> {
        val latestPerAddress = mutableMapOf<String, ConversationThread>()
        val unreadCounts = mutableMapOf<String, Int>()

        contentResolver.query(
            Telephony.Sms.CONTENT_URI,
            arrayOf(Telephony.Sms.ADDRESS, Telephony.Sms.BODY, Telephony.Sms.DATE, Telephony.Sms.READ),
            null, null,
            "${Telephony.Sms.DATE} DESC",
        )?.use { cursor ->
            val addrIdx = cursor.getColumnIndex(Telephony.Sms.ADDRESS)
            val bodyIdx = cursor.getColumnIndex(Telephony.Sms.BODY)
            val dateIdx = cursor.getColumnIndex(Telephony.Sms.DATE)
            val readIdx = cursor.getColumnIndex(Telephony.Sms.READ)

            while (cursor.moveToNext()) {
                val addr = cursor.getString(addrIdx) ?: continue
                if (cursor.getInt(readIdx) == 0) {
                    unreadCounts[addr] = (unreadCounts[addr] ?: 0) + 1
                }
                if (!latestPerAddress.containsKey(addr)) {
                    latestPerAddress[addr] = ConversationThread(
                        address = addr,
                        snippet = cursor.getString(bodyIdx) ?: "",
                        date = cursor.getLong(dateIdx),
                        unreadCount = 0,
                    )
                }
            }
        }

        return latestPerAddress.values
            .map { it.copy(unreadCount = unreadCounts[it.address] ?: 0) }
            .sortedByDescending { it.date }
    }
}
