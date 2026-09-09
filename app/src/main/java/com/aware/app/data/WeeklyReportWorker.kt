package com.aware.app.data

import android.content.Context
import androidx.glance.appwidget.updateAll
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.aware.app.AwareApplication
import com.aware.app.widget.AwareWidget
import java.time.DayOfWeek
import java.time.Duration
import java.time.ZonedDateTime
import java.time.temporal.TemporalAdjusters
import java.util.concurrent.TimeUnit

class WeeklyReportWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result = runCatching {
        val repository = (applicationContext as AwareApplication).container.repository
        repository.generateLatestWeeklyReportIfMissing()
        AwareWidget().updateAll(applicationContext)
        Result.success()
    }.getOrElse { Result.retry() }

    companion object {
        fun schedule(context: Context) {
            val now = ZonedDateTime.now()
            var nextMonday = now.with(TemporalAdjusters.nextOrSame(DayOfWeek.MONDAY))
                .withHour(8).withMinute(0).withSecond(0).withNano(0)
            if (!nextMonday.isAfter(now)) nextMonday = nextMonday.plusWeeks(1)
            val delayMinutes = Duration.between(now, nextMonday).toMinutes().coerceAtLeast(1)
            val request = PeriodicWorkRequestBuilder<WeeklyReportWorker>(7, TimeUnit.DAYS)
                .setInitialDelay(delayMinutes, TimeUnit.MINUTES)
                .build()
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                "aware-weekly-report",
                ExistingPeriodicWorkPolicy.UPDATE,
                request,
            )
        }
    }
}
