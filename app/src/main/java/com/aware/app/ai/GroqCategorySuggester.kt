package com.aware.app.ai

import com.aware.app.security.SecureStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

class GroqCategorySuggester(
    private val secureStore: SecureStore,
    private val client: OkHttpClient = OkHttpClient(),
) {
    fun configured(): Boolean = !secureStore.readSecret(KEY_NAME).isNullOrBlank()
    fun saveKey(value: String) = secureStore.saveSecret(KEY_NAME, value.trim())

    suspend fun suggest(merchant: String, categories: List<String>): String? = withContext(Dispatchers.IO) {
        val apiKey = secureStore.readSecret(KEY_NAME)?.takeIf(String::isNotBlank) ?: return@withContext null
        val redacted = merchant
            .replace(Regex("[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+"), " ")
            .replace(Regex("\\d"), " ")
            .replace(Regex("[^A-Za-z &'_-]"), " ")
            .replace(Regex("\\s+"), " ")
            .trim()
            .take(48)
        if (redacted.length < 2) return@withContext null
        val allowed = categories.distinct().take(30)
        val requestBody = GroqRequest(
            model = "openai/gpt-oss-20b",
            temperature = 0.0,
            maxTokens = 20,
            messages = listOf(
                GroqMessage("system", "Choose exactly one category from the supplied list. Reply with only its exact text."),
                GroqMessage("user", "Merchant: $redacted\nCategories: ${allowed.joinToString(" | ")}"),
            ),
        )
        val request = Request.Builder()
            .url("https://api.groq.com/openai/v1/chat/completions")
            .header("Authorization", "Bearer $apiKey")
            .post(Json.encodeToString(requestBody).toRequestBody(JSON_MEDIA))
            .build()
        runCatching {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@use null
                val content = Json.decodeFromString<GroqResponse>(response.body.string()).choices.firstOrNull()?.message?.content?.trim()
                allowed.firstOrNull { it.equals(content, ignoreCase = true) }
            }
        }.getOrNull()
    }

    companion object {
        private const val KEY_NAME = "groq_api_key"
        private val JSON_MEDIA = "application/json; charset=utf-8".toMediaType()
    }
}

@Serializable private data class GroqRequest(
    val model: String,
    val messages: List<GroqMessage>,
    val temperature: Double,
    @kotlinx.serialization.SerialName("max_tokens") val maxTokens: Int,
)
@Serializable private data class GroqMessage(val role: String, val content: String)
@Serializable private data class GroqResponse(val choices: List<GroqChoice> = emptyList())
@Serializable private data class GroqChoice(val message: GroqMessage)

