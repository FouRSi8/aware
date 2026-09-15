package com.aware.app.ai

import com.aware.app.data.CategoryEntity
import com.aware.app.data.MerchantRuleEntity
import com.aware.app.data.TransactionEntity
import com.aware.app.data.TransactionSource
import com.aware.app.data.TransactionType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MerchantCategorizerTest {
    private val food = category(1, "Food delivery")
    private val groceries = category(2, "Groceries")
    private val salary = category(3, "Salary", income = true)
    private val categories = listOf(food, groceries, salary)

    @Test fun exactLearnedRuleWinsWithoutCloud() {
        val suggestion = MerchantCategorizer.suggest(
            merchant = "UPI / SWIGGY 927461",
            isIncome = false,
            categories = categories,
            rules = listOf(MerchantRuleEntity(normalizedMerchant = "swiggy", displayMerchant = "Swiggy", categoryId = food.id, accountId = 1)),
            history = emptyList(),
        )

        assertEquals(food.id, suggestion?.categoryId)
        assertEquals(CategorySuggestionSource.LEARNED_RULE, suggestion?.source)
        assertEquals(1.0, suggestion?.confidence ?: 0.0, 0.0)
    }

    @Test fun similarHistoricalMerchantCanBeAppliedWithHighConfidence() {
        val suggestion = MerchantCategorizer.suggest(
            merchant = "Acme Fresh Mart Bangalore",
            isIncome = false,
            categories = categories,
            rules = emptyList(),
            history = listOf(transaction("ACME FRESH MART", groceries.id)),
        )

        assertEquals(groceries.id, suggestion?.categoryId)
        assertTrue((suggestion?.confidence ?: 0.0) >= MerchantCategorizer.AUTO_APPLY_CONFIDENCE)
    }

    @Test fun incomeAndExpenseCategorySpacesStaySeparate() {
        val suggestion = MerchantCategorizer.suggest(
            merchant = "Salary payroll September",
            isIncome = true,
            categories = categories,
            rules = listOf(MerchantRuleEntity(normalizedMerchant = "salary payroll september", displayMerchant = "Salary", categoryId = food.id, accountId = 1)),
            history = emptyList(),
        )

        assertEquals(salary.id, suggestion?.categoryId)
        assertEquals(CategorySuggestionSource.BUILT_IN, suggestion?.source)
    }

    @Test fun unfamiliarMerchantRemainsForReview() {
        assertNull(
            MerchantCategorizer.suggest(
                merchant = "Northwind Services",
                isIncome = false,
                categories = categories,
                rules = emptyList(),
                history = emptyList(),
            ),
        )
    }

    @Test fun normalizationRemovesIdentifiersAndBankingNoise() {
        assertEquals("swiggy", MerchantCategorizer.normalize("UPI TXN 920101 SWIGGY ref user@example.com"))
    }

    private fun category(id: Long, name: String, income: Boolean = false) = CategoryEntity(
        id = id,
        name = name,
        emoji = "x",
        colorArgb = 0,
        isIncome = income,
    )

    private fun transaction(merchant: String, categoryId: Long) = TransactionEntity(
        id = 10,
        amountPaise = 100,
        type = TransactionType.EXPENSE,
        source = TransactionSource.MANUAL,
        accountId = 1,
        categoryId = categoryId,
        merchant = merchant,
        occurredAt = 1,
    )
}
