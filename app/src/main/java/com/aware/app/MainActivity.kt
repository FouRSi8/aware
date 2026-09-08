package com.aware.app

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.setContent
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.aware.app.ui.AwareApp
import com.aware.app.ui.MainViewModel
import com.aware.app.ui.theme.AwareTheme
import com.aware.app.ui.theme.Appearance
import com.aware.app.ui.theme.Skin
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.fragment.app.FragmentActivity
import kotlinx.coroutines.launch
import android.widget.Toast
import androidx.glance.appwidget.updateAll

class MainActivity : FragmentActivity() {
    private var unlocked = false
    private var incomingReviewCandidateId by mutableStateOf<Long?>(null)
    private var incomingWidgetTransactionId by mutableStateOf<Long?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        consumeLaunchIntent(intent)
        setContent {
            val app = application as AwareApplication
            val viewModel: MainViewModel = viewModel(factory = MainViewModel.Factory(app.container.repository, app.container.categorySuggester))
            var smsGranted by remember { mutableStateOf(ContextCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS) == PackageManager.PERMISSION_GRANTED) }
            val smsLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { smsGranted = it }
            val notificationLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
                if (!granted) viewModel.setSmartNudges(false)
            }
            val scope = rememberCoroutineScope()
            var csvBytes by remember { mutableStateOf<ByteArray?>(null) }
            var backupBytes by remember { mutableStateOf<ByteArray?>(null) }
            var restoreBytes by remember { mutableStateOf<ByteArray?>(null) }
            val csvLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("text/csv")) { uri ->
                uri?.let { target -> csvBytes?.let { contentResolver.openOutputStream(target)?.use { out -> out.write(it) } } }
                csvBytes = null
            }
            val backupLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/octet-stream")) { uri ->
                uri?.let { target -> backupBytes?.let { contentResolver.openOutputStream(target)?.use { out -> out.write(it) } } }
                backupBytes = null
            }
            val restoreLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
                restoreBytes = uri?.let { contentResolver.openInputStream(it)?.use { input -> input.readBytes() } }
            }
            val appearancePrefs = remember { getSharedPreferences("appearance", MODE_PRIVATE) }
            // Falls back to the pre-key preference so an upgrade keeps the look
            // the user already chose.
            var appearance by remember {
                mutableStateOf(
                    Appearance.fromKey(
                        appearancePrefs.getString("mode_key", null) ?: appearancePrefs.getString("mode", null),
                    ),
                )
            }
            var skin by remember {
                mutableStateOf(
                    Skin.fromKey(
                        appearancePrefs.getString("skin_key", null) ?: appearancePrefs.getString("skin", null),
                    ),
                )
            }
            val dark = when (appearance) {
                Appearance.SYSTEM -> isSystemInDarkTheme()
                Appearance.LIGHT -> false
                Appearance.DARK -> true
            }
            AwareTheme(skin = skin, darkTheme = dark) {
                AwareApp(
                    appearance = appearance,
                    onAppearanceChange = {
                        appearance = it
                        appearancePrefs.edit().putString("mode_key", it.key).apply()
                        scope.launch { com.aware.app.widget.AwareWidget().updateAll(this@MainActivity) }
                    },
                    skin = skin,
                    onSkinChange = { next ->
                        skin = next
                        val editor = appearancePrefs.edit().putString("skin_key", next.key)
                        // The maximal skin is designed dark-first, so landing on
                        // it drops into dark unless the user has already pinned a
                        // brightness of their own. The light switch still works.
                        if (next == Skin.MAXIMAL && appearance == Appearance.SYSTEM) {
                            appearance = Appearance.DARK
                            editor.putString("mode_key", Appearance.DARK.key)
                        }
                        editor.apply()
                    },
                    viewModel = viewModel,
                    widgetTransactionId = incomingWidgetTransactionId,
                    onWidgetTransactionHandled = { incomingWidgetTransactionId = null },
                    smsGranted = smsGranted,
                    onRequestSms = {
                        if (smsGranted) {
                            startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:$packageName")))
                        } else {
                            smsLauncher.launch(Manifest.permission.RECEIVE_SMS)
                        }
                    },
                    onRequestNotifications = { if (android.os.Build.VERSION.SDK_INT >= 33) notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS) },
                    onExportCsv = {
                        scope.launch {
                            csvBytes = app.container.repository.createCsv().toByteArray()
                            csvLauncher.launch("aware-transactions.csv")
                        }
                    },
                    onCreateBackup = { password ->
                        scope.launch {
                            runCatching { app.container.repository.createEncryptedBackup(password.toCharArray()) }
                                .onSuccess { backupBytes = it; backupLauncher.launch("aware-backup.aware") }
                                .onFailure { Toast.makeText(this@MainActivity, it.message ?: "Backup failed", Toast.LENGTH_LONG).show() }
                        }
                    },
                    onChooseRestore = { restoreLauncher.launch(arrayOf("application/octet-stream", "*/*")) },
                    restoreReady = restoreBytes != null,
                    onRestore = { password ->
                        val bytes = restoreBytes ?: return@AwareApp
                        scope.launch {
                            runCatching { app.container.repository.restoreEncryptedBackup(bytes, password.toCharArray()) }
                                .onSuccess { restoreBytes = null; Toast.makeText(this@MainActivity, "aware restored", Toast.LENGTH_SHORT).show() }
                                .onFailure { Toast.makeText(this@MainActivity, "Wrong password or invalid backup", Toast.LENGTH_LONG).show() }
                        }
                    },
                )
            }
            LaunchedEffect(incomingReviewCandidateId) {
                incomingReviewCandidateId?.let {
                    viewModel.openReview(it)
                    incomingReviewCandidateId = null
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        consumeLaunchIntent(intent)
    }

    private fun consumeLaunchIntent(intent: Intent?) {
        incomingReviewCandidateId = intent?.getLongExtra("reviewCandidateId", -1L)?.takeIf { it > 0 }
        incomingWidgetTransactionId = intent?.getLongExtra("transactionId", -1L)?.takeIf { it > 0 }
    }

    override fun onResume() {
        super.onResume()
        val app = application as? AwareApplication ?: return
        if (!app.container.secureStore.getBoolean("app_lock") || unlocked) return
        val authenticators = BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL
        if (BiometricManager.from(this).canAuthenticate(authenticators) != BiometricManager.BIOMETRIC_SUCCESS) return
        BiometricPrompt(this, ContextCompat.getMainExecutor(this), object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) { unlocked = true }
            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) { if (!isFinishing) finish() }
        }).authenticate(
            BiometricPrompt.PromptInfo.Builder()
                .setTitle("Unlock aware")
                .setSubtitle("Your money story is private")
                .setAllowedAuthenticators(authenticators)
                .build(),
        )
    }

}
