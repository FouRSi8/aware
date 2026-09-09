package com.aware.app

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.Settings
import android.view.WindowManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.setContent
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.aware.app.ui.AwareApp
import com.aware.app.ui.MainViewModel
import com.aware.app.ui.theme.AwareTheme
import com.aware.app.ui.theme.Appearance
import com.aware.app.ui.theme.CozyPalette
import com.aware.app.ui.theme.Skin
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.fragment.app.FragmentActivity
import kotlinx.coroutines.launch
import android.widget.Toast
import androidx.glance.appwidget.updateAll

class MainActivity : FragmentActivity() {
    private var unlocked by mutableStateOf(false)
    private var authenticationShowing = false
    private var incomingReviewCandidateId by mutableStateOf<Long?>(null)
    private var incomingWidgetTransactionId by mutableStateOf<Long?>(null)
    private var paymentNotificationAccessGranted by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val app = application as AwareApplication
        val lockEnabled = app.container.secureStore.getBoolean("app_lock")
        unlocked = !lockEnabled
        if (lockEnabled) window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
        consumeLaunchIntent(intent)
        refreshPaymentNotificationAccess()
        setContent {
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
            var cozyPalette by remember {
                mutableStateOf(CozyPalette.fromKey(appearancePrefs.getString("cozy_palette_key", null)))
            }
            val dark = when (appearance) {
                Appearance.SYSTEM -> isSystemInDarkTheme()
                Appearance.LIGHT -> false
                Appearance.DARK -> true
            }
            val baseDensity = LocalDensity.current
            val comfortableDensity = remember(baseDensity.density, baseDensity.fontScale) {
                Density(baseDensity.density * 1.08f, baseDensity.fontScale)
            }
            CompositionLocalProvider(LocalDensity provides comfortableDensity) {
                AwareTheme(skin = skin, cozyPalette = cozyPalette, darkTheme = dark) {
                    if (unlocked) AwareApp(
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
                        scope.launch { com.aware.app.widget.AwareWidget().updateAll(this@MainActivity) }
                    },
                    cozyPalette = cozyPalette,
                    onCozyPaletteChange = { next ->
                        cozyPalette = next
                        appearancePrefs.edit().putString("cozy_palette_key", next.key).apply()
                        scope.launch { com.aware.app.widget.AwareWidget().updateAll(this@MainActivity) }
                    },
                    viewModel = viewModel,
                    widgetTransactionId = incomingWidgetTransactionId,
                    onWidgetTransactionHandled = { incomingWidgetTransactionId = null },
                    smsGranted = smsGranted,
                    paymentNotificationAccessGranted = paymentNotificationAccessGranted,
                    onRequestSms = {
                        if (smsGranted) {
                            startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:$packageName")))
                        } else {
                            smsLauncher.launch(Manifest.permission.RECEIVE_SMS)
                        }
                    },
                    onRequestNotifications = { if (android.os.Build.VERSION.SDK_INT >= 33) notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS) },
                    onRequestPaymentNotificationAccess = {
                        val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
                        runCatching { startActivity(intent) }
                            .onFailure {
                                startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:$packageName")))
                            }
                    },
                    onRefreshWidget = { scope.launch { com.aware.app.widget.AwareWidget().updateAll(this@MainActivity) } },
                    onAppLockChange = { enabled ->
                        if (enabled) {
                            window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
                        } else {
                            window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
                            unlocked = true
                        }
                    },
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
                    ) else PrivateLockScreen(onUnlock = ::requestUnlock)
                }
            }
            LaunchedEffect(incomingReviewCandidateId, unlocked) {
                if (!unlocked) return@LaunchedEffect
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
        refreshPaymentNotificationAccess()
        val app = application as? AwareApplication ?: return
        val lockEnabled = app.container.secureStore.getBoolean("app_lock")
        if (!lockEnabled) {
            window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
            unlocked = true
            return
        }
        window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
        if (unlocked) return
        requestUnlock()
    }

    private fun refreshPaymentNotificationAccess() {
        paymentNotificationAccessGranted = NotificationManagerCompat.getEnabledListenerPackages(this).contains(packageName)
    }

    override fun onStop() {
        super.onStop()
        val app = application as? AwareApplication ?: return
        if (app.container.secureStore.getBoolean("app_lock") && !isChangingConfigurations) {
            unlocked = false
        }
    }

    private fun requestUnlock() {
        if (authenticationShowing || unlocked || isFinishing) return
        val authenticators = BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL
        if (BiometricManager.from(this).canAuthenticate(authenticators) != BiometricManager.BIOMETRIC_SUCCESS) return
        authenticationShowing = true
        BiometricPrompt(this, ContextCompat.getMainExecutor(this), object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                authenticationShowing = false
                unlocked = true
            }
            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                authenticationShowing = false
            }
            override fun onAuthenticationFailed() {
                // The system prompt stays open after a rejected biometric.
                // Keep the guard set so a second prompt cannot be launched on top.
            }
        }).authenticate(
            BiometricPrompt.PromptInfo.Builder()
                .setTitle("Unlock aware")
                .setSubtitle("Your money story is private")
                .setAllowedAuthenticators(authenticators)
                .build(),
        )
    }

}

@Composable
private fun PrivateLockScreen(onUnlock: () -> Unit) {
    Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Box(Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Icon(
                    Icons.Default.Lock,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(10.dp),
                )
                Spacer(Modifier.height(18.dp))
                Text("aware is locked", style = MaterialTheme.typography.headlineMedium)
                Spacer(Modifier.height(8.dp))
                Text(
                    "Your financial data stays hidden until you unlock.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 15.sp,
                )
                Spacer(Modifier.height(24.dp))
                Button(onClick = onUnlock) { Text("Unlock") }
            }
        }
    }
}
