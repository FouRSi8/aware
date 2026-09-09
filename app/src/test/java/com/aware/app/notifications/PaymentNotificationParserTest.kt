package com.aware.app.notifications

import com.aware.app.data.TransactionType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class PaymentNotificationParserTest {
    private val parser = PaymentNotificationParser()

    @Test fun parsesGooglePayPayment() {
        val result = parser.parse(
            "com.google.android.apps.nbu.paisa.user",
            "gpay:42",
            listOf("Payment successful", "Paid ₹1,248.50 to Corner Cafe via UPI"),
            1_700_000_000_000,
        )!!
        assertEquals(124_850L, result.amountPaise)
        assertEquals(TransactionType.EXPENSE, result.type)
        assertEquals("Corner Cafe", result.merchant)
    }

    @Test fun parsesSuperMoneyReceipt() {
        val result = parser.parse(
            "money.super.payments",
            "super:7",
            listOf("Money received", "₹500 received from Riya"),
            1_700_000_000_000,
        )!!
        assertEquals(50_000L, result.amountPaise)
        assertEquals(TransactionType.INCOME, result.type)
    }

    @Test fun rejectsFailurePromotionAndUnknownApps() {
        assertNull(parser.parse("com.google.android.apps.nbu.paisa.user", "1", listOf("Payment of ₹100 failed"), 1))
        assertNull(parser.parse("money.super.payments", "2", listOf("Offer: win ₹500 cashback"), 1))
        assertNull(parser.parse("com.example.fake", "3", listOf("Paid ₹100 to Shop"), 1))
    }
}
