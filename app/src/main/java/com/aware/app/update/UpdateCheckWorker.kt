package com.aware.app.update

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.aware.app.MainActivity
import com.aware.app.R
import java.util.concurrent.TimeUnit

class UpdateCheckWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result = runCatching {
        val result = AppUpdater.check()
        if (result is UpdateCheckResult.Available && canNotify()) notify(result.release)
        Result.success()
    }.getOrElse { Result.retry() }

    private fun canNotify() = Build.VERSION.SDK_INT < 33 ||
        ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED

    private fun notify(release: AppRelease) {
        val manager = applicationContext.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(NotificationChannel(CHANNEL, "App updates", NotificationManager.IMPORTANCE_DEFAULT))
        val intent = Intent(applicationContext, MainActivity::class.java).putExtra(EXTRA_OPEN_UPDATE, true)
        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            7301,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        manager.notify(
            NOTIFICATION_ID,
            NotificationCompat.Builder(applicationContext, CHANNEL)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("aware ${release.version} is ready")
                .setContentText("Tap to review and install the latest update.")
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .build(),
        )
    }

    companion object {
        const val EXTRA_OPEN_UPDATE = "openUpdate"
        private const val CHANNEL = "app_updates"
        private const val NOTIFICATION_ID = 7301

        fun schedule(context: Context) {
            val request = PeriodicWorkRequestBuilder<UpdateCheckWorker>(7, TimeUnit.DAYS)
                .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
                .build()
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                "aware-weekly-update-check",
                ExistingPeriodicWorkPolicy.UPDATE,
                request,
            )
        }
    }
}
