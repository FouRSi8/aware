package com.aware.app.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface AccountDao {
    @Query("SELECT * FROM accounts ORDER BY isDefault DESC, createdAt") fun observeAll(): Flow<List<AccountEntity>>
    @Query("SELECT * FROM accounts WHERE isDefault = 1 LIMIT 1") suspend fun defaultAccount(): AccountEntity?
    @Query("SELECT * FROM accounts WHERE kind = 'CASH' LIMIT 1") suspend fun cashAccount(): AccountEntity?
    @Query("SELECT * FROM accounts ORDER BY id") suspend fun allOnce(): List<AccountEntity>
    @Query("SELECT * FROM accounts WHERE id = :id LIMIT 1") suspend fun byId(id: Long): AccountEntity?
    @Query("UPDATE accounts SET isDefault = 0") suspend fun clearDefault()
    @Insert suspend fun insert(account: AccountEntity): Long
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun restoreAll(items: List<AccountEntity>)
    @Query("DELETE FROM accounts") suspend fun clear()
    @Update suspend fun update(account: AccountEntity)
}

@Dao
interface CategoryDao {
    @Query("SELECT * FROM categories WHERE isArchived = 0 ORDER BY isIncome, name") fun observeAll(): Flow<List<CategoryEntity>>
    @Query("SELECT * FROM categories WHERE lower(name) = lower(:name) LIMIT 1") suspend fun byName(name: String): CategoryEntity?
    @Query("SELECT * FROM categories ORDER BY id") suspend fun allOnce(): List<CategoryEntity>
    @Insert(onConflict = OnConflictStrategy.IGNORE) suspend fun insertAll(items: List<CategoryEntity>)
    @Insert suspend fun insert(item: CategoryEntity): Long
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun restoreAll(items: List<CategoryEntity>)
    @Query("DELETE FROM categories") suspend fun clear()
    @Update suspend fun update(item: CategoryEntity)
}

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions ORDER BY occurredAt DESC") fun observeAll(): Flow<List<TransactionEntity>>
    @Query("SELECT * FROM transactions WHERE occurredAt >= :from AND occurredAt < :to AND status = 'POSTED' ORDER BY occurredAt DESC") fun observeRange(from: Long, to: Long): Flow<List<TransactionEntity>>
    @Query("SELECT * FROM transactions WHERE id = :id") suspend fun byId(id: Long): TransactionEntity?
    @Query("SELECT * FROM transactions ORDER BY id") suspend fun allOnce(): List<TransactionEntity>
    @Query("SELECT * FROM transactions WHERE status = 'EXPECTED' AND type = :type AND occurredAt BETWEEN :from AND :to") suspend fun expectedNear(type: TransactionType, from: Long, to: Long): List<TransactionEntity>
    @Query("SELECT COALESCE(SUM(amountPaise), 0) FROM transactions WHERE status = 'POSTED' AND type = 'EXPENSE' AND occurredAt >= :from AND occurredAt < :to AND (:categoryId IS NULL OR categoryId = :categoryId)") suspend fun expenseTotal(from: Long, to: Long, categoryId: Long?): Long
    @Query("SELECT COALESCE(SUM(amountPaise), 0) FROM transactions WHERE status = 'POSTED' AND type = 'EXPENSE' AND occurredAt >= :from AND occurredAt < :to AND (:categoryId IS NULL OR categoryId = :categoryId) AND (:accountId IS NULL OR accountId = :accountId) AND (:payee IS NULL OR lower(merchant) = lower(:payee))") suspend fun expenseTotalScoped(from: Long, to: Long, categoryId: Long?, accountId: Long?, payee: String?): Long
    @Insert(onConflict = OnConflictStrategy.IGNORE) suspend fun insert(item: TransactionEntity): Long
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun restoreAll(items: List<TransactionEntity>)
    @Query("DELETE FROM transactions") suspend fun clear()
    @Update suspend fun update(item: TransactionEntity)
    @Delete suspend fun delete(item: TransactionEntity)
}

@Dao
interface CaptureDao {
    @Query("SELECT * FROM capture_candidates WHERE status = 'PENDING_REVIEW' ORDER BY receivedAt DESC") fun observePending(): Flow<List<CaptureCandidateEntity>>
    @Query("SELECT * FROM capture_candidates WHERE status = 'PENDING_REVIEW' ORDER BY receivedAt DESC LIMIT 1") suspend fun latestPending(): CaptureCandidateEntity?
    @Query("SELECT COUNT(*) FROM capture_candidates WHERE status = 'PENDING_REVIEW'") suspend fun pendingCount(): Int
    @Query("SELECT * FROM capture_candidates WHERE id = :id") suspend fun byId(id: Long): CaptureCandidateEntity?
    @Insert(onConflict = OnConflictStrategy.IGNORE) suspend fun insert(item: CaptureCandidateEntity): Long
    @Update suspend fun update(item: CaptureCandidateEntity)
    @Query("DELETE FROM capture_candidates WHERE receivedAt < :before AND status != 'PENDING_REVIEW'") suspend fun deleteExpired(before: Long)
}

@Dao
interface BudgetDao {
    @Query("SELECT * FROM budget_buckets WHERE monthKey = :monthKey ORDER BY name") fun observeMonth(monthKey: String): Flow<List<BudgetBucketEntity>>
    @Query("SELECT * FROM budget_buckets WHERE (isRecurring = 1 OR monthKey = :monthKey) AND (startAt = 0 OR startAt <= :now) AND (endAt IS NULL OR endAt >= :now) ORDER BY name") fun observeActive(monthKey: String, now: Long): Flow<List<BudgetBucketEntity>>
    @Query("SELECT * FROM budget_buckets ORDER BY id") suspend fun allOnce(): List<BudgetBucketEntity>
    @Query("SELECT * FROM budget_buckets WHERE monthKey = :monthKey ORDER BY id") suspend fun monthOnce(monthKey: String): List<BudgetBucketEntity>
    @Query("SELECT * FROM budget_buckets WHERE (isRecurring = 1 OR monthKey = :monthKey) AND (startAt = 0 OR startAt <= :now) AND (endAt IS NULL OR endAt >= :now) ORDER BY id") suspend fun activeOnce(monthKey: String, now: Long): List<BudgetBucketEntity>
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun upsert(item: BudgetBucketEntity): Long
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun restoreAll(items: List<BudgetBucketEntity>)
    @Query("DELETE FROM budget_buckets") suspend fun clear()
    @Delete suspend fun delete(item: BudgetBucketEntity)
}

@Dao
interface RecurringDao {
    @Query("SELECT * FROM recurring_rules WHERE isActive = 1 ORDER BY nextExpectedAt") fun observeActive(): Flow<List<RecurringRuleEntity>>
    @Query("SELECT * FROM recurring_rules WHERE isActive = 1 AND nextExpectedAt <= :now ORDER BY nextExpectedAt") suspend fun due(now: Long): List<RecurringRuleEntity>
    @Query("SELECT * FROM recurring_rules ORDER BY id") suspend fun allOnce(): List<RecurringRuleEntity>
    @Insert suspend fun insert(item: RecurringRuleEntity): Long
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun restoreAll(items: List<RecurringRuleEntity>)
    @Query("DELETE FROM recurring_rules") suspend fun clear()
    @Update suspend fun update(item: RecurringRuleEntity)
    @Delete suspend fun delete(item: RecurringRuleEntity)
}

@Dao
interface MerchantRuleDao {
    @Query("SELECT * FROM merchant_rules ORDER BY id") suspend fun allOnce(): List<MerchantRuleEntity>
    @Query("SELECT * FROM merchant_rules WHERE normalizedMerchant = :merchant LIMIT 1") suspend fun find(merchant: String): MerchantRuleEntity?
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun upsert(rule: MerchantRuleEntity): Long
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun restoreAll(items: List<MerchantRuleEntity>)
    @Query("DELETE FROM merchant_rules") suspend fun clear()
}
