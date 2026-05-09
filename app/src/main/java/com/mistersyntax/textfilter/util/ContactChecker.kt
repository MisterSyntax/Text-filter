package com.mistersyntax.textfilter.util

import android.content.Context
import android.content.pm.PackageManager
import android.provider.ContactsContract
import androidx.core.content.ContextCompat

object ContactChecker {

    /** Returns true if [phoneNumber] matches any entry in the device contacts. */
    fun isKnownContact(context: Context, phoneNumber: String): Boolean {
        if (ContextCompat.checkSelfPermission(
                context, android.Manifest.permission.READ_CONTACTS
            ) != PackageManager.PERMISSION_GRANTED
        ) return false  // no permission → can't confirm, treat as unknown

        val uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI
        val projection = arrayOf(ContactsContract.CommonDataKinds.Phone.NUMBER)
        val normalised = normalise(phoneNumber)

        context.contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
            val col = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
            while (cursor.moveToNext()) {
                val contactNumber = cursor.getString(col) ?: continue
                if (normalise(contactNumber) == normalised) return true
            }
        }
        return false
    }

    // Strip everything except digits so +1 (555) 123-4567 == 15551234567
    private fun normalise(number: String) = number.filter { it.isDigit() }
}
