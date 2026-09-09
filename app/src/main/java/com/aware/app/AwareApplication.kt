package com.aware.app

import android.app.Application
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.aware.app.data.AppDatabase
import com.aware.app.data.AwareRepository
import com.aware.app.security.SecureStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import net.zetetic.database.sqlcipher.SupportOpenHelperFactory
import com.aware.app.data.RecurringWorker
import com.aware.app.ai.GroqCategorySuggester
import com.aware.app.data.BudgetAlertWorker
import com.aware.app.data.WeeklyReportWorker
import androidx.glance.appwidget.updateAll
import com.aware.app.widget.AwareWidget
import com.aware.app.update.UpdateCheckWorker

class AwareApplication : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        System.loadLibrary("sqlcipher")
        val secureStore = SecureStore(this)
        val factory = SupportOpenHelperFactory(secureStore.databasePassphrase(), null, true)
        val database = Room.databaseBuilder(this, AppDatabase::class.java, "aware.db")
            .openHelperFactory(factory)
            .addMigrations(MIGRATION_1_2)
            .addMigrations(MIGRATION_2_3)
            .fallbackToDestructiveMigrationOnDowngrade()
            .build()
        container = AppContainer(database, secureStore)
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            container.repository.ensureStarterStructure()
            container.repository.cleanupExpiredRawBodies()
            container.repository.materializeDueRecurring()
            container.repository.generateLatestWeeklyReportIfMissing()
            AwareWidget().updateAll(this@AwareApplication)
        }
        RecurringWorker.schedule(this)
        BudgetAlertWorker.schedule(this)
        WeeklyReportWorker.schedule(this)
        UpdateCheckWorker.schedule(this)
    }
}

private val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE capture_candidates ADD COLUMN source TEXT NOT NULL DEFAULT 'SMS'")
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS weekly_reports (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, weekStart INTEGER NOT NULL, weekEndExclusive INTEGER NOT NULL, generatedAt INTEGER NOT NULL, incomePaise INTEGER NOT NULL, refundPaise INTEGER NOT NULL, spendingPaise INTEGER NOT NULL, transferPaise INTEGER NOT NULL, topMerchant TEXT)",
        )
        db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_weekly_reports_weekStart ON weekly_reports (weekStart)")
    }
}

private val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE budget_buckets ADD COLUMN scope TEXT NOT NULL DEFAULT 'CATEGORY'")
        db.execSQL("ALTER TABLE budget_buckets ADD COLUMN accountId INTEGER")
        db.execSQL("ALTER TABLE budget_buckets ADD COLUMN payee TEXT")
        db.execSQL("ALTER TABLE budget_buckets ADD COLUMN period TEXT NOT NULL DEFAULT 'MONTHLY'")
        db.execSQL("ALTER TABLE budget_buckets ADD COLUMN startAt INTEGER NOT NULL DEFAULT 0")
        db.execSQL("ALTER TABLE budget_buckets ADD COLUMN endAt INTEGER")
        db.execSQL("ALTER TABLE budget_buckets ADD COLUMN isRecurring INTEGER NOT NULL DEFAULT 1")
        db.execSQL("ALTER TABLE recurring_rules ADD COLUMN customIntervalDays INTEGER NOT NULL DEFAULT 1")
        db.execSQL("ALTER TABLE recurring_rules ADD COLUMN reminderMinutesBefore INTEGER NOT NULL DEFAULT 0")
    }
}

class AppContainer(database: AppDatabase, val secureStore: SecureStore) {
    val repository = AwareRepository(database, secureStore)
    val categorySuggester = GroqCategorySuggester(secureStore)
}
