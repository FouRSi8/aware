package com.aware.app.sms

import com.aware.app.data.TransactionType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class TransactionSmsParserTest {
    private val parser = TransactionSmsParser()
    private val now = 1_788_600_000_000L

    @Test fun parsesUpiDebit() {
        val parsed = parser.parse("AX-HDFCBK", "Rs.1,048.50 debited from A/c XX1234 paid to SWIGGY via UPI. UTR 123456789012", now)!!
        assertEquals(104850L, parsed.amountPaise)
        assertEquals(TransactionType.EXPENSE, parsed.type)
        assertEquals("1234", parsed.accountSuffix)
        assertTrue(parsed.merchant.contains("SWIGGY", ignoreCase = true))
        assertTrue(parsed.confidence >= .8f)
    }

    @Test fun parsesSalaryCredit() {
        val parsed = parser.parse("VM-BANK", "INR 30,000.00 credited to account XX8877 from ARTEM ART OF CO. Ref SAL202609", now)!!
        assertEquals(3_000_000L, parsed.amountPaise)
        assertEquals(TransactionType.INCOME, parsed.type)
    }

    @Test fun treatsAtmAsTransfer() {
        val parsed = parser.parse("BANK", "INR 7,500 withdrawn from account 1234 at ATM on 04-09-26. Txn ID ATM778899", now)!!
        assertEquals(TransactionType.TRANSFER, parsed.type)
        assertEquals("ATM cash withdrawal", parsed.merchant)
    }

    @Test fun ignoresOtpPromotionsAndFailures() {
        assertNull(parser.parse("BANK", "OTP 123456 for INR 900 purchase. Do not share.", now))
        assertNull(parser.parse("BANK", "Your INR 900 UPI transaction failed and was not debited", now))
        assertNull(parser.parse("SHOP", "Offer! Get INR 500 cashback. Apply now", now))
    }

    @Test fun fingerprintIsStableForDuplicateBody() {
        val sms = "Rs 250 paid to ZOMATO via UPI. Ref 123ABC456"
        assertEquals(parser.parse("BANK", sms, now)!!.fingerprint, parser.parse("BANK", sms, now + 1000)!!.fingerprint)
    }
}

