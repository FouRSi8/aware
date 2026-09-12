package com.aware.app.data

import androidx.room.testing.MigrationTestHelper
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.aware.app.MIGRATION_4_5
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SavingsMigrationTest {
    @get:Rule
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        AppDatabase::class.java,
    )

    @Test
    fun migrationAddsSavingsControlsWithoutInventingPersonalAmounts() {
        helper.createDatabase(DATABASE_NAME, 4).use { database ->
            database.execSQL("INSERT INTO income_categories (id, name, emoji, colorArgb, isArchived) VALUES (1, 'Salary', 'S', 1, 0)")
            database.execSQL("INSERT INTO expense_categories (id, name, emoji, colorArgb, isArchived) VALUES (2, 'Family', 'F', 2, 0)")
            database.execSQL("INSERT INTO accounts (id, name, kind, openingBalancePaise, currency, isDefault, createdAt) VALUES (1, 'Main', 'BANK', 0, 'INR', 1, 0)")
            database.execSQL("INSERT INTO transactions (id, amountPaise, currency, type, status, source, accountId, destinationAccountId, categoryId, merchant, note, tags, occurredAt, createdAt, sourceFingerprint) VALUES (1, 1500000, 'INR', 'INCOME', 'POSTED', 'MANUAL', 1, NULL, 1, 'Employer', '', '', 0, 0, NULL)")
        }

        helper.runMigrationsAndValidate(DATABASE_NAME, 5, true, MIGRATION_4_5).use { database ->
            database.query("SELECT incomeKind, linkedTransactionId FROM transactions WHERE id = 1").use { cursor ->
                cursor.moveToFirst()
                assertEquals("SALARY", cursor.getString(0))
                assertNull(cursor.getString(1))
            }
            database.query("SELECT expenseNature FROM expense_categories WHERE id = 2").use { cursor ->
                cursor.moveToFirst()
                assertEquals("COMMITMENT", cursor.getString(0))
            }
            database.query("SELECT COUNT(*) FROM monthly_plans").use { cursor ->
                cursor.moveToFirst()
                assertEquals(0, cursor.getInt(0))
            }
        }
    }

    private companion object {
        const val DATABASE_NAME = "savings-migration-test"
    }
}
