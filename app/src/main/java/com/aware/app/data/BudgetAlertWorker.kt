package com.aware.app.data

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.aware.app.AwareApplication
import com.aware.app.R
import java.text.NumberFormat
import java.util.Locale
import java.util.concurrent.TimeUnit

class BudgetAlertWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result = runCatching {
        if (Build.VERSION.SDK_INT >= 33 && ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) return Result.success()
        val manager = applicationContext.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(NotificationChannel(CHANNEL, "Budget nudges", NotificationManager.IMPORTANCE_DEFAULT))
        val repository = (applicationContext as AwareApplication).container.repository
        repository.newBudgetAlerts().forEach { alert ->
            val body = if (alert.percent >= 100) "You crossed ${alert.name}: ${money(alert.spentPaise)} of ${money(alert.capPaise)}." else "${alert.name} is ${alert.percent}% used. You still have ${money((alert.capPaise - alert.spentPaise).coerceAtLeast(0))}."
            manager.notify((alert.budgetId * 10 + alert.percent).toInt(), NotificationCompat.Builder(applicationContext, CHANNEL)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(if (alert.percent >= 100) "Budget crossed — breathe, then adjust" else "A gentle money nudge")
                .setContentText(body)
                .setStyle(NotificationCompat.BigTextStyle().bigText(body))
                .setAutoCancel(true)
                .build())
        }
        Result.success()
    }.getOrElse { Result.retry() }

    companion object {
        private const val CHANNEL = "budget_nudges"
        fun schedule(context: Context) {
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                "aware-budget-alerts",
                ExistingPeriodicWorkPolicy.UPDATE,
                PeriodicWorkRequestBuilder<BudgetAlertWorker>(6, TimeUnit.HOURS).build(),
            )
        }
        private fun money(paise: Long) = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("en-IN")).format(paise / 100.0)
    }
}
