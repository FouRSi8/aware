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
import com.aware.app.update.UpdateCheckWorker
import com.aware.app.widget.AwareWidgets
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.collectLatest

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
            .addMigrations(MIGRATION_3_4)
            .addMigrations(MIGRATION_4_5)
            .addMigrations(MIGRATION_5_6)
            .fallbackToDestructiveMigrationOnDowngrade()
            .build()
        container = AppContainer(database, secureStore)
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            container.repository.ensureStarterStructure()
            container.repository.materializeDueRecurring()
        }
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            combine(
                container.repository.dashboard(),
                container.repository.transactions,
                container.repository.budgets(),
            ) { _, _, _ -> Unit }
                .collectLatest { AwareWidgets.refresh(this@AwareApplication) }
        }
        RecurringWorker.schedule(this)
        BudgetAlertWorker.schedule(this)
        if (BuildConfig.SELF_UPDATE_ENABLED) UpdateCheckWorker.schedule(this)
    }
}

val MIGRATION_5_6 = object : Migration(5, 6) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("DROP TABLE IF EXISTS capture_candidates")
        db.execSQL("DROP TABLE IF EXISTS weekly_reports")
    }
}

val MIGRATION_4_5 = object : Migration(4, 5) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE expense_categories ADD COLUMN expenseNature TEXT NOT NULL DEFAULT 'DISCRETIONARY'")
        db.execSQL("UPDATE expense_categories SET expenseNature = 'COMMITMENT' WHERE lower(name) IN ('family', 'subscriptions')")
        db.execSQL("UPDATE expense_categories SET expenseNature = 'ESSENTIAL' WHERE lower(name) IN ('groceries', 'travel', 'health')")
        db.execSQL("UPDATE expense_categories SET expenseNature = 'ONE_TIME' WHERE lower(name) = 'education'")
        db.execSQL("ALTER TABLE transactions ADD COLUMN incomeKind TEXT")
        db.execSQL("ALTER TABLE transactions ADD COLUMN linkedTransactionId INTEGER")
        db.execSQL("UPDATE transactions SET incomeKind = 'SALARY' WHERE type = 'INCOME' AND categoryId IN (SELECT id FROM income_categories WHERE lower(name) = 'salary')")
        db.execSQL(
            """CREATE TABLE IF NOT EXISTS monthly_plans (
                monthKey TEXT NOT NULL PRIMARY KEY,
                expectedIncomePaise INTEGER NOT NULL,
                savingsTargetPaise INTEGER NOT NULL,
                commitmentTargetPaise INTEGER NOT NULL,
                updatedAt INTEGER NOT NULL
            )""".trimIndent(),
        )
        db.execSQL(
            """CREATE TABLE IF NOT EXISTS savings_goals (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                name TEXT NOT NULL,
                targetPaise INTEGER NOT NULL,
                savedPaise INTEGER NOT NULL,
                targetAt INTEGER,
                isArchived INTEGER NOT NULL,
                createdAt INTEGER NOT NULL
            )""".trimIndent(),
        )
    }
}

val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("CREATE TABLE IF NOT EXISTS expense_categories (id INTEGER NOT NULL PRIMARY KEY, name TEXT NOT NULL, emoji TEXT NOT NULL, colorArgb INTEGER NOT NULL, isArchived INTEGER NOT NULL)")
        db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_expense_categories_name ON expense_categories (name)")
        db.execSQL("CREATE TABLE IF NOT EXISTS income_categories (id INTEGER NOT NULL PRIMARY KEY, name TEXT NOT NULL, emoji TEXT NOT NULL, colorArgb INTEGER NOT NULL, isArchived INTEGER NOT NULL)")
        db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_income_categories_name ON income_categories (name)")
        db.execSQL("INSERT INTO expense_categories (id, name, emoji, colorArgb, isArchived) SELECT id, name, emoji, colorArgb, isArchived FROM categories WHERE isIncome = 0")
        db.execSQL("INSERT INTO income_categories (id, name, emoji, colorArgb, isArchived) SELECT id, name, emoji, colorArgb, isArchived FROM categories WHERE isIncome = 1")

        // SQLite cannot point one foreign key at two category tables. Category IDs
        // remain globally unique, while account relations keep database-enforced FKs.
        db.execSQL(
            """CREATE TABLE transactions_new (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                amountPaise INTEGER NOT NULL,
                currency TEXT NOT NULL,
                type TEXT NOT NULL,
                status TEXT NOT NULL,
                source TEXT NOT NULL,
                accountId INTEGER NOT NULL,
                destinationAccountId INTEGER,
                categoryId INTEGER,
                merchant TEXT NOT NULL,
                note TEXT NOT NULL,
                tags TEXT NOT NULL,
                occurredAt INTEGER NOT NULL,
                createdAt INTEGER NOT NULL,
                sourceFingerprint TEXT,
                FOREIGN KEY(accountId) REFERENCES accounts(id) ON UPDATE NO ACTION ON DELETE RESTRICT,
                FOREIGN KEY(destinationAccountId) REFERENCES accounts(id) ON UPDATE NO ACTION ON DELETE SET NULL
            )""".trimIndent(),
        )
        db.execSQL("INSERT INTO transactions_new SELECT id, amountPaise, currency, type, status, source, accountId, destinationAccountId, categoryId, merchant, note, tags, occurredAt, createdAt, sourceFingerprint FROM transactions")
        db.execSQL("DROP TABLE transactions")
        db.execSQL("DROP TABLE categories")
        db.execSQL("ALTER TABLE transactions_new RENAME TO transactions")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_transactions_occurredAt ON transactions (occurredAt)")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_transactions_accountId ON transactions (accountId)")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_transactions_destinationAccountId ON transactions (destinationAccountId)")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_transactions_categoryId ON transactions (categoryId)")
        db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_transactions_sourceFingerprint ON transactions (sourceFingerprint)")
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
