package com.mistersyntax.textfilter.service

import android.app.Service
import android.content.Intent
import android.os.IBinder

/**
 * Satisfies the Android requirement for a default SMS app to declare a service
 * with the RESPOND_VIA_MESSAGE action. This app does not send messages, so the
 * implementation is intentionally a no-op stub.
 */
class HeadlessSmsService : Service() {
    override fun onBind(intent: Intent?): IBinder? = null
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int = START_NOT_STICKY
}
