package com.aware.app.ai

import com.aware.app.data.CategoryEntity
import com.aware.app.data.MerchantRuleEntity
import com.aware.app.data.TransactionEntity
import com.aware.app.data.TransactionType
import java.text.Normalizer
import java.util.Locale

enum class CategorySuggestionSource { LEARNED_RULE, HISTORY, BUILT_IN }

data class CategorySuggestion(
    val categoryId: Long,
    val confidence: Double,
    val source: CategorySuggestionSource,
)

/**
 * A deterministic, offline-first merchant classifier. It deliberately returns
 * no result when confidence is weak so financial data is never silently filed
 * under a questionable category.
 */
object MerchantCategorizer {
    const val AUTO_APPLY_CONFIDENCE = 0.84

    fun normalize(value: String): String = Normalizer.normalize(value, Normalizer.Form.NFKD)
        .lowercase(Locale.ROOT)
        .replace(EMAIL, " ")
        .replace(Regex("\\p{M}+"), "")
        .replace(Regex("\\d+"), " ")
        .replace(Regex("[^\\p{L}]+"), " ")
        .trim()
        .split(Regex("\\s+"))
        .filter { it.length >= 2 && it !in STOP_WORDS }
        .joinToString(" ")

    fun suggest(
        merchant: String,
        isIncome: Boolean,
        categories: List<CategoryEntity>,
        rules: List<MerchantRuleEntity>,
        history: List<TransactionEntity>,
    ): CategorySuggestion? {
        val normalized = normalize(merchant)
        if (normalized.length < 2) return null
        val available = categories.filter { it.isIncome == isIncome }.associateBy { it.id }
        if (available.isEmpty()) return null

        rules.firstOrNull { it.normalizedMerchant == normalized && it.categoryId in available }
            ?.categoryId
            ?.let { return CategorySuggestion(it, 1.0, CategorySuggestionSource.LEARNED_RULE) }

        val compatibleHistory = history.filter { item ->
            item.categoryId in available && item.type != TransactionType.TRANSFER && item.type != TransactionType.ADJUSTMENT
        }
        val exactHistory = compatibleHistory.filter { normalize(it.merchant) == normalized }
            .groupingBy { it.categoryId!! }
            .eachCount()
            .maxByOrNull { it.value }
        if (exactHistory != null) {
            return CategorySuggestion(exactHistory.key, 0.99, CategorySuggestionSource.HISTORY)
        }

        builtInCategory(normalized, available.values.toList(), isIncome)?.let {
            return CategorySuggestion(it, 0.94, CategorySuggestionSource.BUILT_IN)
        }

        val candidates = buildList {
            rules.forEach { rule ->
                val categoryId = rule.categoryId
                if (categoryId in available) add(categoryId!! to similarity(normalized, rule.normalizedMerchant))
            }
            compatibleHistory.forEach { item ->
                val other = normalize(item.merchant)
                if (other.isNotBlank()) add(item.categoryId!! to similarity(normalized, other))
            }
        }
        val ranked = candidates
            .groupBy({ it.first }, { it.second })
            .mapValues { (_, scores) -> scores.sortedDescending().take(3).average() }
            .entries
            .sortedByDescending { it.value }
        val best = ranked.firstOrNull() ?: return null
        val runnerUp = ranked.getOrNull(1)?.value ?: 0.0
        if (best.value < AUTO_APPLY_CONFIDENCE || best.value - runnerUp < 0.08) return null
        return CategorySuggestion(best.key, best.value, CategorySuggestionSource.HISTORY)
    }

    private fun builtInCategory(normalized: String, categories: List<CategoryEntity>, isIncome: Boolean): Long? {
        val preferred = if (isIncome) {
            if (containsAny(normalized, "salary", "payroll", "wages")) "Salary" else return null
        } else when {
            containsAny(normalized, "swiggy", "zomato", "food delivery") -> "Food delivery"
            containsAny(normalized, "grocery", "groceries", "supermarket", "bigbasket", "blinkit", "zepto") -> "Groceries"
            containsAny(normalized, "restaurant", "cafe", "coffee", "dining") -> "Dining"
            containsAny(normalized, "uber", "ola", "metro", "rail", "flight", "petrol", "fuel") -> "Travel"
            containsAny(normalized, "amazon", "flipkart", "myntra", "shopping") -> "Shopping"
            containsAny(normalized, "netflix", "spotify", "subscription", "prime video") -> "Subscriptions"
            containsAny(normalized, "hospital", "pharma", "medical", "clinic", "doctor") -> "Health"
            containsAny(normalized, "school", "tuition", "course", "university", "education", "book") -> "Education"
            else -> return null
        }
        return categories.firstOrNull { it.name.equals(preferred, ignoreCase = true) }?.id
    }

    private fun containsAny(value: String, vararg needles: String): Boolean = needles.any { value.contains(it) }

    internal fun similarity(first: String, second: String): Double {
        if (first == second) return 1.0
        if (first.length >= 4 && second.length >= 4 && (first.contains(second) || second.contains(first))) return 0.92
        val firstTokens = first.split(' ').filter(String::isNotBlank).toSet()
        val secondTokens = second.split(' ').filter(String::isNotBlank).toSet()
        val union = firstTokens union secondTokens
        val tokenScore = if (union.isEmpty()) 0.0 else (firstTokens intersect secondTokens).size.toDouble() / union.size
        val firstTrigrams = trigrams(first.replace(" ", ""))
        val secondTrigrams = trigrams(second.replace(" ", ""))
        val trigramScore = if (firstTrigrams.isEmpty() || secondTrigrams.isEmpty()) 0.0 else {
            2.0 * (firstTrigrams intersect secondTrigrams).size / (firstTrigrams.size + secondTrigrams.size)
        }
        return 0.45 * tokenScore + 0.55 * trigramScore
    }

    private fun trigrams(value: String): Set<String> = when {
        value.isBlank() -> emptySet()
        value.length < 3 -> setOf(value)
        else -> value.windowed(3).toSet()
    }

    private val EMAIL = Regex("[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+")
    private val STOP_WORDS = setOf(
        "upi", "txn", "transaction", "payment", "paid", "debit", "credit", "bank", "ref", "reference",
        "imps", "neft", "rtgs", "pos", "pvt", "private", "ltd", "limited", "india", "online", "transfer",
    )
}
