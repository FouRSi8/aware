package com.aware.app.update

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.aware.app.BuildConfig
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.OkHttpClient
import okhttp3.Request

data class AppRelease(
    val version: String,
    val title: String,
    val notes: String,
    val apkUrl: String,
    val pageUrl: String,
)

sealed interface UpdateCheckResult {
    data class Available(val release: AppRelease) : UpdateCheckResult
    data class Current(val version: String = BuildConfig.VERSION_NAME) : UpdateCheckResult
}

object AppUpdater {
    private const val LATEST_RELEASE = "https://api.github.com/repos/FouRSi8/aware/releases/latest"
    private val client = OkHttpClient()
    private val json = Json { ignoreUnknownKeys = true }

    suspend fun check(): UpdateCheckResult = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url(LATEST_RELEASE)
            .header("Accept", "application/vnd.github+json")
            .header("X-GitHub-Api-Version", "2022-11-28")
            .header("User-Agent", "aware-android/${BuildConfig.VERSION_NAME}")
            .build()
        client.newCall(request).execute().use { response ->
            check(response.isSuccessful) { "GitHub returned ${response.code}" }
            val root = json.parseToJsonElement(checkNotNull(response.body).string()).jsonObject
            val tag = root.getValue("tag_name").jsonPrimitive.content
            val version = tag.removePrefix("v")
            val apk = root.getValue("assets").jsonArray
                .map { it.jsonObject }
                .firstOrNull { it["name"]?.jsonPrimitive?.content?.endsWith(".apk", ignoreCase = true) == true }
                ?: error("This release does not include an APK")
            if (compareVersions(version, BuildConfig.VERSION_NAME) <= 0) {
                UpdateCheckResult.Current()
            } else {
                UpdateCheckResult.Available(
                    AppRelease(
                        version = version,
                        title = root["name"]?.jsonPrimitive?.content ?: "aware $tag",
                        notes = root["body"]?.jsonPrimitive?.content.orEmpty(),
                        apkUrl = apk.getValue("browser_download_url").jsonPrimitive.content,
                        pageUrl = root.getValue("html_url").jsonPrimitive.content,
                    ),
                )
            }
        }
    }

    suspend fun download(context: Context, release: AppRelease): File = withContext(Dispatchers.IO) {
        val directory = File(context.externalCacheDir ?: context.cacheDir, "updates").apply { mkdirs() }
        val destination = File(directory, "aware-${release.version}.apk")
        val temporary = File(directory, "${destination.name}.download")
        val request = Request.Builder().url(release.apkUrl).header("User-Agent", "aware-android/${BuildConfig.VERSION_NAME}").build()
        client.newCall(request).execute().use { response ->
            check(response.isSuccessful) { "Download failed (${response.code})" }
            checkNotNull(response.body).byteStream().use { input ->
                temporary.outputStream().use { output -> input.copyTo(output) }
            }
        }
        check(temporary.length() > 0) { "The downloaded APK was empty" }
        if (destination.exists()) destination.delete()
        check(temporary.renameTo(destination)) { "Could not prepare the downloaded update" }
        destination
    }

    fun installIntent(context: Context, apk: File): Intent {
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.updates", apk)
        return Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/vnd.android.package-archive")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    }

    internal fun compareVersions(left: String, right: String): Int {
        val a = left.substringBefore('-').split('.').map { it.toIntOrNull() ?: 0 }
        val b = right.substringBefore('-').split('.').map { it.toIntOrNull() ?: 0 }
        repeat(maxOf(a.size, b.size)) { index ->
            val comparison = (a.getOrElse(index) { 0 }).compareTo(b.getOrElse(index) { 0 })
            if (comparison != 0) return comparison
        }
        return 0
    }
}
