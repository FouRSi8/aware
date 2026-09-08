package com.aware.app.sms

import com.aware.app.data.TransactionType
import java.math.BigDecimal
import java.math.RoundingMode
import java.security.MessageDigest
import java.util.Locale

data class ParsedSms(
    val sender: String,
    val rawBody: String,
    val amountPaise: Long,
    val type: TransactionType,
    val merchant: String,
    val accountSuffix: String?,
    val reference: String?,
    val confidence: Float,
    val fingerprint: String,
    val receivedAt: Long,
)

class TransactionSmsParser {
    fun parse(sender: String, body: String, receivedAt: Long): ParsedSms? {
        val normalized = body.replace(Regex("\\s+"), " ").trim()
        val lower = normalized.lowercase(Locale.ENGLISH)
        if (lower.length < 12 || BLOCKED.any(lower::contains)) return null
        val type = detectType(lower) ?: return null
        val amount = extractAmount(normalized) ?: return null
        if (amount <= 0L) return null
        val reference = REFERENCE.find(normalized)?.groupValues?.getOrNull(1)?.takeIf(String::isNotBlank)
        val suffix = ACCOUNT.find(normalized)?.groupValues?.getOrNull(1)
        val merchant = extractMerchant(normalized, sender, type)
        var confidence = 0.56f
        if (REFERENCE.containsMatchIn(normalized)) confidence += 0.12f
        if (ACCOUNT.containsMatchIn(normalized)) confidence += 0.08f
        if (PAYMENT_TERMS.any(lower::contains)) confidence += 0.14f
        if (merchant != sender) confidence += 0.08f
        if (type == TransactionType.TRANSFER && lower.contains("atm")) confidence += 0.08f
        confidence = confidence.coerceAtMost(0.99f)
        val fingerprintSource = listOf(sender.lowercase(), reference ?: normalized.lowercase(), amount, type.name).joinToString("|")
        return ParsedSms(
            sender = sender,
            rawBody = normalized,
            amountPaise = amount,
            type = type,
            merchant = merchant,
            accountSuffix = suffix,
            reference = reference,
            confidence = confidence,
            fingerprint = sha256(fingerprintSource),
            receivedAt = receivedAt,
        )
    }

    private fun detectType(lower: String): TransactionType? = when {
        listOf("reversed", "reversal", "refunded", "refund of", "credited back").any(lower::contains) -> TransactionType.REFUND
        (lower.contains("withdrawn") || lower.contains("cash withdrawal")) && lower.contains("atm") -> TransactionType.TRANSFER
        listOf("credited", "received", "salary", "deposited").any(lower::contains) -> TransactionType.INCOME
        listOf("debited", "spent", "paid", "purchase", "sent", "upi txn").any(lower::contains) -> TransactionType.EXPENSE
        else -> null
    }

    private fun extractAmount(body: String): Long? {
        val match = AMOUNT.find(body) ?: return null
        val raw = match.groupValues[1].replace(",", "")
        return runCatching {
            BigDecimal(raw).multiply(BigDecimal(100)).setScale(0, RoundingMode.HALF_UP).longValueExact()
        }.getOrNull()
    }

    private fun extractMerchant(body: String, sender: String, type: TransactionType): String {
        if (type == TransactionType.TRANSFER) return "ATM cash withdrawal"
        val candidate = MERCHANT_PATTERNS.firstNotNullOfOrNull { regex ->
            regex.find(body)?.groupValues?.getOrNull(1)?.trim(' ', '.', ',', '-', ':')
        }?.replace(Regex("\\s+"), " ")
        return candidate?.takeIf { it.length in 2..48 && !it.contains("account", true) } ?: sender
    }

    private fun sha256(value: String): String = MessageDigest.getInstance("SHA-256")
        .digest(value.toByteArray())
        .joinToString("") { "%02x".format(it) }

    companion object {
        private val AMOUNT = Regex("(?i)(?:INR|Rs\\.?|₹)\\s*([0-9][0-9,]*(?:\\.[0-9]{1,2})?)")
        private val REFERENCE = Regex("(?i)(?:ref(?:erence)?(?: no)?|txn(?: id)?|utr)[:#\\s-]*([A-Z0-9]{6,30})")
        private val ACCOUNT = Regex("(?i)(?:a/c|acct|account|card)(?: no)?[\\s:*xX-]*(\\d{3,6})")
        private val MERCHANT_PATTERNS = listOf(
            Regex("(?i)\\b(?:paid|sent)\\s+(?:to\\s+)?([A-Za-z0-9][A-Za-z0-9 .&'_-]{1,47}?)(?:\\s+(?:via|on|using|ref|upi|from)\\b|[.;]|$)"),
            Regex("(?i)\\b(?:at|to)\\s+([A-Za-z0-9][A-Za-z0-9 .&'_-]{1,47}?)(?:\\s+(?:on|via|ref|upi)\\b|[.;]|$)"),
            Regex("(?i)\\binfo[: -]+([A-Za-z0-9][A-Za-z0-9 .&'_-]{1,47}?)(?:[.;]|$)"),
            Regex("(?i)\\bfrom\\s+([A-Za-z0-9][A-Za-z0-9 .&'_-]{1,47}?)(?:\\s+(?:on|ref|upi)\\b|[.;]|$)"),
        )
        private val PAYMENT_TERMS = listOf("upi", "bank", "a/c", "account", "card", "txn", "transaction", "atm")
        private val BLOCKED = listOf(
            "otp", "one time password", "do not share", "failed", "declined", "unsuccessful",
            "offer", "cashback offer", "pre-approved", "apply now", "due reminder", "requesting money",
        )
    }
}

