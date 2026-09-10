package com.aware.app.statement

import com.aware.app.data.TransactionType
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.apache.poi.openxml4j.opc.OPCPackage
import org.apache.poi.poifs.crypt.EncryptionInfo
import org.apache.poi.poifs.crypt.EncryptionMode
import org.apache.poi.poifs.filesystem.POIFSFileSystem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.ByteArrayOutputStream
import java.io.ByteArrayInputStream
import java.time.LocalDate
import java.time.ZoneId

class StatementImportParserTest {
    private val zone = ZoneId.of("Asia/Kolkata")
    private val parser = StatementImportParser(zone)

    @Test fun parsesIndianDebitAndCreditColumns() {
        val csv = """
            Account statement
            Transaction Date,Narration,Withdrawal,Deposit,Balance,UTR
            04/09/2026,"UPI, SWIGGY","1,048.50",,"18,951.50",ABC12345
            05/09/2026,Salary,,30000.00,"48,951.50",SAL99887
        """.trimIndent()

        val result = parser.parse("statement.csv", csv.toByteArray())

        assertEquals(2, result.candidates.size)
        assertEquals(104_850L, result.candidates[0].amountPaise)
        assertEquals(TransactionType.EXPENSE, result.candidates[0].type)
        assertEquals(3_000_000L, result.candidates[1].amountPaise)
        assertEquals(TransactionType.INCOME, result.candidates[1].type)
    }

    @Test fun usesDirectionColumnAndDetectsRefund() {
        val csv = """
            Date,Description,Amount,Dr/Cr
            2026-09-04,Book store,899.00,DR
            2026-09-05,Book store refund,899.00,CR
        """.trimIndent()

        val rows = parser.parse("statement.csv", csv.toByteArray()).candidates

        assertEquals(TransactionType.EXPENSE, rows[0].type)
        assertEquals(TransactionType.REFUND, rows[1].type)
        assertTrue(rows.all { it.directionVerified })
    }

    @Test fun readsAmountSuffixesAndTwoDigitDates() {
        val csv = """
            Txn Date,Transaction Details,Amount
            04-Sep-26,Coffee,250.00 DR
            05-Sep-26,Reversal,250.00 CR
        """.trimIndent()

        val rows = parser.parse("statement.csv", csv.toByteArray()).candidates

        assertEquals(TransactionType.EXPENSE, rows[0].type)
        assertEquals(TransactionType.REFUND, rows[1].type)
        assertTrue(rows.all { it.directionVerified })
    }

    @Test fun reconcilesSingleAmountAgainstRunningBalance() {
        val csv = """
            Date,Particulars,Transaction Amount,Balance
            01/09/2026,Opening context,100.00,1000.00
            02/09/2026,Coffee,100.00,900.00
            03/09/2026,Money received,250.00,1150.00
        """.trimIndent()

        val result = parser.parse("statement.csv", csv.toByteArray())

        assertFalse(result.candidates[0].directionVerified)
        assertEquals(TransactionType.EXPENSE, result.candidates[1].type)
        assertEquals(TransactionType.INCOME, result.candidates[2].type)
        assertEquals(2, result.balanceChecks)
        assertEquals(2, result.balancedMatches)
    }

    @Test fun keepsRepeatedPaymentsDistinctButStable() {
        val csv = """
            Date,Description,Debit,Credit
            04/09/2026,Metro,50.00,
            04/09/2026,Metro,50.00,
        """.trimIndent()

        val first = parser.parse("statement.csv", csv.toByteArray()).candidates
        val second = parser.parse("renamed.csv", csv.toByteArray()).candidates

        assertNotEquals(first[0].fingerprint, first[1].fingerprint)
        assertEquals(first.map { it.fingerprint }, second.map { it.fingerprint })
    }

    @Test fun readsXlsxAndWipesProvidedPasswordArray() {
        val bytes = sampleXlsx()
        val password = "unused".toCharArray()

        val result = parser.parse("statement.xlsx", bytes, password)

        assertEquals(12_345L, result.candidates.single().amountPaise)
        assertTrue(password.all { it == '\u0000' })
        assertEquals(
            LocalDate.of(2026, 9, 4),
            java.time.Instant.ofEpochMilli(result.candidates.single().occurredAt).atZone(zone).toLocalDate(),
        )
    }

    @Test fun requiresAndVerifiesEncryptedXlsxPassword() {
        val encrypted = encryptXlsx(sampleXlsx(), "monthly-secret")

        try {
            parser.parse("protected.xlsx", encrypted)
            throw AssertionError("Expected a password prompt")
        } catch (_: StatementPasswordRequiredException) {
            // Expected.
        }
        val wrong = "wrong".toCharArray()
        try {
            parser.parse("protected.xlsx", encrypted, wrong)
            throw AssertionError("Expected password rejection")
        } catch (_: IncorrectStatementPasswordException) {
            assertTrue(wrong.all { it == '\u0000' })
        }
        val correct = "monthly-secret".toCharArray()
        val result = parser.parse("protected.xlsx", encrypted, correct)

        assertEquals(12_345L, result.candidates.single().amountPaise)
        assertTrue(correct.all { it == '\u0000' })
    }

    private fun sampleXlsx(): ByteArray = ByteArrayOutputStream().use { output ->
        XSSFWorkbook().use { workbook ->
            val sheet = workbook.createSheet("Transactions")
            listOf(
                listOf("Date", "Description", "Debit", "Credit"),
                listOf("04/09/2026", "Groceries", "123.45", ""),
            ).forEachIndexed { rowIndex, values ->
                val row = sheet.createRow(rowIndex)
                values.forEachIndexed { column, value -> row.createCell(column).setCellValue(value) }
            }
            workbook.write(output)
        }
        output.toByteArray()
    }

    private fun encryptXlsx(plain: ByteArray, password: String): ByteArray = POIFSFileSystem().use { fileSystem ->
        val info = EncryptionInfo(EncryptionMode.agile)
        val encryptor = info.encryptor.apply { confirmPassword(password) }
        OPCPackage.open(ByteArrayInputStream(plain)).use { packageFile ->
            encryptor.getDataStream(fileSystem).use(packageFile::save)
        }
        ByteArrayOutputStream().use { output ->
            fileSystem.writeFilesystem(output)
            output.toByteArray()
        }
    }
}
