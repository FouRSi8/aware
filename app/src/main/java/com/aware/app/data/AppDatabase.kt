package com.aware.app.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [
        AccountEntity::class,
        ExpenseCategoryEntity::class,
        IncomeCategoryEntity::class,
        TransactionEntity::class,
        BudgetBucketEntity::class,
        RecurringRuleEntity::class,
        MerchantRuleEntity::class,
        MonthlyPlanEntity::class,
        SavingsGoalEntity::class,
    ],
    version = 6,
    exportSchema = true,
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun accountDao(): AccountDao
    abstract fun categoryDao(): CategoryDao
    abstract fun transactionDao(): TransactionDao
    abstract fun budgetDao(): BudgetDao
    abstract fun recurringDao(): RecurringDao
    abstract fun merchantRuleDao(): MerchantRuleDao
    abstract fun monthlyPlanDao(): MonthlyPlanDao
    abstract fun savingsGoalDao(): SavingsGoalDao
}
