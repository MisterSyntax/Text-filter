package com.mistersyntax.textfilter.ui

import android.Manifest
import android.app.role.RoleManager
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.Telephony
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.mistersyntax.textfilter.TextFilterApp
import com.mistersyntax.textfilter.databinding.ActivityMainBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: BlockedMessagesAdapter

    private val requestPermissions = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { grants ->
        if (grants.values.all { it }) {
            updateDefaultAppBanner()
        } else {
            Toast.makeText(this, "SMS permission is required to detect spam", Toast.LENGTH_LONG).show()
        }
    }

    private val requestDefaultSmsApp = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        updateDefaultAppBanner()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        adapter = BlockedMessagesAdapter(
            onDeleteClick = { message ->
                lifecycleScope.launch {
                    (application as TextFilterApp).database.blockedMessageDao().delete(message.id)
                }
            }
        )

        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter

        binding.btnMakeDefault.setOnClickListener { requestDefaultSmsAppRole() }
        binding.btnClearAll.setOnClickListener {
            lifecycleScope.launch {
                (application as TextFilterApp).database.blockedMessageDao().deleteAll()
            }
        }

        observeBlockedMessages()
        checkPermissions()
    }

    override fun onResume() {
        super.onResume()
        updateDefaultAppBanner()
    }

    private fun checkPermissions() {
        val needed = listOf(Manifest.permission.RECEIVE_SMS, Manifest.permission.READ_SMS)
            .filter { ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED }
        if (needed.isNotEmpty()) {
            requestPermissions.launch(needed.toTypedArray())
        }
    }

    private fun updateDefaultAppBanner() {
        val isDefault = Telephony.Sms.getDefaultSmsPackage(this) == packageName
        binding.bannerMonitorMode.visibility = if (isDefault) View.GONE else View.VISIBLE
        binding.bannerDefaultMode.visibility = if (isDefault) View.VISIBLE else View.GONE
    }

    private fun requestDefaultSmsAppRole() {
        val roleManager = getSystemService(RoleManager::class.java)
        if (roleManager.isRoleAvailable(RoleManager.ROLE_SMS) &&
            !roleManager.isRoleHeld(RoleManager.ROLE_SMS)
        ) {
            val intent = roleManager.createRequestRoleIntent(RoleManager.ROLE_SMS)
            requestDefaultSmsApp.launch(intent)
        }
    }

    private fun observeBlockedMessages() {
        val dao = (application as TextFilterApp).database.blockedMessageDao()

        lifecycleScope.launch {
            dao.observeCount().collectLatest { count ->
                binding.tvBlockedCount.text = "$count texts blocked"
            }
        }

        lifecycleScope.launch {
            dao.observeAll().collectLatest { messages ->
                adapter.submitList(messages)
                binding.tvEmpty.visibility = if (messages.isEmpty()) View.VISIBLE else View.GONE
            }
        }
    }
}

private val dateFormat = SimpleDateFormat("MMM d, h:mm a", Locale.getDefault())
internal fun Long.toDisplayDate(): String = dateFormat.format(Date(this))
