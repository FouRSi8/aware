package com.aware.app.data

import org.junit.Assert.assertEquals
import org.junit.Test

class MonthlyFinanceCalculatorTest {
    private val bank = AccountEntity(id = 1, name = "Bank", kind = AccountKind.BANK, openingBalancePaise = 50_412)
    private val cash = AccountEntity(id = 2, name = "Cash", kind = AccountKind.CASH)
    private val commitment = CategoryEntity(id = 10, name = "Family", emoji = "F", colorArgb = 0, expenseNature = ExpenseNature.COMMITMENT)
    private val discretionary = CategoryEntity(id = 11, name = "Flexible", emoji = "D", colorArgb = 0, expenseNature = ExpenseNature.DISCRETIONARY)

    private fun transaction(
        id: Long,
        amount: Long,
        type: TransactionType,
        accountId: Long = bank.id,
        destinationId: Long? = null,
        categoryId: Long? = discretionary.id,
        incomeKind: IncomeKind? = null,
    ) = TransactionEntity(
        id = id,
        amountPaise = amount,
        type = type,
        source = TransactionSource.MANUAL,
        accountId = accountId,
        destinationAccountId = destinationId,
        categoryId = categoryId,
        merchant = "fixture-$id",
        occurredAt = 1_788_600_000_000L,
        incomeKind = incomeKind,
    )

    @Test fun separatesSpendableIncomeAndKeepsAtmTransfersNeutral() {
        val transactions = listOf(
            transaction(1, 1_500_000, TransactionType.INCOME, categoryId = null, incomeKind = IncomeKind.SALARY),
            transaction(2, 1_233_000, TransactionType.INCOME, categoryId = null, incomeKind = IncomeKind.PASS_THROUGH),
            transaction(3, 750_000, TransactionType.TRANSFER, destinationId = cash.id, categoryId = null),
            transaction(4, 750_000, TransactionType.EXPENSE, accountId = cash.id, categoryId = commitment.id),
            transaction(5, 200_000, TransactionType.EXPENSE, categoryId = commitment.id),
            transaction(6, 183_500, TransactionType.EXPENSE),
            transaction(7, 280_200, TransactionType.EXPENSE),
            transaction(8, 200_884, TransactionType.EXPENSE),
            transaction(9, 1_151_600, TransactionType.EXPENSE),
        )

        val result = MonthlyFinanceCalculator.calculate(
            monthTransactions = transactions,
            allTransactions = transactions,
            categories = listOf(commitment, discretionary),
            accounts = listOf(bank, cash),
            plan = null,
            pendingCount = 2,
            daysRemaining = 11,
        )

        assertEquals(1_500_000, result.incomePaise)
        assertEquals(1_233_000, result.otherIncomePaise)
        assertEquals(2_766_184, result.spendingPaise)
        assertEquals(950_000, result.commitmentSpendingPaise)
        assertEquals(-1_266_184, result.savingsPaise)
        assertEquals(-1_266_184, result.safeToSpendPaise)
        assertEquals(17_228, result.actualBalancePaise)
        assertEquals(0, result.unresolvedCashPaise)
        assertEquals(2, result.pendingCount)
    }

    @Test fun protectsPlannedSavingsAndCommitmentsBeforeFlexibleSpending() {
        val transactions = listOf(
            transaction(1, 1_000_000, TransactionType.INCOME, categoryId = null, incomeKind = IncomeKind.SALARY),
            transaction(2, 100_000, TransactionType.EXPENSE),
        )
        val plan = MonthlyPlanEntity("2026-09", expectedIncomePaise = 1_200_000, savingsTargetPaise = 300_000, commitmentTargetPaise = 400_000)

        val result = MonthlyFinanceCalculator.calculate(
            monthTransactions = transactions,
            allTransactions = transactions,
            categories = listOf(discretionary),
            accounts = listOf(bank),
            plan = plan,
            pendingCount = 0,
            daysRemaining = 20,
        )

        assertEquals(400_000, result.safeToSpendPaise)
        assertEquals(300_000, result.plannedSavingsPaise)
    }
}
