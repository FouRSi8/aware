package com.aware.app.backup

import com.aware.app.data.AccountEntity
import com.aware.app.data.BudgetBucketEntity
import com.aware.app.data.CategoryEntity
import com.aware.app.data.MerchantRuleEntity
import com.aware.app.data.RecurringRuleEntity
import com.aware.app.data.TransactionEntity
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.security.SecureRandom
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

@Serializable
data class BackupPayload(
    val version: Int = 1,
    val createdAt: Long = System.currentTimeMillis(),
    val accounts: List<AccountEntity>,
    val categories: List<CategoryEntity>,
    val transactions: List<TransactionEntity>,
    val budgets: List<BudgetBucketEntity>,
    val recurring: List<RecurringRuleEntity>,
    val merchantRules: List<MerchantRuleEntity>,
)

object BackupCodec {
    private val json = Json { encodeDefaults = true; ignoreUnknownKeys = true }

    fun encrypt(payload: BackupPayload, password: CharArray): ByteArray {
        require(password.size >= 8) { "Password must contain at least 8 characters" }
        val salt = ByteArray(16).also(SecureRandom()::nextBytes)
        val iv = ByteArray(12).also(SecureRandom()::nextBytes)
        val key = derive(password, salt)
        val encrypted = Cipher.getInstance("AES/GCM/NoPadding").run {
            init(Cipher.ENCRYPT_MODE, key, GCMParameterSpec(128, iv))
            doFinal(json.encodeToString(payload).toByteArray())
        }
        password.fill('\u0000')
        return MAGIC + salt + iv + encrypted
    }

    fun decrypt(bytes: ByteArray, password: CharArray): BackupPayload {
        // Backups written before the rename carry the old magic; keep reading them.
        val header = listOf(MAGIC, LEGACY_MAGIC).firstOrNull { candidate ->
            bytes.size > candidate.size + 28 && bytes.copyOfRange(0, candidate.size).contentEquals(candidate)
        }
        requireNotNull(header) { "Not an aware backup" }
        val salt = bytes.copyOfRange(header.size, header.size + 16)
        val iv = bytes.copyOfRange(header.size + 16, header.size + 28)
        val encrypted = bytes.copyOfRange(header.size + 28, bytes.size)
        val plain = Cipher.getInstance("AES/GCM/NoPadding").run {
            init(Cipher.DECRYPT_MODE, derive(password, salt), GCMParameterSpec(128, iv))
            doFinal(encrypted)
        }
        password.fill('\u0000')
        return json.decodeFromString(String(plain))
    }

    fun csv(transactions: List<TransactionEntity>, accounts: List<AccountEntity>, categories: List<CategoryEntity>): String {
        fun quote(value: String) = "\"${value.replace("\"", "\"\"")}\""
        val accountNames = accounts.associate { it.id to it.name }
        val categoryNames = categories.associate { it.id to it.name }
        return buildString {
            appendLine("date,type,status,amount_inr,merchant,account,destination,category,note,tags,source")
            transactions.sortedByDescending(TransactionEntity::occurredAt).forEach { item ->
                append(java.time.Instant.ofEpochMilli(item.occurredAt).atZone(java.time.ZoneId.systemDefault()).toLocalDate()).append(',')
                append(item.type).append(',').append(item.status).append(',').append("%.2f".format(java.util.Locale.ENGLISH, item.amountPaise / 100.0)).append(',')
                append(quote(item.merchant)).append(',').append(quote(accountNames[item.accountId].orEmpty())).append(',')
                append(quote(accountNames[item.destinationAccountId].orEmpty())).append(',').append(quote(categoryNames[item.categoryId].orEmpty())).append(',')
                append(quote(item.note)).append(',').append(quote(item.tags)).append(',').appendLine(item.source)
            }
        }
    }

    private fun derive(password: CharArray, salt: ByteArray): SecretKeySpec {
        val spec = PBEKeySpec(password, salt, 150_000, 256)
        val bytes = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).encoded
        spec.clearPassword()
        return SecretKeySpec(bytes, "AES")
    }

    private val MAGIC = "AWARE001".toByteArray(Charsets.US_ASCII)
    private val LEGACY_MAGIC = "EXPENDO1".toByteArray(Charsets.US_ASCII)
}
