package com.aware.app.data

/** Pure accounting rules used by the dashboard and deterministic fixture tests. */
internal object MonthlyFinanceCalculator {
    private val spendableIncomeKinds = setOf(IncomeKind.SALARY, IncomeKind.OTHER_EARNED, IncomeKind.GIFT)

    fun calculate(
        monthTransactions: List<TransactionEntity>,
        allTransactions: List<TransactionEntity>,
        categories: List<CategoryEntity>,
        accounts: List<AccountEntity>,
        plan: MonthlyPlanEntity?,
        pendingCount: Int,
        daysRemaining: Int,
    ): DashboardSummary {
        val postedMonth = monthTransactions.filter { it.status == TransactionStatus.POSTED }
        val incomeItems = postedMonth.filter { it.type == TransactionType.INCOME }
        val income = incomeItems.filter { it.incomeKind in spendableIncomeKinds }.sumOf { it.amountPaise }
        val otherCredits = incomeItems.filter { it.incomeKind !in spendableIncomeKinds }.sumOf { it.amountPaise }
        val refunds = postedMonth.filter { it.type == TransactionType.REFUND }.sumOf { it.amountPaise }
        val spending = postedMonth.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amountPaise }
        val categoriesById = categories.associateBy { it.id }
        val commitmentSpending = postedMonth.filter {
            it.type == TransactionType.EXPENSE && categoriesById[it.categoryId]?.expenseNature == ExpenseNature.COMMITMENT
        }.sumOf { it.amountPaise }
        val flexibleSpending = spending - commitmentSpending
        val plannedIncome = plan?.expectedIncomePaise?.takeIf { it > 0 } ?: income
        val plannedSavings = plan?.savingsTargetPaise ?: 0L
        val plannedCommitments = plan?.commitmentTargetPaise ?: 0L
        val safeToSpend = plannedIncome + refunds - plannedSavings - maxOf(plannedCommitments, commitmentSpending) - flexibleSpending

        val postedAll = allTransactions.filter { it.status == TransactionStatus.POSTED }
        fun balance(account: AccountEntity): Long = account.openingBalancePaise + postedAll.sumOf { transaction ->
            when {
                transaction.type == TransactionType.TRANSFER && transaction.accountId == account.id -> -transaction.amountPaise
                transaction.type == TransactionType.TRANSFER && transaction.destinationAccountId == account.id -> transaction.amountPaise
                transaction.accountId == account.id && transaction.type == TransactionType.EXPENSE -> -transaction.amountPaise
                transaction.accountId == account.id && transaction.type in setOf(TransactionType.INCOME, TransactionType.REFUND) -> transaction.amountPaise
                else -> 0L
            }
        }
        val actualBalance = accounts.sumOf(::balance)
        val unresolvedCash = accounts.filter { it.kind == AccountKind.CASH }.sumOf(::balance).coerceAtLeast(0)

        return DashboardSummary(
            incomePaise = income,
            otherIncomePaise = otherCredits,
            refundPaise = refunds,
            spendingPaise = spending,
            savingsPaise = income + refunds - spending,
            safeToSpendPaise = safeToSpend,
            plannedSavingsPaise = plannedSavings,
            commitmentSpendingPaise = commitmentSpending,
            unresolvedCashPaise = unresolvedCash,
            actualBalancePaise = actualBalance,
            daysRemaining = daysRemaining,
            pendingCount = pendingCount,
        )
    }
}
