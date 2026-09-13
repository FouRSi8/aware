package com.aware.app.data

import androidx.room.testing.MigrationTestHelper
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.aware.app.MIGRATION_5_6
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CaptureRemovalMigrationTest {
    @get:Rule
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        AppDatabase::class.java,
    )

    @Test
    fun migrationRemovesCaptureCachesAndPreservesTheLedger() {
        helper.createDatabase(DATABASE_NAME, 5).use { database ->
            database.execSQL("INSERT INTO accounts (id, name, kind, openingBalancePaise, currency, isDefault, createdAt) VALUES (1, 'Main', 'BANK', 0, 'INR', 1, 0)")
            database.execSQL("INSERT INTO transactions (id, amountPaise, currency, type, status, source, accountId, destinationAccountId, categoryId, merchant, note, tags, occurredAt, createdAt, sourceFingerprint, incomeKind, linkedTransactionId) VALUES (1, 12500, 'INR', 'EXPENSE', 'POSTED', 'MANUAL', 1, NULL, NULL, 'Fixture', '', '', 0, 0, NULL, NULL, NULL)")
            database.execSQL("INSERT INTO capture_candidates (id, sender, encryptedBody, amountPaise, currency, type, merchant, accountSuffix, reference, confidence, fingerprint, receivedAt, source, status) VALUES (1, 'BANK', 'encrypted', 12500, 'INR', 'EXPENSE', 'Fixture', NULL, NULL, 0.9, 'capture', 0, 'SMS', 'PENDING_REVIEW')")
            database.execSQL("INSERT INTO weekly_reports (id, weekStart, weekEndExclusive, generatedAt, incomePaise, refundPaise, spendingPaise, transferPaise, topMerchant) VALUES (1, 0, 1, 1, 0, 0, 12500, 0, 'Fixture')")
        }

        helper.runMigrationsAndValidate(DATABASE_NAME, 6, true, MIGRATION_5_6).use { database ->
            database.query("SELECT amountPaise, merchant FROM transactions WHERE id = 1").use { cursor ->
                cursor.moveToFirst()
                assertEquals(12_500, cursor.getLong(0))
                assertEquals("Fixture", cursor.getString(1))
            }
            database.query("SELECT COUNT(*) FROM sqlite_master WHERE type = 'table' AND name IN ('capture_candidates', 'weekly_reports')").use { cursor ->
                cursor.moveToFirst()
                assertEquals(0, cursor.getInt(0))
            }
        }
    }

    private companion object {
        const val DATABASE_NAME = "capture-removal-migration-test"
    }
}
