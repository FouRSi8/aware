package com.aware.app.sms

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import com.aware.app.AwareApplication
import com.aware.app.widget.AwareWidget
import androidx.glance.appwidget.updateAll
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class TransactionSmsReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) return
        val pendingResult = goAsync()
        val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
        val body = messages.joinToString("") { it.messageBody.orEmpty() }
        val sender = messages.firstOrNull()?.displayOriginatingAddress.orEmpty().ifBlank { "Bank" }
        val receivedAt = messages.firstOrNull()?.timestampMillis ?: System.currentTimeMillis()
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                val parsed = TransactionSmsParser().parse(sender, body, receivedAt) ?: return@launch
                val app = context.applicationContext as AwareApplication
                if (app.container.repository.saveCapture(parsed) > 0) AwareWidget().updateAll(context)
            } finally {
                pendingResult.finish()
            }
        }
    }
}
