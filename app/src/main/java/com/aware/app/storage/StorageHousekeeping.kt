package com.aware.app.storage

import android.content.Context
import java.io.File
import java.util.Locale

data class StorageSnapshot(
    val ledgerBytes: Long,
    val temporaryBytes: Long,
) {
    val totalBytes: Long get() = ledgerBytes + temporaryBytes
}

fun readStorageSnapshot(context: Context): StorageSnapshot {
    val database = context.getDatabasePath("aware.db")
    val ledger = listOf(database, File(database.path + "-wal"), File(database.path + "-shm"))
        .sumOf { it.length().coerceAtLeast(0L) }
    val temporary = listOfNotNull(context.cacheDir, context.externalCacheDir).sumOf(::fileTreeSize)
    return StorageSnapshot(ledger, temporary)
}

/** Clears only app-owned cache directories. The encrypted Room database is never touched. */
fun clearTemporaryStorage(context: Context): Long {
    val roots = listOfNotNull(context.cacheDir, context.externalCacheDir)
    val before = roots.sumOf(::fileTreeSize)
    roots.forEach(::deleteChildren)
    return before - roots.sumOf(::fileTreeSize)
}

fun formatStorageSize(bytes: Long): String {
    if (bytes < 1024) return "$bytes B"
    val units = arrayOf("KB", "MB", "GB")
    var value = bytes.toDouble()
    var unit = -1
    while (value >= 1024 && unit < units.lastIndex) {
        value /= 1024
        unit++
    }
    return String.format(Locale.ENGLISH, if (value >= 10) "%.0f %s" else "%.1f %s", value, units[unit])
}

private fun fileTreeSize(file: File): Long = when {
    !file.exists() -> 0L
    file.isFile -> file.length()
    else -> file.listFiles()?.sumOf(::fileTreeSize) ?: 0L
}

private fun deleteChildren(root: File) {
    root.listFiles()?.forEach { child ->
        if (child.isDirectory) deleteChildren(child)
        child.delete()
    }
}
