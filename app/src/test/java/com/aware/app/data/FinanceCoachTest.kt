package com.aware.app.data

import java.time.LocalDate
import java.time.ZoneOffset
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FinanceCoachTest {
    private val food = CategoryEntity(1, "Food delivery", "F", 0, expenseNature = ExpenseNature.DISCRETIONARY)
    private fun expense(id: Long, amount: Long, day: Int, merchant: String = "Swiggy") = TransactionEntity(
        id = id, amountPaise = amount, type = TransactionType.EXPENSE, source = TransactionSource.MANUAL,
        accountId = 1, categoryId = food.id, merchant = merchant,
        occurredAt = LocalDate.of(2026, 9, day).atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli(),
    )

    @Test fun forecastsAndFindsConcreteFoodOpportunity() {
        val result = FinanceCoach.analyse(
            transactions = listOf(expense(1, 30_000, 1), expense(2, 30_000, 5), expense(3, 30_000, 10)),
            categories = listOf(food), budgets = emptyList(),
            plan = MonthlyPlanEntity("2026-09", 300_000, 100_000, 0),
            today = LocalDate.of(2026, 9, 15), zone = ZoneOffset.UTC,
        )
        assertEquals(180_000, result.projectedSpendPaise)
        assertEquals(120_000, result.projectedSavingsPaise)
        assertEquals(30_000, result.topOpportunityPaise)
        assertTrue(result.actions.any { it.title == "Trim food delivery" })
        assertFalse(result.overspendingLikely)
    }

    @Test fun warnsWhenProjectedSavingsMissTarget() {
        val result = FinanceCoach.analyse(
            transactions = listOf(expense(1, 200_000, 10)), categories = listOf(food), budgets = emptyList(),
            plan = MonthlyPlanEntity("2026-09", 300_000, 150_000, 0),
            today = LocalDate.of(2026, 9, 15), zone = ZoneOffset.UTC,
        )
        assertTrue(result.overspendingLikely)
        assertTrue(result.answers.getValue("Am I on track?").contains("miss"))
    }
}
