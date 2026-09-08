package com.aware.app.data

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.aware.app.AwareApplication
import java.util.concurrent.TimeUnit

class RecurringWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result = runCatching {
        (applicationContext as AwareApplication).container.repository.materializeDueRecurring()
        Result.success()
    }.getOrElse { Result.retry() }

    companion object {
        fun schedule(context: Context) {
            val request = PeriodicWorkRequestBuilder<RecurringWorker>(24, TimeUnit.HOURS).build()
            WorkManager.getInstance(context).enqueueUniquePeriodicWork("aware-recurring", ExistingPeriodicWorkPolicy.UPDATE, request)
        }
    }
}
