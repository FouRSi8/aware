package com.aware.app.notifications

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import androidx.glance.appwidget.updateAll
import com.aware.app.AwareApplication
import com.aware.app.data.TransactionSource
import com.aware.app.widget.AwareWidget
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class PaymentNotificationListener : NotificationListenerService() {
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val parser = PaymentNotificationParser()

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        if (sbn.packageName !in PaymentNotificationParser.PROVIDERS) return
        val extras = sbn.notification.extras
        val parts = buildList<CharSequence?> {
            add(extras.getCharSequence(Notification.EXTRA_TITLE))
            add(extras.getCharSequence(Notification.EXTRA_TEXT))
            add(extras.getCharSequence(Notification.EXTRA_BIG_TEXT))
            add(extras.getCharSequence(Notification.EXTRA_SUB_TEXT))
            extras.getCharSequenceArray(Notification.EXTRA_TEXT_LINES)?.forEach(::add)
        }
        val parsed = parser.parse(sbn.packageName, sbn.key, parts, sbn.postTime) ?: return
        serviceScope.launch {
            val repository = (application as AwareApplication).container.repository
            if (repository.saveCapture(parsed, TransactionSource.NOTIFICATION) > 0) {
                AwareWidget().updateAll(applicationContext)
            }
        }
    }

    override fun onDestroy() {
        serviceScope.cancel()
        super.onDestroy()
    }
}
