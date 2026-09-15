package com.aware.app.data

import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId

data class CoachAction(
    val title: String,
    val detail: String,
    val monthlyImpactPaise: Long = 0,
)

data class FinanceCoachSnapshot(
    val projectedSpendPaise: Long = 0,
    val projectedSavingsPaise: Long = 0,
    val projectedSavingsRate: Int = 0,
    val safeDailySpendPaise: Long = 0,
    val daysRemaining: Int = 0,
    val overspendingLikely: Boolean = false,
    val topOpportunityPaise: Long = 0,
    val actions: List<CoachAction> = emptyList(),
    val answers: Map<String, String> = emptyMap(),
)

/** Private, deterministic coaching derived entirely from the local ledger. */
object FinanceCoach {
    fun analyse(
        transactions: List<TransactionEntity>,
        categories: List<CategoryEntity>,
        budgets: List<BudgetBucketEntity>,
        plan: MonthlyPlanEntity?,
        today: LocalDate = LocalDate.now(),
        zone: ZoneId = ZoneId.systemDefault(),
    ): FinanceCoachSnapshot {
        val month = YearMonth.from(today)
        val posted = transactions.filter {
            it.status == TransactionStatus.POSTED &&
                YearMonth.from(Instant.ofEpochMilli(it.occurredAt).atZone(zone)) == month
        }
        val expenses = posted.filter { it.type == TransactionType.EXPENSE }
        val spent = expenses.sumOf { it.amountPaise }
        val earned = posted.filter {
            it.type == TransactionType.INCOME && it.incomeKind in setOf(IncomeKind.SALARY, IncomeKind.OTHER_EARNED, IncomeKind.GIFT)
        }.sumOf { it.amountPaise }
        val refunds = posted.filter { it.type == TransactionType.REFUND }.sumOf { it.amountPaise }
        val incomeBase = plan?.expectedIncomePaise?.takeIf { it > 0 } ?: earned
        val elapsed = today.dayOfMonth.coerceAtLeast(1)
        val remaining = (month.lengthOfMonth() - elapsed).coerceAtLeast(0)
        val projectedSpend = if (spent == 0L) 0L else spent * month.lengthOfMonth() / elapsed
        val projectedSavings = incomeBase + refunds - projectedSpend
        val projectedRate = if (incomeBase <= 0) 0 else (projectedSavings * 100 / incomeBase).toInt().coerceIn(-999, 100)
        val savingsTarget = plan?.savingsTargetPaise ?: 0L
        val safeDaily = if (remaining == 0) 0L else
            (incomeBase + refunds - savingsTarget - spent).coerceAtLeast(0) / remaining
        val categoriesById = categories.associateBy { it.id }
        val discretionary = expenses.filter {
            categoriesById[it.categoryId]?.expenseNature == ExpenseNature.DISCRETIONARY
        }
        val discretionaryTotal = discretionary.sumOf { it.amountPaise }
        val byCategory = discretionary.groupBy { it.categoryId }.mapValues { (_, rows) -> rows.sumOf { it.amountPaise } }
        val topCategory = byCategory.maxByOrNull { it.value }
        val topCategoryName = topCategory?.key?.let(categoriesById::get)?.name ?: "discretionary spending"
        val byMerchant = discretionary.filter { it.merchant.isNotBlank() }
            .groupBy { it.merchant.trim() }
            .mapValues { (_, rows) -> rows.sumOf { it.amountPaise } to rows.size }
        val repeatMerchant = byMerchant.entries.filter { it.value.second >= 3 }.maxByOrNull { it.value.first }
        val foodDelivery = expenses.filter { categoriesById[it.categoryId]?.name.equals("Food delivery", true) }
        val foodTotal = foodDelivery.sumOf { it.amountPaise }
        val opportunity = maxOf(
            if (foodDelivery.size >= 3) foodTotal / 3 else 0L,
            topCategory?.value?.div(5) ?: 0L,
            discretionaryTotal / 10,
        )
        val actions = buildList {
            if (incomeBase <= 0L) add(CoachAction("Set your monthly income", "A forecast needs an income or monthly plan before it can protect savings."))
            if (plan == null) add(CoachAction("Pay yourself first", "Create a monthly plan and reserve a savings target before flexible spending."))
            if (foodDelivery.size >= 3) add(CoachAction("Trim food delivery", "${foodDelivery.size} orders total ${formatMoney(foodTotal)}. Removing roughly one in three could save ${formatMoney(foodTotal / 3)}.", foodTotal / 3))
            if (repeatMerchant != null) add(CoachAction("Watch ${repeatMerchant.key}", "${repeatMerchant.value.second} payments have added up to ${formatMoney(repeatMerchant.value.first)} this month."))
            if (topCategory != null) add(CoachAction("Tighten $topCategoryName", "It is your largest discretionary category at ${formatMoney(topCategory.value)}. A 20% reduction keeps ${formatMoney(topCategory.value / 5)}.", topCategory.value / 5))
            if (projectedSavings < savingsTarget && incomeBase > 0) add(CoachAction("Slow the daily pace", "Stay near ${formatMoney(safeDaily)} per day to protect the ${formatMoney(savingsTarget)} savings target."))
            val uncategorised = expenses.filter { it.categoryId == null }.sumOf { it.amountPaise }
            if (uncategorised > 0) add(CoachAction("Classify the unknowns", "${formatMoney(uncategorised)} is uncategorised, so the diagnosis is less precise."))
            if (isEmpty()) add(CoachAction("Keep the rhythm", "No obvious leak is visible yet. Keep logging and protect savings before discretionary purchases."))
        }.distinctBy { it.title }.take(4)
        val activeMonthlyCaps = budgets.filter { it.period == BudgetPeriod.MONTHLY && it.capPaise > 0 }
        val crossed = activeMonthlyCaps.count { budget ->
            val scoped = expenses.filter { item ->
                when (budget.scope) {
                    BudgetScope.OVERALL -> true
                    BudgetScope.CATEGORY -> budget.categoryId == item.categoryId
                    BudgetScope.ACCOUNT -> budget.accountId == item.accountId
                    BudgetScope.PAYEE -> item.merchant.equals(budget.payee, true)
                }
            }.sumOf { it.amountPaise }
            scoped > budget.capPaise
        }
        val status = if (projectedSavings < savingsTarget) "At the current pace, you may miss your savings target by ${formatMoney(savingsTarget - projectedSavings)}." else "Your current pace protects the planned savings target."
        val answers = linkedMapOf(
            "Where is my money going?" to if (topCategory == null) "Log a few categorised expenses and I’ll identify the strongest pattern." else "$topCategoryName leads discretionary spending at ${formatMoney(topCategory.value)} this month.",
            "Am I on track?" to status,
            "What should I change?" to actions.first().detail,
            "How much can I spend today?" to if (remaining == 0) "This month ends today. Review the final total before setting next month’s plan." else "About ${formatMoney(safeDaily)} today keeps your current savings plan intact.",
        )
        return FinanceCoachSnapshot(
            projectedSpendPaise = projectedSpend,
            projectedSavingsPaise = projectedSavings,
            projectedSavingsRate = projectedRate,
            safeDailySpendPaise = safeDaily,
            daysRemaining = remaining,
            overspendingLikely = projectedSavings < savingsTarget || crossed > 0,
            topOpportunityPaise = opportunity,
            actions = actions,
            answers = answers,
        )
    }

    private fun formatMoney(paise: Long): String = "₹%,.0f".format(paise / 100.0)
}
