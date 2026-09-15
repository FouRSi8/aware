package com.aware.app.ai

import com.aware.app.security.SecureStore
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.add
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

data class GroqCategoryInput(
    val rowId: Int,
    val merchant: String,
    val isIncome: Boolean,
)

class GroqCategorySuggester(
    private val secureStore: SecureStore,
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(25, TimeUnit.SECONDS)
        .callTimeout(30, TimeUnit.SECONDS)
        .build(),
) {
    fun configured(): Boolean = !secureStore.readSecret(KEY_NAME).isNullOrBlank()
    fun savedKey(): String = secureStore.readSecret(KEY_NAME).orEmpty()
    fun saveKey(value: String) = secureStore.saveSecret(KEY_NAME, value.trim())

    /** Sends all unresolved rows in one constrained request and validates every
     * result against the locally supplied category set before accepting it. */
    suspend fun suggestBatch(
        rows: List<GroqCategoryInput>,
        expenseCategories: List<String>,
        incomeCategories: List<String>,
    ): Map<Int, String> = withContext(Dispatchers.IO) {
        val apiKey = secureStore.readSecret(KEY_NAME)?.takeIf(String::isNotBlank) ?: return@withContext emptyMap()
        val expenseAllowed = expenseCategories.distinct().take(MAX_CATEGORIES)
        val incomeAllowed = incomeCategories.distinct().take(MAX_CATEGORIES)
        val safeRows = rows.take(MAX_ROWS).mapNotNull { row ->
            redact(row.merchant).takeIf { it.length >= 2 }?.let { row.copy(merchant = it) }
        }.filter { if (it.isIncome) incomeAllowed.isNotEmpty() else expenseAllowed.isNotEmpty() }
        if (safeRows.isEmpty()) return@withContext emptyMap()
        val allAllowed = (expenseAllowed + incomeAllowed).distinct()
        val requestBody = GroqRequest(
            model = MODEL,
            temperature = 0.0,
            maxTokens = (safeRows.size * 24 + 64).coerceAtMost(768),
            messages = listOf(
                GroqMessage(
                    "system",
                    "Categorise each merchant. Use only an exact category from the matching income or expense list. Return one result for every row and no commentary.",
                ),
                GroqMessage(
                    "user",
                    Json.encodeToString(
                        GroqBatchPrompt(
                            expenseCategories = expenseAllowed,
                            incomeCategories = incomeAllowed,
                            rows = safeRows.map { GroqPromptRow(it.rowId, it.merchant, if (it.isIncome) "income" else "expense") },
                        ),
                    ),
                ),
            ),
            responseFormat = responseFormat(allAllowed),
        )
        val request = Request.Builder()
            .url("https://api.groq.com/openai/v1/chat/completions")
            .header("Authorization", "Bearer $apiKey")
            .post(Json.encodeToString(requestBody).toRequestBody(JSON_MEDIA))
            .build()
        runCatching {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@use emptyMap()
                val content = Json.decodeFromString<GroqResponse>(response.body.string())
                    .choices.firstOrNull()?.message?.content?.trim()
                    ?: return@use emptyMap()
                val suggestions = Json.decodeFromString<GroqBatchResult>(content).suggestions
                val inputs = safeRows.associateBy { it.rowId }
                suggestions.mapNotNull { suggestion ->
                    val input = inputs[suggestion.rowId] ?: return@mapNotNull null
                    val allowed = if (input.isIncome) incomeAllowed else expenseAllowed
                    val canonical = allowed.firstOrNull { it.equals(suggestion.category, ignoreCase = true) }
                        ?: return@mapNotNull null
                    suggestion.rowId to canonical
                }.toMap()
            }
        }.getOrDefault(emptyMap())
    }

    private fun redact(merchant: String): String = merchant
        .replace(EMAIL, " ")
        .replace(Regex("\\d"), " ")
        .replace(Regex("[^A-Za-z &'_-]"), " ")
        .replace(Regex("\\s+"), " ")
        .trim()
        .take(48)

    private fun responseFormat(categories: List<String>): JsonObject = buildJsonObject {
        put("type", "json_schema")
        put("json_schema", buildJsonObject {
            put("name", "merchant_categories")
            put("strict", true)
            put("schema", buildJsonObject {
                put("type", "object")
                put("properties", buildJsonObject {
                    put("suggestions", buildJsonObject {
                        put("type", "array")
                        put("items", buildJsonObject {
                            put("type", "object")
                            put("properties", buildJsonObject {
                                put("row_id", buildJsonObject { put("type", "integer") })
                                put("category", buildJsonObject {
                                    put("type", "string")
                                    put("enum", buildJsonArray { categories.forEach(::add) })
                                })
                            })
                            put("required", buildJsonArray { add("row_id"); add("category") })
                            put("additionalProperties", false)
                        })
                    })
                })
                put("required", buildJsonArray { add("suggestions") })
                put("additionalProperties", false)
            })
        })
    }

    companion object {
        private const val KEY_NAME = "groq_api_key"
        private const val MODEL = "openai/gpt-oss-20b"
        private const val MAX_ROWS = 25
        private const val MAX_CATEGORIES = 30
        private val JSON_MEDIA = "application/json; charset=utf-8".toMediaType()
        private val EMAIL = Regex("[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+")
    }
}

@Serializable
private data class GroqRequest(
    val model: String,
    val messages: List<GroqMessage>,
    val temperature: Double,
    @SerialName("max_tokens") val maxTokens: Int,
    @SerialName("response_format") val responseFormat: JsonObject,
)

@Serializable private data class GroqMessage(val role: String, val content: String)
@Serializable private data class GroqResponse(val choices: List<GroqChoice> = emptyList())
@Serializable private data class GroqChoice(val message: GroqMessage)
@Serializable private data class GroqBatchPrompt(
    @SerialName("expense_categories") val expenseCategories: List<String>,
    @SerialName("income_categories") val incomeCategories: List<String>,
    val rows: List<GroqPromptRow>,
)
@Serializable private data class GroqPromptRow(
    @SerialName("row_id") val rowId: Int,
    val merchant: String,
    val kind: String,
)
@Serializable private data class GroqBatchResult(val suggestions: List<GroqBatchSuggestion> = emptyList())
@Serializable private data class GroqBatchSuggestion(
    @SerialName("row_id") val rowId: Int,
    val category: String,
)
