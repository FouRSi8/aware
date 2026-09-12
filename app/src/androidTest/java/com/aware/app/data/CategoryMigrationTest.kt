package com.aware.app.data

import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.aware.app.MIGRATION_3_4
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CategoryMigrationTest {
    @get:Rule
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        AppDatabase::class.java,
    )

    @Test
    fun migrationSplitsCategoriesAndAllowsSameNameAcrossKinds() {
        helper.createDatabase(DATABASE_NAME, 3).use { database ->
            database.execSQL("INSERT INTO categories (id, name, emoji, colorArgb, isIncome, isArchived) VALUES (1, 'Family', 'F', 1, 0, 0)")
            database.execSQL("INSERT INTO categories (id, name, emoji, colorArgb, isIncome, isArchived) VALUES (2, 'Salary', 'S', 2, 1, 0)")
        }

        helper.runMigrationsAndValidate(DATABASE_NAME, 4, true, MIGRATION_3_4).use { database ->
            assertEquals(1, database.count("expense_categories", "Family"))
            assertEquals(1, database.count("income_categories", "Salary"))

            database.execSQL("INSERT INTO income_categories (id, name, emoji, colorArgb, isArchived) VALUES (3, 'Family', 'F', 3, 0)")
            assertEquals(1, database.count("income_categories", "Family"))
        }
    }

    private fun SupportSQLiteDatabase.count(table: String, name: String): Int =
        query("SELECT COUNT(*) FROM $table WHERE name = ?", arrayOf(name)).use { cursor ->
            cursor.moveToFirst()
            cursor.getInt(0)
        }

    private companion object {
        const val DATABASE_NAME = "category-migration-test"
    }
}
