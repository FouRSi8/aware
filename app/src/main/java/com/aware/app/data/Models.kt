package com.aware.app.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable enum class AccountKind { BANK, UPI, CASH }
@Serializable enum class TransactionType { INCOME, EXPENSE, TRANSFER, REFUND, ADJUSTMENT }
@Serializable enum class TransactionStatus { EXPECTED, PENDING_REVIEW, POSTED, DISMISSED, REVERSED }
@Serializable enum class TransactionSource { SMS, NOTIFICATION, MANUAL, RECURRING, STATEMENT }
@Serializable enum class RecurrenceCadence { DAILY, WEEKLY, MONTHLY, YEARLY, CUSTOM }
@Serializable enum class BudgetScope { OVERALL, CATEGORY, ACCOUNT, PAYEE }
@Serializable enum class BudgetPeriod { DAILY, WEEKLY, MONTHLY, YEARLY }

@Entity(tableName = "accounts")
@Serializable
data class AccountEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val kind: AccountKind,
    val openingBalancePaise: Long = 0,
    val currency: String = "INR",
    val isDefault: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
)

@Entity(tableName = "categories", indices = [Index("name", unique = true)])
@Serializable
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val emoji: String,
    val colorArgb: Long,
    val isIncome: Boolean = false,
    val isArchived: Boolean = false,
)

@Entity(
    tableName = "transactions",
    indices = [Index("occurredAt"), Index("accountId"), Index("destinationAccountId"), Index("categoryId"), Index("sourceFingerprint", unique = true)],
    foreignKeys = [
        ForeignKey(AccountEntity::class, ["id"], ["accountId"], onDelete = ForeignKey.RESTRICT),
        ForeignKey(AccountEntity::class, ["id"], ["destinationAccountId"], onDelete = ForeignKey.SET_NULL),
        ForeignKey(CategoryEntity::class, ["id"], ["categoryId"], onDelete = ForeignKey.SET_NULL),
    ],
)
@Serializable
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val amountPaise: Long,
    val currency: String = "INR",
    val type: TransactionType,
    val status: TransactionStatus = TransactionStatus.POSTED,
    val source: TransactionSource,
    val accountId: Long,
    val destinationAccountId: Long? = null,
    val categoryId: Long? = null,
    val merchant: String,
    val note: String = "",
    val tags: String = "",
    val occurredAt: Long,
    val createdAt: Long = System.currentTimeMillis(),
    val sourceFingerprint: String? = null,
)

@Entity(tableName = "capture_candidates", indices = [Index("fingerprint", unique = true), Index("receivedAt")])
data class CaptureCandidateEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sender: String,
    val encryptedBody: String,
    val amountPaise: Long,
    val currency: String = "INR",
    val type: TransactionType,
    val merchant: String,
    val accountSuffix: String? = null,
    val reference: String? = null,
    val confidence: Float,
    val fingerprint: String,
    val receivedAt: Long,
    val source: TransactionSource = TransactionSource.SMS,
    val status: TransactionStatus = TransactionStatus.PENDING_REVIEW,
)

@Entity(tableName = "weekly_reports", indices = [Index(value = ["weekStart"], unique = true)])
data class WeeklyReportEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val weekStart: Long,
    val weekEndExclusive: Long,
    val generatedAt: Long,
    val incomePaise: Long,
    val refundPaise: Long,
    val spendingPaise: Long,
    val transferPaise: Long,
    val topMerchant: String? = null,
)

@Entity(tableName = "budget_buckets", indices = [Index(value = ["monthKey", "categoryId"], unique = true)])
@Serializable
data class BudgetBucketEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val monthKey: String,
    val name: String,
    val categoryId: Long? = null,
    val scope: BudgetScope = BudgetScope.CATEGORY,
    val accountId: Long? = null,
    val payee: String? = null,
    val period: BudgetPeriod = BudgetPeriod.MONTHLY,
    val startAt: Long = 0,
    val endAt: Long? = null,
    val isRecurring: Boolean = true,
    val capPaise: Long,
    val allocationPaise: Long = 0,
    val alert50: Boolean = true,
    val alert75: Boolean = true,
    val alert90: Boolean = true,
    val alert100: Boolean = true,
)

@Entity(tableName = "recurring_rules")
@Serializable
data class RecurringRuleEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val amountPaise: Long,
    val type: TransactionType,
    val accountId: Long,
    val categoryId: Long? = null,
    val cadence: RecurrenceCadence,
    val customIntervalDays: Int = 1,
    val reminderMinutesBefore: Int = 0,
    val startAt: Long,
    val endAt: Long? = null,
    val nextExpectedAt: Long,
    val matchingTolerancePaise: Long = 0,
    val isActive: Boolean = true,
)

@Entity(tableName = "merchant_rules", indices = [Index("normalizedMerchant", unique = true)])
@Serializable
data class MerchantRuleEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val normalizedMerchant: String,
    val displayMerchant: String,
    val categoryId: Long?,
    val accountId: Long?,
    val updatedAt: Long = System.currentTimeMillis(),
)

data class DashboardSummary(
    val incomePaise: Long = 0,
    val otherIncomePaise: Long = 0,
    val spendingPaise: Long = 0,
    val savingsPaise: Long = 0,
    val pendingCount: Int = 0,
)
