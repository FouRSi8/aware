package com.aware.app.notifications

import com.aware.app.sms.ParsedSms
import com.aware.app.sms.TransactionSmsParser
import java.security.MessageDigest
import java.util.Locale

class PaymentNotificationParser {
    fun parse(
        packageName: String,
        notificationKey: String,
        textParts: List<CharSequence?>,
        postedAt: Long,
    ): ParsedSms? {
        val provider = PROVIDERS[packageName] ?: return null
        val body = textParts.asSequence()
            .mapNotNull { it?.toString()?.replace(Regex("\\s+"), " ")?.trim() }
            .filter(String::isNotBlank)
            .distinct()
            .joinToString(". ")
        if (body.isBlank()) return null
        val lower = body.lowercase(Locale.ENGLISH)
        if (REJECTED.any(lower::contains) || CONFIRMATION.none(lower::contains)) return null
        val parsed = TransactionSmsParser().parse(provider, body, postedAt) ?: return null
        return parsed.copy(fingerprint = sha256("notification|$packageName|$notificationKey|${parsed.amountPaise}|${parsed.type}"))
    }

    private fun sha256(value: String): String = MessageDigest.getInstance("SHA-256")
        .digest(value.toByteArray())
        .joinToString("") { "%02x".format(it) }

    companion object {
        val PROVIDERS = mapOf(
            "com.google.android.apps.nbu.paisa.user" to "Google Pay",
            "money.super.payments" to "super.money",
        )
        private val CONFIRMATION = listOf("paid", "payment", "sent", "received", "credited", "debited", "refund", "reversal")
        private val REJECTED = listOf(
            "failed", "declined", "unsuccessful", "pending", "processing", "payment request", "requested money",
            "reminder", "offer", "win up to", "pre-approved",
        )
    }
}
