package com.aware.app.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [
        AccountEntity::class,
        CategoryEntity::class,
        TransactionEntity::class,
        CaptureCandidateEntity::class,
        BudgetBucketEntity::class,
        RecurringRuleEntity::class,
        MerchantRuleEntity::class,
        WeeklyReportEntity::class,
    ],
    version = 3,
    exportSchema = true,
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun accountDao(): AccountDao
    abstract fun categoryDao(): CategoryDao
    abstract fun transactionDao(): TransactionDao
    abstract fun captureDao(): CaptureDao
    abstract fun budgetDao(): BudgetDao
    abstract fun recurringDao(): RecurringDao
    abstract fun merchantRuleDao(): MerchantRuleDao
    abstract fun weeklyReportDao(): WeeklyReportDao
}
