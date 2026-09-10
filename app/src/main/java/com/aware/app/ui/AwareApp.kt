package com.aware.app.ui

import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoGraph
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.ArrowOutward
import androidx.compose.material.icons.filled.SouthWest
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Contrast
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.CurrencyRupee
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import com.aware.app.BuildConfig
import com.aware.app.update.AppRelease
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.aware.app.data.AccountEntity
import com.aware.app.data.AccountKind
import com.aware.app.data.BudgetBucketEntity
import com.aware.app.data.BudgetPeriod
import com.aware.app.data.BudgetScope
import com.aware.app.data.CaptureCandidateEntity
import com.aware.app.data.CategoryEntity
import com.aware.app.data.RecurrenceCadence
import com.aware.app.data.RecurringRuleEntity
import com.aware.app.data.TransactionEntity
import com.aware.app.data.TransactionSource
import com.aware.app.data.TransactionType
import com.aware.app.ui.theme.Appearance
import com.aware.app.ui.theme.CozyPalette
import com.aware.app.ui.theme.LocalTokens
import androidx.compose.material3.LocalTextStyle
import com.aware.app.ui.theme.Skin
import com.aware.app.ui.theme.CozyPaper
import com.aware.app.ui.theme.CozyPowderBlue
import com.aware.app.ui.theme.CozyPistachio
import com.aware.app.ui.theme.CozyApricot
import com.aware.app.ui.theme.CozyLavender
import com.aware.app.ui.theme.CozyDustyRose
import com.aware.app.ui.theme.CozySage
import com.aware.app.ui.theme.CozyNegative
import com.aware.app.ui.theme.Void
import com.aware.app.ui.theme.NeonLime
import com.aware.app.ui.theme.NeonMagenta
import com.aware.app.ui.theme.NeonCyan
import com.aware.app.ui.theme.NeonViolet
import com.aware.app.ui.theme.NeonOrange
import com.aware.app.ui.theme.NeonGreen
import com.aware.app.ui.theme.NeonRed
import com.aware.app.ui.theme.readableAccent
import com.aware.app.storage.StorageSnapshot
import com.aware.app.storage.clearTemporaryStorage
import com.aware.app.storage.formatStorageSize
import com.aware.app.storage.readStorageSnapshot
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.compositeOver
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Locale
import kotlinx.coroutines.delay
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private enum class Tab(
    val label: String,
    val icon: ImageVector,
) {
    HOME("Log", Icons.Default.Home),
    ACTIVITY("Activity", Icons.AutoMirrored.Filled.ReceiptLong),
    PLAN("Budgets", Icons.Default.Savings),
    INSIGHTS("Insights", Icons.Default.AutoGraph),
    SETTINGS("Settings", Icons.Default.Settings),
}

private enum class SummaryPeriod(val label: String) {
    TODAY("Today"), WEEK("This week"), MONTH("This month")
}
private enum class InsightDimension(val label: String) { CATEGORY("Category"), PAYEE("Payee"), TAG("Tag") }

private data class PeriodSnapshot(
    val incomePaise: Long,
    val expensePaise: Long,
    val refundPaise: Long,
    val previousExpensePaise: Long,
) {
    val netPaise: Long get() = incomePaise + refundPaise - expensePaise
}

private data class TransactionConfirmation(
    val amountPaise: Long,
    val merchant: String,
    val type: TransactionType,
)

@Composable
fun AwareApp(
    appearance: Appearance,
    onAppearanceChange: (Appearance) -> Unit,
    skin: Skin,
    onSkinChange: (Skin) -> Unit,
    cozyPalette: CozyPalette,
    onCozyPaletteChange: (CozyPalette) -> Unit,
    viewModel: MainViewModel,
    widgetTransactionId: Long?,
    onWidgetTransactionHandled: () -> Unit,
    smsGranted: Boolean,
    paymentNotificationAccessGranted: Boolean,
    onRequestSms: () -> Unit,
    onRequestNotifications: () -> Unit,
    onRequestPaymentNotificationAccess: () -> Unit,
    onRefreshWidget: () -> Unit,
    onAppLockChange: (Boolean) -> Unit,
    onExportCsv: () -> Unit,
    onChooseStatement: () -> Unit,
    onCreateBackup: (String) -> Unit,
    onChooseRestore: () -> Unit,
    restoreReady: Boolean,
    onRestore: (String) -> Unit,
    showUpdatePanel: Boolean,
    updateRelease: AppRelease?,
    updateMessage: String?,
    updateBusy: Boolean,
    onCheckForUpdates: () -> Unit,
    onInstallUpdate: (AppRelease) -> Unit,
    onDismissUpdate: () -> Unit,
) {
    val state by viewModel.state.collectAsState()
    val review by viewModel.reviewCandidate.collectAsState()
    val groqConfigured by viewModel.groqConfigured.collectAsState()
    val aiSuggestion by viewModel.aiSuggestion.collectAsState()
    val appLockEnabled by viewModel.appLockEnabled.collectAsState()
    val smartNudgesEnabled by viewModel.smartNudgesEnabled.collectAsState()
    val statementImport by viewModel.statementImport.collectAsState()
    var selected by remember { mutableStateOf(Tab.HOME) }
    var addType by remember { mutableStateOf<TransactionType?>(null) }
    var showBudget by remember { mutableStateOf(false) }
    var showRecurring by remember { mutableStateOf(false) }
    var showAccount by remember { mutableStateOf(false) }
    var showAccountManager by remember { mutableStateOf(false) }
    var editingAccount by remember { mutableStateOf<AccountEntity?>(null) }
    var selectedTransaction by remember { mutableStateOf<TransactionEntity?>(null) }
    var editingTransaction by remember { mutableStateOf<TransactionEntity?>(null) }
    var confirmation by remember { mutableStateOf<TransactionConfirmation?>(null) }
    var showGroqKey by remember { mutableStateOf(false) }
    var showBackupPassword by remember { mutableStateOf(false) }
    var showRestorePassword by remember { mutableStateOf(false) }
    var showMonthlyReport by remember { mutableStateOf(false) }
    var showMoneyMoveChooser by remember { mutableStateOf(false) }
    var showOpenSourceNotice by remember { mutableStateOf(false) }
    var showStorage by remember { mutableStateOf(false) }
    var showCategoryForIncome by remember { mutableStateOf<Boolean?>(null) }
    var categoryManagerIncome by remember { mutableStateOf<Boolean?>(null) }
    var categoryCreatedCallback by remember { mutableStateOf<((Long) -> Unit)?>(null) }
    var showAppearance by remember { mutableStateOf(false) }
    var showSkin by remember { mutableStateOf(false) }
    var showCozyPalette by remember { mutableStateOf(false) }
    var showNotificationAccessDisclosure by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(viewModel) {
        viewModel.messages.collect { message -> snackbarHostState.showSnackbar(message) }
    }
    LaunchedEffect(restoreReady) { if (restoreReady) showRestorePassword = true }
    LaunchedEffect(state.pending.map { it.id to it.status }) { onRefreshWidget() }
    LaunchedEffect(widgetTransactionId) {
        val transactionId = widgetTransactionId ?: return@LaunchedEffect
        val result = snackbarHostState.showSnackbar(
            message = "Transaction added from widget",
            actionLabel = "Undo",
            withDismissAction = true,
        )
        if (result == SnackbarResult.ActionPerformed) viewModel.deleteTransaction(transactionId)
        onWidgetTransactionHandled()
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = { PremiumBottomBar(selected, onSelected = { selected = it }, onAdd = { showMoneyMoveChooser = true }) },
    ) { padding ->
        // Bottom-tab destinations switch directly. Cross-composing two full,
        // scrollable dashboards during a fade caused missed frames when users
        // moved quickly between tabs; the pill/icon motion already supplies
        // continuity without making the heavy screens overlap.
        Box(Modifier.fillMaxSize()) {
            when (selected) {
                Tab.HOME -> HomeScreen(
                    state, smsGranted, paymentNotificationAccessGranted, onRequestSms, viewModel::openReview,
                    onAdd = { addType = it },
                    onOpenActivity = { selected = Tab.ACTIVITY },
                    onOpenPlan = { selected = Tab.PLAN },
                    onOpenInsights = { selected = Tab.INSIGHTS },
                    onOpenTransaction = { selectedTransaction = it },
                    modifier = Modifier.padding(padding),
                )
                Tab.ACTIVITY -> ActivityScreen(
                    state,
                    onBack = { selected = Tab.HOME },
                    onExportCsv = onExportCsv,
                    onOpenTransaction = { selectedTransaction = it },
                    modifier = Modifier.padding(padding),
                )
                Tab.PLAN -> PlanScreen(state, { showBudget = true }, { showRecurring = true }, Modifier.padding(padding))
                Tab.INSIGHTS -> InsightsScreen(
                    state,
                    onOpenPlan = { selected = Tab.PLAN },
                    onOpenAiSetup = { showGroqKey = true },
                    onOpenMonthlyReport = { showMonthlyReport = true },
                    onOpenSettings = { selected = Tab.SETTINGS },
                    onOpenTransaction = { selectedTransaction = it },
                    modifier = Modifier.padding(padding),
                )
                Tab.SETTINGS -> SettingsScreen(
                    state, smsGranted, paymentNotificationAccessGranted, groqConfigured, appLockEnabled, smartNudgesEnabled,
                    onRequestSms,
                    { showNotificationAccessDisclosure = true },
                    { viewModel.setSmartNudges(!smartNudgesEnabled); if (!smartNudgesEnabled) onRequestNotifications() },
                    {
                        val enabled = !appLockEnabled
                        viewModel.setAppLock(enabled)
                        onAppLockChange(enabled)
                    },
                    { showAccountManager = true }, { categoryManagerIncome = it }, { showGroqKey = true }, onChooseStatement,
                    { showBackupPassword = true }, onExportCsv, onChooseRestore, { showStorage = true },
                    { showOpenSourceNotice = true }, appearance, { showAppearance = true },
                    skin, { showSkin = true }, cozyPalette, { showCozyPalette = true },
                    onCheckForUpdates, Modifier.padding(padding),
                )
            }
        }
    }

    if (showAppearance) AwareDialog("Appearance", { showAppearance = false }) {
        Text("Light or dark. System follows your device's setting.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        Appearance.entries.forEach { option ->
            ChoiceCard(
                title = option.label,
                subtitle = when (option) {
                    Appearance.SYSTEM -> "Match whatever the phone is doing."
                    Appearance.LIGHT -> "Always bright."
                    Appearance.DARK -> "Always dim."
                    Appearance.OLED -> "True black pixels for OLED displays."
                },
                selected = appearance == option,
                onClick = { onAppearanceChange(option) },
            )
        }
    }
    if (showSkin) AwareDialog("Theme", { showSkin = false }) {
        Text(
            "Two complete identities. Switching repaints every screen instantly - your data is untouched.",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Skin.entries.forEach { option ->
            ChoiceCard(
                title = option.label,
                subtitle = option.blurb,
                selected = skin == option,
                onClick = { onSkinChange(option) },
                swatch = skinSwatch(option),
            )
        }
    }
    if (showCozyPalette) AwareDialog("Cozy palette", { showCozyPalette = false }) {
        Text(
            "Each palette has a carefully tuned light and dark version. Appearance still controls which one you see.",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        val darkPreview = MaterialTheme.colorScheme.background.luminance() < .4f
        CozyPalette.entries.forEach { option ->
            ChoiceCard(
                title = option.label,
                subtitle = option.blurb,
                selected = cozyPalette == option,
                onClick = { onCozyPaletteChange(option) },
                swatch = cozyPaletteSwatch(option, darkPreview),
            )
        }
    }
    if (showMoneyMoveChooser) MoneyMoveChooser(
        onDismiss = { showMoneyMoveChooser = false },
        onChoose = { type -> showMoneyMoveChooser = false; addType = type },
    )
    addType?.let { initialType -> AddTransactionDialog(
        state, initialType,
        onDismiss = { addType = null },
        onAddCategory = { isIncome, onCreated ->
            categoryCreatedCallback = onCreated
            showCategoryForIncome = isIncome
        },
        onAddAccount = { showAccount = true },
    ) { amount, merchant, type, account, destination, category, note, tags, occurredAt ->
        viewModel.addManual(amount, merchant, type, account, destination, category, note, tags, occurredAt)
        addType = null
        confirmation = TransactionConfirmation(amount, merchant, type)
    } }
    if (showBudget) BudgetDialog(state, { showBudget = false }, onAddCategory = { showCategoryForIncome = false }) { name, cap, scope, category, account, payee, period, start, end, recurring -> viewModel.addBudget(name, cap, scope, category, account, payee, period, start, end, recurring); showBudget = false }
    if (showRecurring) RecurringDialog(
        state, { showRecurring = false },
        onAddCategory = { showCategoryForIncome = it },
        onAddAccount = { showAccount = true },
    ) { name, amount, type, account, category, cadence, interval, start, end, reminder -> viewModel.addRecurring(name, amount, type, account, category, cadence, interval, start, end, reminder); showRecurring = false }
    if (showAccount) AccountDialog({ showAccount = false }) { name, kind, opening -> viewModel.addAccount(name, kind, opening); showAccount = false }
    if (showAccountManager) AccountManagerDialog(
        accounts = state.accounts,
        onDismiss = { showAccountManager = false },
        onAdd = { editingAccount = AccountEntity(name = "", kind = AccountKind.BANK) },
        onEdit = { editingAccount = it },
    )
    categoryManagerIncome?.let { isIncome ->
        CategoryManagerDialog(
            isIncome = isIncome,
            categories = state.categories.filter { it.isIncome == isIncome },
            onDismiss = { categoryManagerIncome = null },
            onAdd = {
                categoryCreatedCallback = null
                showCategoryForIncome = isIncome
            },
        )
    }
    editingAccount?.let { account ->
        AccountEditorDialog(
            account = account.takeIf { it.id != 0L },
            onDismiss = { editingAccount = null },
            onSave = { id, name, kind, opening, isDefault ->
                viewModel.saveAccount(id, name, kind, opening, isDefault)
                editingAccount = null
            },
        )
    }
    selectedTransaction?.let { transaction ->
        TransactionDetailDialog(
            transaction = transaction,
            state = state,
            onDismiss = { selectedTransaction = null },
            onEdit = {
                selectedTransaction = null
                editingTransaction = transaction
            },
            onDelete = {
                viewModel.deleteTransaction(transaction.id)
                selectedTransaction = null
            },
        )
    }

    if (showUpdatePanel) AwareDialog("App update", onDismissUpdate) {
        when {
            updateBusy -> {
                Row(
                    Modifier.fillMaxWidth().padding(vertical = 18.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    CircularProgressIndicator(Modifier.size(24.dp), strokeWidth = 3.dp)
                    Text(updateMessage ?: "Checking GitHub Releases…")
                }
            }
            updateRelease != null -> {
                Text(
                    "aware ${updateRelease.version} is available",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                if (updateRelease.notes.isNotBlank()) {
                    Text(
                        updateRelease.notes.take(700),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.heightIn(max = 220.dp).verticalScroll(rememberScrollState()),
                    )
                }
                Text(
                    "Android will ask you to confirm the installation. Your data stays in place.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp,
                )
                Button(
                    onClick = { onInstallUpdate(updateRelease) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = LocalTokens.current.affirm, contentColor = LocalTokens.current.onAffirm),
                ) { Text("Download and install") }
            }
            else -> {
                Text(updateMessage ?: "You’re using the latest version of aware.")
                Text("Installed version ${BuildConfig.VERSION_NAME}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                Button(onClick = onCheckForUpdates, modifier = Modifier.fillMaxWidth()) { Text("Check again") }
            }
        }
    }
    if (showNotificationAccessDisclosure) AwareDialog("Capture payment notifications", { showNotificationAccessDisclosure = false }) {
        Text(
            "aware will read notifications from Google Pay and super.money to find transaction amounts. Android grants notification access broadly, but aware filters all other apps out on-device.",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            "Every detected payment stays in Upcoming until you review and approve it. Nothing is added to your ledger automatically.",
            fontWeight = FontWeight.SemiBold,
        )
        Button(
            onClick = {
                showNotificationAccessDisclosure = false
                onRequestPaymentNotificationAccess()
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = LocalTokens.current.affirm, contentColor = LocalTokens.current.onAffirm),
        ) { Text("Continue to Android settings") }
    }
    editingTransaction?.let { transaction ->
        AddTransactionDialog(
            state = state,
            initialType = transaction.type,
            transaction = transaction,
            onDismiss = { editingTransaction = null },
            onAddCategory = { isIncome, onCreated ->
                categoryCreatedCallback = onCreated
                showCategoryForIncome = isIncome
            },
            onAddAccount = { showAccount = true },
        ) { amount, merchant, type, account, destination, category, note, tags, occurredAt ->
            viewModel.updateTransaction(transaction, amount, merchant, type, account, destination, category, note, tags, occurredAt)
            editingTransaction = null
        }
    }
    if (showGroqKey) GroqKeyDialog({ showGroqKey = false }) { viewModel.saveGroqKey(it); showGroqKey = false }
    if (showBackupPassword) PasswordDialog("Encrypt backup", "Use at least 8 characters. You will need this password to restore.", { showBackupPassword = false }) { onCreateBackup(it); showBackupPassword = false }
    if (showRestorePassword) PasswordDialog("Restore aware", "Enter the password used when this backup was created. Existing ledger data will be replaced.", { showRestorePassword = false }) { onRestore(it); showRestorePassword = false }
    if (showMonthlyReport) MonthlyReportDialog(state) { showMonthlyReport = false }
    if (showOpenSourceNotice) OpenSourceNoticeDialog { showOpenSourceNotice = false }
    if (showStorage) StorageDialog { showStorage = false }
    when (val currentImport = statementImport) {
        StatementImportUiState.Idle -> Unit
        is StatementImportUiState.Reading -> StatementReadingDialog(currentImport.fileName, viewModel::closeStatementImport)
        is StatementImportUiState.NeedsPassword -> StatementPasswordDialog(
            fileName = currentImport.fileName,
            error = currentImport.error,
            onDismiss = viewModel::closeStatementImport,
            onUnlock = viewModel::unlockStatement,
        )
        is StatementImportUiState.PasswordForgotten -> PasswordDissolveOverlay(viewModel::finishPasswordAnimation)
        is StatementImportUiState.Reviewing -> StatementReviewDialog(
            review = currentImport.review,
            state = state,
            groqConfigured = groqConfigured,
            onDismiss = viewModel::closeStatementImport,
            onAccount = viewModel::setStatementAccount,
            onToggle = viewModel::toggleStatementRow,
            onSelectNew = viewModel::selectNewStatementRows,
            onType = viewModel::setStatementType,
            onCategory = viewModel::setStatementCategory,
            onDestination = viewModel::setStatementDestination,
            onGroq = viewModel::categoriseStatementWithGroq,
            onAddCategory = { showCategoryForIncome = it },
            onImport = viewModel::importReviewedStatement,
        )
    }
    review?.let { candidate ->
        CandidateReviewDialog(
            candidate = candidate,
            state = state,
            onDismiss = viewModel::closeReview,
            onDiscard = { viewModel.dismissCandidate(candidate.id) },
            onConfirm = { amount, merchant, type, category, account, destination, learn ->
                viewModel.confirmCandidate(candidate.id, amount, merchant, type, category, account, destination, learn)
                confirmation = TransactionConfirmation(amount, merchant, type)
            },
            groqConfigured = groqConfigured,
            aiSuggestion = aiSuggestion,
            onSuggest = { viewModel.suggestCategory(candidate) },
            onAddCategory = { showCategoryForIncome = it },
            onAddAccount = { showAccount = true },
        )
    }
    showCategoryForIncome?.let { isIncome ->
        CategoryDialog(isIncome, {
            categoryCreatedCallback = null
            showCategoryForIncome = null
        }) { name, emoji, color ->
            viewModel.addCategory(name, emoji, color, isIncome) { id ->
                categoryCreatedCallback?.invoke(id)
                categoryCreatedCallback = null
                showCategoryForIncome = null
            }
        }
    }
    confirmation?.let { saved ->
        TransactionConfirmationOverlay(saved) { confirmation = null }
    }
}

/** Representative colours for each skin, shown in the theme picker. */
private fun skinSwatch(skin: Skin): List<Color> = when (skin) {
    Skin.COZY -> listOf(CozyPaper, CozyPowderBlue, CozyPistachio, CozyApricot, CozyLavender)
    Skin.MAXIMAL -> listOf(Void, NeonLime, NeonMagenta, NeonCyan, NeonViolet)
}

private fun cozyPaletteSwatch(palette: CozyPalette, dark: Boolean): List<Color> = when (palette) {
    CozyPalette.OAT_GARDEN -> if (dark) {
        listOf(Color(0xFF1B1816), Color(0xFFC9DDAA), Color(0xFFB5CDD2), Color(0xFFD3939A), Color(0xFFE3B582))
    } else {
        listOf(Color(0xFFFFF9F0), CozyPistachio, CozyPowderBlue, CozyDustyRose, CozyApricot)
    }
    CozyPalette.SAGE_ROSE -> if (dark) {
        listOf(Color(0xFF141914), Color(0xFFAEB8A0), Color(0xFFD4AAA5), Color(0xFFB8CECB), Color(0xFFE1BA88))
    } else {
        listOf(Color(0xFFFBF8F1), Color(0xFFAEB8A0), Color(0xFFD5B2AC), Color(0xFFBED0D0), Color(0xFFE0B988))
    }
    CozyPalette.PLUM_HEARTH -> if (dark) {
        listOf(Color(0xFF130E11), Color(0xFF351E28), Color(0xFFD5A4B5), Color(0xFFD9B982), Color(0xFFAFC5A7))
    } else {
        listOf(Color(0xFFFFF7F4), Color(0xFFD4A7B5), Color(0xFFE7C48F), Color(0xFFAFC1A5), Color(0xFFB9CBD2))
    }
    CozyPalette.LINEN_CAFE -> if (dark) {
        listOf(Color(0xFF261B16), Color(0xFF4A342A), Color(0xFF7D5A44), Color(0xFFB2967D), Color(0xFFD7C9B8))
    } else {
        listOf(Color(0xFFF5F1EA), Color(0xFFD7C9B8), Color(0xFFB2967D), Color(0xFF7D5A44), Color(0xFF4A342A))
    }
    CozyPalette.NAVY_TIDE -> if (dark) {
        listOf(Color(0xFF172432), Color(0xFF2F4156), Color(0xFF567C8D), Color(0xFFC8D9E6), Color(0xFFF5EFEB))
    } else {
        listOf(Color(0xFFF5EFEB), Color(0xFFFFFFFF), Color(0xFFC8D9E6), Color(0xFF567C8D), Color(0xFF2F4156))
    }
    CozyPalette.CHARCOAL_LEATHER -> if (dark) {
        listOf(Color(0xFF242323), Color(0xFF363636), Color(0xFF525254), Color(0xFF795238), Color(0xFFAEA7A3))
    } else {
        listOf(Color(0xFFF5F2F0), Color(0xFFAEA7A3), Color(0xFF959595), Color(0xFF795238), Color(0xFF363636))
    }
}

@Composable
private fun ChoiceCard(
    title: String,
    subtitle: String,
    selected: Boolean,
    onClick: () -> Unit,
    swatch: List<Color>? = null,
) {
    val border by animateColorAsState(
        if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
        tween(160), label = "choice-$title",
    )
    Surface(
        modifier = Modifier.fillMaxWidth().clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
            onClick = onClick,
        ),
        shape = awareShape(16.dp),
        color = if (selected) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface,
        border = BorderStroke(if (selected) 2.dp else 1.dp, border),
    ) {
        Row(Modifier.padding(15.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Spacer(Modifier.height(2.dp))
                Text(subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp, lineHeight = 15.sp)
                swatch?.let { colors ->
                    Spacer(Modifier.height(9.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                        colors.forEach { c ->
                            Box(
                                Modifier.size(18.dp).clip(awareShape(5.dp)).background(c)
                                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, awareShape(5.dp)),
                            )
                        }
                    }
                }
            }
            if (selected) {
                Spacer(Modifier.width(10.dp))
                Box(
                    Modifier.size(24.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("✓", color = MaterialTheme.colorScheme.onPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun PremiumBottomBar(selected: Tab, onSelected: (Tab) -> Unit, onAdd: () -> Unit) {
    val haptics = LocalHapticFeedback.current
    val t = LocalTokens.current
    Box(
        Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.background).navigationBarsPadding().height(88.dp),
    ) {
        val barShape = RoundedCornerShape(if (t.maximal) 0.dp else 24.dp)
        Surface(
            Modifier.fillMaxWidth().padding(top = 18.dp).height(54.dp)
                .padding(horizontal = if (t.maximal) 0.dp else 18.dp),
            shape = barShape, color = t.navBar,
            shadowElevation = if (t.maximal) 0.dp else 8.dp,
            border = if (t.maximal) BorderStroke(t.outlineWidth, t.frame) else null,
        ) {
            Row(Modifier.fillMaxSize().padding(horizontal = if (t.maximal) 14.dp else 6.dp)) {
                BottomDestination(Tab.HOME, selected, haptics, onSelected, Modifier.weight(1f))
                BottomDestination(Tab.INSIGHTS, selected, haptics, onSelected, Modifier.weight(1f))
                Spacer(Modifier.width(72.dp))
                BottomDestination(Tab.PLAN, selected, haptics, onSelected, Modifier.weight(1f))
                BottomDestination(Tab.SETTINGS, selected, haptics, onSelected, Modifier.weight(1f))
            }
        }
        val interaction = remember { MutableInteractionSource() }
        val pressed by interaction.collectIsPressedAsState()
        val scale by animateFloatAsState(
            if (pressed) .91f else 1f,
            spring(dampingRatio = .68f, stiffness = Spring.StiffnessMedium),
            label = "center-add-press",
        )
        val addShape = if (t.maximal) RoundedCornerShape(0.dp) else CircleShape
        Surface(
            modifier = Modifier.size(56.dp).align(Alignment.TopCenter)
                .graphicsLayer { scaleX = scale; scaleY = scale }
                .shadow(if (t.glow > 0.dp) t.glow else 6.dp, addShape, ambientColor = t.accent, spotColor = t.accent)
                .border(3.dp, MaterialTheme.colorScheme.background, addShape)
                .clickable(interactionSource = interaction, indication = null) {
                    haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onAdd()
                },
            shape = addShape,
            color = t.accent,
            contentColor = t.onAccent,
        ) {
            Icon(Icons.Default.Add, "Add money move", Modifier.padding(13.dp).size(24.dp))
        }
    }
}

@Composable
private fun BottomDestination(
    tab: Tab,
    selected: Tab,
    haptics: androidx.compose.ui.hapticfeedback.HapticFeedback,
    onSelected: (Tab) -> Unit,
    modifier: Modifier = Modifier,
) {
    val active = selected == tab
    val t = LocalTokens.current
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) .94f else 1f, spring(dampingRatio = .72f, stiffness = Spring.StiffnessMedium), label = "tab-${tab.name}")
    val contentColor by animateColorAsState(
        if (active) t.onNavPill else t.navIdle,
        tween(180), label = "tab-color-${tab.name}",
    )
    val pillColor by animateColorAsState(
        if (active) t.navPill else Color.Transparent,
        tween(180), label = "tab-pill-${tab.name}",
    )
    Box(
        modifier.fillMaxHeight()
            .clickable(interactionSource = interaction, indication = null) {
            if (!active) haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            onSelected(tab)
        },
        contentAlignment = Alignment.Center,
    ) {
        Box(
            Modifier.width(46.dp).height(38.dp)
                .clip(RoundedCornerShape(if (t.maximal) 0.dp else 13.dp))
                .background(pillColor),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                tab.icon,
                tab.label,
                Modifier.graphicsLayer { scaleX = scale; scaleY = scale }.size(25.dp),
                tint = contentColor,
            )
        }
    }
}
@Composable
private fun NeoCard(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.surface,
    content: @Composable ColumnScope.() -> Unit,
) {
    Card(
        modifier = modifier,
        shape = awareShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = color),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) { Column(Modifier.padding(16.dp), content = content) }
}

@Composable
private fun HomeScreen(
    state: MainUiState,
    smsGranted: Boolean,
    paymentNotificationAccessGranted: Boolean,
    onRequestSms: () -> Unit,
    onReview: (Long) -> Unit,
    onAdd: (TransactionType) -> Unit,
    onOpenActivity: () -> Unit,
    onOpenPlan: () -> Unit,
    onOpenInsights: () -> Unit,
    onOpenTransaction: (TransactionEntity) -> Unit,
    modifier: Modifier = Modifier,
) {
    var period by remember { mutableStateOf(SummaryPeriod.WEEK) }
    val visibleTransactions = remember(state.transactions, period) { transactionsForPeriod(state.transactions, period) }
    val grouped = remember(visibleTransactions) {
        val zone = ZoneId.systemDefault()
        visibleTransactions.take(12).groupBy { Instant.ofEpochMilli(it.occurredAt).atZone(zone).toLocalDate() }
            .toSortedMap(compareByDescending { it })
    }
    val categoriesById = remember(state.categories) { state.categories.associateBy { it.id } }
    val accountsById = remember(state.accounts) { state.accounts.associateBy { it.id } }
    LazyColumn(modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 26.dp)) {
        item {
            AwareTopBar(
                period = period,
                onPeriod = { period = it },
                onSearch = onOpenActivity,
                pendingCount = state.pending.size,
                onReviewPending = { state.pending.firstOrNull()?.let { onReview(it.id) } },
            )
        }
        if (!smsGranted && !paymentNotificationAccessGranted) {
            item {
                val tk = LocalTokens.current
                Surface(
                    Modifier.padding(horizontal = 20.dp, vertical = 8.dp).fillMaxWidth(),
                    RoundedCornerShape(if (tk.maximal) 0.dp else 12.dp),
                    if (tk.maximal) tk.panel else MaterialTheme.colorScheme.surface,
                    border = if (tk.maximal) BorderStroke(tk.outlineWidth, tk.frame) else null,
                ) {
                    Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(40.dp).clip(awareShape(10.dp)).background(LocalTokens.current.accent.copy(.18f)), contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Sms, null, tint = LocalTokens.current.accent, modifier = Modifier.size(19.dp))
                        }
                        Spacer(Modifier.width(14.dp))
                        Column(Modifier.weight(1f)) {
                            Text("Automatic capture is off", fontWeight = FontWeight.SemiBold)
                            Text("Only new transaction messages", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                        }
                        TextButton(onClick = onRequestSms) { Text("Enable") }
                    }
                }
            }
        }
        item { AwareBalanceHeader(state, period) }
        if (state.pending.isNotEmpty()) {
            item { AwareSectionHeader("UPCOMING", "${state.pending.size} TO REVIEW") }
            items(state.pending.take(3), key = { "capture-${it.id}" }) { candidate ->
                AwareCaptureRow(candidate) { onReview(candidate.id) }
            }
        }
        if (visibleTransactions.isEmpty()) {
            item { AwareSectionHeader(period.label.uppercase(), money(0)) }
            item { EmptyHint("Your log is empty", "Press the plus button or let your next payment arrive.", Icons.Default.Inbox) }
        } else {
            grouped.forEach { (date, transactions) ->
                item(key = "home-$date") { AwareDateHeader(date, transactions) }
                items(transactions, key = { it.id }, contentType = { "txn" }) {
                    TransactionRow(it, categoriesById, accountsById, onClick = { onOpenTransaction(it) })
                }
            }
            item {
                TextButton(onClick = onOpenActivity, modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                    Text("See complete activity", color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
private fun AwareTopBar(
    period: SummaryPeriod,
    onPeriod: (SummaryPeriod) -> Unit,
    onSearch: () -> Unit,
    pendingCount: Int,
    onReviewPending: () -> Unit,
) {
    val t = LocalTokens.current
    val searchInk = readableAccent(t.onAccent, t.lilac)
    val iconShape = if (t.maximal) RoundedCornerShape(0.dp) else CircleShape
    Column(Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 20.dp, vertical = 10.dp)) {
        Breadcrumb("ledger")
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Surface(
                shape = iconShape,
                color = if (t.maximal) Color.Transparent else t.lilac,
                border = if (t.maximal) BorderStroke(t.outlineWidth, t.frame) else null,
            ) {
                IconButton(onClick = onSearch, modifier = Modifier.size(40.dp)) {
                    Icon(Icons.Default.Search, "Search activity", tint = if (t.maximal) t.accent else searchInk)
                }
            }
            Spacer(Modifier.weight(1f))
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                StencilText(
                    "aware",
                    style = MaterialTheme.typography.headlineSmall,
                    shadowOffset = 2.dp,
                    maxLines = 1,
                )
                Text(
                    if (t.maximal) "LEDGER // LIVE" else "Your money, in focus",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.labelSmall,
                )
            }
            Spacer(Modifier.weight(1f))
            if (pendingCount > 0) {
                Surface(
                    shape = iconShape,
                    color = if (t.maximal) Color.Transparent else t.warn.copy(alpha = .2f),
                    border = if (t.maximal) BorderStroke(t.outlineWidth, t.warn) else null,
                ) {
                    IconButton(onClick = onReviewPending, modifier = Modifier.size(40.dp)) {
                        BadgedBox(
                            badge = {
                                Badge(
                                    containerColor = t.warn,
                                    contentColor = readableAccent(t.onAccent, t.warn),
                                ) {
                                    Text(if (pendingCount > 99) "99+" else pendingCount.toString())
                                }
                            },
                        ) {
                            Icon(
                                Icons.Default.Inbox,
                                "Review $pendingCount pending payment${if (pendingCount == 1) "" else "s"}",
                                tint = if (t.maximal) t.warn else MaterialTheme.colorScheme.onSurface,
                            )
                        }
                    }
                }
            } else {
                // Preserve the centered wordmark without showing an inactive duplicate action.
                Spacer(Modifier.size(40.dp))
            }
        }
        PeriodSelector(period, onPeriod, Modifier.padding(top = 9.dp))
    }
}

@Composable
private fun AwareBalanceHeader(state: MainUiState, period: SummaryPeriod) {
    val snapshot = remember(state.transactions, period) { summarizePeriod(state.transactions, period) }
    val t = LocalTokens.current
    // The console skin reads as instrumentation, not a colour block: the hero
    // becomes a framed plate and the accent is spent on markers, not fills.
    val heroFill = if (t.maximal) t.panel else t.hero
    val onHero = if (t.maximal) MaterialTheme.colorScheme.onBackground else t.onHero
    val muted = onHero.copy(alpha = .62f)
    val delta = remember(snapshot) {
        if (snapshot.previousExpensePaise <= 0L) null
        else ((snapshot.expensePaise - snapshot.previousExpensePaise) * 100f / snapshot.previousExpensePaise)
    }
    Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 14.dp)) {
    Surface(
        Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(t.heroRadius),
        color = heroFill,
        border = if (t.maximal) BorderStroke(2.dp, t.accent) else null,
    ) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 20.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
                Column(Modifier.weight(1f)) {
                    if (t.maximal) {
                        MachineLabel("balance // net position", markerColor = t.accent)
                    } else {
                        Text(
                            "YOUR BALANCE",
                            color = muted,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = .8.sp,
                        )
                    }
                }
                if (t.maximal) {
                    CornerMeta(
                        listOf(
                            "period ${period.label}",
                            "entries ${state.transactions.size}",
                            "mode local_only",
                        ),
                    )
                } else {
                    Text(period.label, color = muted, fontSize = 10.sp, fontWeight = FontWeight.Medium)
                }
            }
            Spacer(Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                AnimatedMoneyAmount(
                    paise = snapshot.netPaise,
                    signed = true,
                    style = MaterialTheme.typography.displayLarge,
                    color = onHero,
                    modifier = Modifier.weight(1f, fill = false),
                )
                delta?.let { percent ->
                    Spacer(Modifier.width(9.dp))
                    val down = percent <= 0f
                    Surface(shape = awareShape(50.dp), color = onHero.copy(alpha = .14f)) {
                        Text(
                            (if (down) "↓ " else "↑ ") + "${kotlin.math.abs(percent).toInt()}%",
                            Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            color = onHero,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
            HorizontalDivider(color = onHero.copy(alpha = .18f))
            Spacer(Modifier.height(13.dp))
            Row(Modifier.fillMaxWidth()) {
                HeroFooterMetric("Money in", "+" + money(snapshot.incomePaise + snapshot.refundPaise), onHero, muted, Modifier.weight(1f))
                Box(Modifier.width(1.dp).height(30.dp).background(onHero.copy(alpha = .18f)))
                HeroFooterMetric("Money out", "−" + money(snapshot.expensePaise), onHero, muted, Modifier.weight(1f).padding(start = 16.dp))
            }
        }
    }
    // The reference pins a bordered readout directly beneath its hero panel.
    if (t.maximal) {
        MetaStrip(
            listOf(
                MetaItem("accounts", state.accounts.size.toString()),
                MetaItem("caps_set", state.budgets.size.toString()),
                MetaItem("capture_q", if (state.pending.isEmpty()) "clear" else "${state.pending.size} held"),
            ),
        )
    }
    }
}

@Composable
private fun HeroFooterMetric(label: String, value: String, onHero: Color, muted: Color, modifier: Modifier = Modifier) {
    val t = LocalTokens.current
    Column(modifier) {
        Text(
            if (t.maximal) label.uppercase().replace(' ', '_') else label,
            color = muted,
            style = MaterialTheme.typography.labelSmall,
        )
        Spacer(Modifier.height(3.dp))
        Text(value, color = onHero, fontSize = 17.sp, fontWeight = FontWeight.Bold, maxLines = 1)
    }
}

@Composable
private fun AwareSectionHeader(label: String, trailing: String) {
    val t = LocalTokens.current
    Row(
        Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, top = 19.dp, bottom = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (t.maximal) {
            Text("//", color = t.accent, style = MaterialTheme.typography.labelSmall)
            Spacer(Modifier.width(6.dp))
        }
        Text(
            if (t.maximal) label.uppercase().replace(' ', '_') else label,
            Modifier.weight(1f),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.labelSmall,
        )
        Text(
            if (t.maximal) trailing.uppercase() else trailing,
            color = if (t.maximal) t.accent else MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.labelSmall,
        )
    }
    HorizontalDivider(Modifier.padding(horizontal = 20.dp), color = if (t.maximal) t.frame else MaterialTheme.colorScheme.outlineVariant)
}

@Composable
private fun AwareDateHeader(date: LocalDate, transactions: List<TransactionEntity>) {
    val net = transactions.sumOf {
        when (it.type) {
            TransactionType.INCOME, TransactionType.REFUND -> it.amountPaise
            TransactionType.EXPENSE -> -it.amountPaise
            else -> 0L
        }
    }
    val label = when (date) {
        LocalDate.now() -> "TODAY"
        LocalDate.now().minusDays(1) -> "YESTERDAY"
        else -> date.format(DateTimeFormatter.ofPattern("EEE, d MMM")).uppercase()
    }
    AwareSectionHeader(label, (if (net > 0) "+" else if (net < 0) "−" else "") + money(kotlin.math.abs(net)))
}

@Composable
private fun AwareCaptureRow(candidate: CaptureCandidateEntity, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = 20.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.size(36.dp).clip(awareShape(10.dp)).background(LocalTokens.current.warn.copy(.25f)), contentAlignment = Alignment.Center) {
            Icon(if (candidate.source == TransactionSource.NOTIFICATION) Icons.Default.Notifications else Icons.Default.Sms, null, tint = LocalTokens.current.warn, modifier = Modifier.size(18.dp))
        }
        Spacer(Modifier.width(11.dp))
        Column(Modifier.weight(1f)) {
            Text(candidate.merchant, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text("${candidate.sender} · awaiting your approval", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        Text(money(candidate.amountPaise), fontWeight = FontWeight.SemiBold)
    }
}
@Composable
private fun PeriodSelector(selected: SummaryPeriod, onSelected: (SummaryPeriod) -> Unit, modifier: Modifier = Modifier) {
    val t = LocalTokens.current
    Surface(modifier.fillMaxWidth(), shape = awareShape(17.dp), color = Color.Transparent) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            SummaryPeriod.entries.forEach { period ->
                val active = selected == period
                val color by animateColorAsState(if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.background, tween(170), label = "period-${period.name}")
                Surface(
                    modifier = Modifier.weight(1f).clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { onSelected(period) },
                    shape = RoundedCornerShape(if (t.maximal) 0.dp else 15.dp),
                    color = color,
                    border = if (active) null else BorderStroke(1.dp, if (t.maximal) t.frame else MaterialTheme.colorScheme.outline),
                    contentColor = if (active) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onBackground,
                ) {
                    Text(
                        if (t.maximal) period.label.uppercase() else period.label,
                        Modifier.padding(vertical = 8.dp),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        style = if (t.maximal) MaterialTheme.typography.labelSmall else MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }
    }
}
@Composable
private fun SectionTitle(title: String, subtitle: String, onAction: (() -> Unit)? = null) {
    Row(Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, top = 28.dp, bottom = 12.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            if (!subtitle.equals("See all", true)) Text(subtitle, fontSize = 9.sp, color = MaterialTheme.colorScheme.primary)
            Text(title, style = MaterialTheme.typography.titleLarge)
        }
        if (subtitle.equals("See all", true)) {
            Text(
                "See all",
                modifier = if (onAction == null) Modifier else Modifier.clip(awareShape(50.dp)).clickable(onClick = onAction).padding(horizontal = 10.dp, vertical = 7.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 10.sp,
            )
        }
    }
}

@Composable
private fun EmptyHint(title: String, subtitle: String, icon: ImageVector) {
    val t = LocalTokens.current
    DashedPanel(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 18.dp)) {
        Column(
            Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                Modifier.size(52.dp)
                    .clip(RoundedCornerShape(t.cardRadius.coerceAtMost(14.dp)))
                    .background(if (t.maximal) Color.Transparent else MaterialTheme.colorScheme.surface)
                    .then(if (t.maximal) Modifier.border(t.outlineWidth, t.frame) else Modifier),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icon, null, tint = if (t.maximal) t.accent else MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(23.dp))
            }
            Spacer(Modifier.height(13.dp))
            StencilText(
                title,
                style = MaterialTheme.typography.headlineSmall,
                shadowOffset = 2.dp,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                if (t.maximal) subtitle.uppercase() else subtitle,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = if (t.maximal) MaterialTheme.typography.labelSmall else MaterialTheme.typography.bodySmall,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            )
        }
    }
}
@Composable
private fun TransactionRow(
    transaction: TransactionEntity,
    // Indexed rather than lists: this row renders inside LazyColumns, and the
    // old firstOrNull scans ran once per category and account on every row.
    categoriesById: Map<Long, CategoryEntity>,
    accountsById: Map<Long, AccountEntity>,
    onClick: (() -> Unit)? = null,
) {
    val category = transaction.categoryId?.let(categoriesById::get)
    val account = accountsById[transaction.accountId]
    val incoming = transaction.type == TransactionType.INCOME || transaction.type == TransactionType.REFUND
    val subtitle = remember(transaction.id, category, account) {
        buildString {
            append(formatTime(transaction.occurredAt))
            append(" · ")
            append(category?.name ?: transaction.type.name.lowercase().replaceFirstChar(Char::uppercase))
            account?.let { append(" · ").append(it.name) }
        }
    }
    val amountLabel = remember(transaction.id, transaction.amountPaise, transaction.type) {
        (if (incoming) "+" else if (transaction.type == TransactionType.TRANSFER) "↔ " else "−") + money(transaction.amountPaise)
    }
    Row(
        Modifier.fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = 20.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val categoryColor = category?.colorArgb?.let(::Color)
            ?: if (incoming) LocalTokens.current.positive else LocalTokens.current.hero
        Box(Modifier.size(36.dp).clip(awareShape(10.dp)).background(categoryColor.copy(.27f)), contentAlignment = Alignment.Center) {
            Text(category?.emoji ?: if (incoming) "↗" else "↙", fontSize = 17.sp)
        }
        Spacer(Modifier.width(11.dp))
        Column(Modifier.weight(1f)) {
            Text(transaction.merchant, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(
                subtitle,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 10.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Text(
            amountLabel,
            fontWeight = FontWeight.SemiBold,
            color = if (incoming) readableAccent(LocalTokens.current.positive) else MaterialTheme.colorScheme.onSurface,
            fontSize = 14.sp,
        )
    }
}

@Composable
private fun ActivityScreen(
    state: MainUiState,
    onBack: () -> Unit,
    onExportCsv: () -> Unit,
    onOpenTransaction: (TransactionEntity) -> Unit,
    modifier: Modifier = Modifier,
) {
    var query by remember { mutableStateOf("") }
    var typeFilter by remember { mutableStateOf<TransactionType?>(null) }
    var categoryFilter by remember { mutableStateOf<Long?>(null) }
    var accountFilter by remember { mutableStateOf<Long?>(null) }
    var month by remember { mutableStateOf(YearMonth.now()) }
    val zone = remember { ZoneId.systemDefault() }
    val monthStart = remember(month, zone) { month.atDay(1).atStartOfDay(zone).toInstant().toEpochMilli() }
    val monthEnd = remember(month, zone) { month.plusMonths(1).atDay(1).atStartOfDay(zone).toInstant().toEpochMilli() }
    val filtered = remember(state.transactions, query, typeFilter, categoryFilter, accountFilter, monthStart, monthEnd) {
        state.transactions.filter {
            it.occurredAt in monthStart until monthEnd &&
                (query.isBlank() || it.merchant.contains(query, true) || it.note.contains(query, true) || it.tags.contains(query, true)) &&
                (typeFilter == null || it.type == typeFilter) &&
                (categoryFilter == null || it.categoryId == categoryFilter) &&
                (accountFilter == null || it.accountId == accountFilter || it.destinationAccountId == accountFilter)
        }
    }
    val grouped = remember(filtered, zone) {
        filtered.groupBy { Instant.ofEpochMilli(it.occurredAt).atZone(zone).toLocalDate() }.toSortedMap(compareByDescending { it })
    }
    val monthSpent = remember(filtered) { filtered.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amountPaise } }
    val categoriesById = remember(state.categories) { state.categories.associateBy { it.id } }
    val accountsById = remember(state.accounts) { state.accounts.associateBy { it.id } }
    LazyColumn(modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 34.dp)) {
        item { AwareSubpageHeader("All activity", onBack, onExportCsv) }
        item { MonthControl(month, { month = month.minusMonths(1) }, { month = month.plusMonths(1) }) }
        item { ActivityMonthSummary(monthSpent, filtered.size) }
        item {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 10.dp),
                leadingIcon = { Icon(Icons.Default.Search, null) },
                placeholder = { Text("Search merchant, note, or tag") },
                shape = awareShape(16.dp),
                colors = cozyFieldColors(),
                singleLine = true,
            )
        }
        item {
            FilterStrip {
                FilterPill("All types", typeFilter == null) { typeFilter = null }
                listOf(TransactionType.EXPENSE, TransactionType.INCOME, TransactionType.TRANSFER, TransactionType.REFUND).forEach { type ->
                    FilterPill(type.name.lowercase().replaceFirstChar(Char::uppercase), typeFilter == type) { typeFilter = type }
                }
            }
        }
        item {
            FilterStrip {
                FilterPill("All categories", categoryFilter == null) { categoryFilter = null }
                state.categories.forEach { category ->
                    FilterPill("${category.emoji} ${category.name}", categoryFilter == category.id) { categoryFilter = category.id }
                }
            }
        }
        item {
            FilterStrip {
                FilterPill("All accounts", accountFilter == null) { accountFilter = null }
                state.accounts.forEach { account -> FilterPill(account.name, accountFilter == account.id) { accountFilter = account.id } }
            }
        }
        item { SectionTitle("${filtered.size} transaction${if (filtered.size == 1) "" else "s"}", month.format(DateTimeFormatter.ofPattern("MMMM yyyy"))) }
        if (filtered.isEmpty()) {
            item { EmptyHint("Nothing matches", "Try another month or clear a filter.", Icons.Default.Search) }
        } else {
            grouped.forEach { (date, transactions) ->
                item(key = "date-$date") { DateGroupHeader(date, transactions) }
                items(transactions, key = { it.id }, contentType = { "txn" }) { transaction ->
                    TransactionRow(transaction, categoriesById, accountsById, onClick = { onOpenTransaction(transaction) })
                }
            }
        }
    }
}

@Composable
private fun AwareSubpageHeader(title: String, onBack: () -> Unit, onExport: () -> Unit) {
    Column(Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 20.dp, vertical = 10.dp)) {
        Breadcrumb("ledger / activity")
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack, modifier = Modifier.size(34.dp)) { Icon(Icons.Default.ChevronLeft, "Back") }
            Spacer(Modifier.width(6.dp))
            ScreenTitleInline(title, Modifier.weight(1f))
            IconButton(onClick = onExport) { Icon(Icons.Default.FileDownload, "Export transactions") }
        }
        ConsoleRule(Modifier.padding(top = 10.dp))
    }
}

@Composable
private fun FilterStrip(content: @Composable RowScope.() -> Unit) {
    Row(
        Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        content = content,
    )
}

@Composable
private fun MonthControl(month: YearMonth, onPrevious: () -> Unit, onNext: () -> Unit) {
    Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        Surface(shape = CircleShape, color = MaterialTheme.colorScheme.surface, modifier = Modifier.clickable(onClick = onPrevious)) {
            Icon(Icons.Default.ChevronLeft, "Previous month", Modifier.padding(9.dp).size(18.dp))
        }
        Text(month.format(DateTimeFormatter.ofPattern("MMMM yyyy")), Modifier.weight(1f), textAlign = androidx.compose.ui.text.style.TextAlign.Center, fontWeight = FontWeight.SemiBold)
        Surface(shape = CircleShape, color = MaterialTheme.colorScheme.surface, modifier = Modifier.clickable(onClick = onNext)) {
            Icon(Icons.Default.ChevronRight, "Next month", Modifier.padding(9.dp).size(18.dp))
        }
    }
}

@Composable
private fun ActivityMonthSummary(spentPaise: Long, count: Int) {
    Column(Modifier.padding(horizontal = 20.dp, vertical = 16.dp).fillMaxWidth()) {
        Text("TOTAL SPENT", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 9.sp, letterSpacing = .7.sp)
        Spacer(Modifier.height(6.dp))
        AnimatedMoneyAmount(spentPaise, style = MaterialTheme.typography.headlineLarge)
        Text("$count transaction${if (count == 1) "" else "s"} in this view", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
    }
}

@Composable
private fun DateGroupHeader(date: LocalDate, transactions: List<TransactionEntity>) {
    val expense = transactions.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amountPaise }
    val incoming = transactions.filter { it.type == TransactionType.INCOME || it.type == TransactionType.REFUND }.sumOf { it.amountPaise }
    val label = when (date) {
        LocalDate.now() -> "Today"
        LocalDate.now().minusDays(1) -> "Yesterday"
        else -> date.format(DateTimeFormatter.ofPattern("d MMM yyyy"))
    }
    Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
        Surface(shape = awareShape(9.dp), color = MaterialTheme.colorScheme.surfaceVariant) {
            Text(date.format(DateTimeFormatter.ofPattern("EEE")).uppercase(), Modifier.padding(horizontal = 9.dp, vertical = 6.dp), color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 9.sp, fontWeight = FontWeight.SemiBold)
        }
        Spacer(Modifier.width(10.dp))
        Text(label, Modifier.weight(1f), fontWeight = FontWeight.SemiBold)
        Text(if (expense > 0) "−${money(expense)}" else "+${money(incoming)}", color = if (expense > 0) MaterialTheme.colorScheme.onSurface else LocalTokens.current.positive, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun FilterPill(label: String, selected: Boolean, onClick: () -> Unit) {
    ConsoleChip(label, selected, Modifier.clickable(onClick = onClick))
}

@Composable
private fun PlanScreen(state: MainUiState, onBudget: () -> Unit, onRecurring: () -> Unit, modifier: Modifier = Modifier) {
    val totalCap = remember(state.budgets) { state.budgets.sumOf { it.capPaise } }
    // One pass over the ledger per budget, memoized: this used to re-parse the
    // budget month and re-scan every transaction on each recomposition.
    val spentByBudget = remember(state.budgets, state.transactions) {
        state.budgets.associate { it.id to spentForBudget(it, state.transactions) }
    }
    val totalSpent = remember(spentByBudget, state.budgets) {
        state.budgets.sumOf { (spentByBudget[it.id] ?: 0L).coerceAtMost(it.capPaise) }
    }
    val categoriesById = remember(state.categories) { state.categories.associateBy { it.id } }
    LazyColumn(modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 30.dp)) {
        item {
            Column(Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 20.dp, vertical = 18.dp)) {
            Breadcrumb("budgets")
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                ScreenTitleInline("Budgets", Modifier.weight(1f))
                Surface(
                    modifier = Modifier.clickable(onClick = onBudget),
                    shape = RoundedCornerShape(LocalTokens.current.chipRadius),
                    color = LocalTokens.current.accent,
                    contentColor = LocalTokens.current.onAccent,
                ) {
                    Text(
                        if (LocalTokens.current.maximal) "+ NEW CAP" else "+ New budget",
                        Modifier.padding(horizontal = 15.dp, vertical = 9.dp),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
            ConsoleRule(Modifier.padding(top = 10.dp))
            }
        }
        item { AwareBudgetGauge(totalSpent, totalCap) }
        if (state.budgets.isEmpty()) {
            item { EmptyHint("No budgets yet", "Create a budget and aware will keep the period visible.", Icons.Default.Tune) }
        } else {
            state.budgets.chunked(2).forEachIndexed { index, pair ->
                item(key = "budget-row-$index") {
                    Row(
                        Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 5.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        pair.forEach { budget ->
                            AwareBudgetCard(budget, spentByBudget[budget.id] ?: 0L, categoriesById, Modifier.weight(1f))
                        }
                        if (pair.size == 1) Spacer(Modifier.weight(1f))
                    }
                }
            }
        }
        item {
            Row(Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, top = 28.dp, bottom = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("RECURRING", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                    Text("Expected cash flow", fontWeight = FontWeight.SemiBold, fontSize = 18.sp)
                }
                TextButton(onClick = onRecurring) { Text("+ Add", fontWeight = FontWeight.SemiBold) }
            }
        }
        if (state.recurring.isEmpty()) item { EmptyHint("Nothing scheduled", "Add salary, family support, subscriptions, or any repeating entry.", Icons.AutoMirrored.Filled.ReceiptLong) }
        else items(state.recurring, key = { it.id }) { RecurringRow(it, state.categories) }
    }
}

@Composable
private fun AwareBudgetGauge(spent: Long, cap: Long) {
    val target = if (cap <= 0) 0f else (spent.toFloat() / cap).coerceIn(0f, 1f)
    val progress by animateFloatAsState(target, spring(dampingRatio = .82f, stiffness = Spring.StiffnessLow), label = "overall-budget")
    val track = MaterialTheme.colorScheme.surface
    val fill = when { target >= 1f -> LocalTokens.current.negative; target >= .75f -> LocalTokens.current.warn; else -> LocalTokens.current.accent }
    // A ring, as in the reference goal and category cards. The previous
    // semicircle was drawn into the full canvas rect, so it rendered as a
    // flattened ellipse rather than an arc.
    Box(Modifier.fillMaxWidth().padding(vertical = 12.dp), contentAlignment = Alignment.Center) {
        Box(Modifier.size(178.dp), contentAlignment = Alignment.Center) {
            Canvas(Modifier.fillMaxSize()) {
                val stroke = 18.dp.toPx()
                val inset = stroke / 2f
                val side = kotlin.math.min(size.width, size.height) - stroke
                val topLeft = androidx.compose.ui.geometry.Offset(inset, inset)
                val arcSize = androidx.compose.ui.geometry.Size(side, side)
                drawArc(track, 0f, 360f, false, topLeft = topLeft, size = arcSize, style = Stroke(stroke))
                drawArc(
                    fill, -90f, 360f * progress, false,
                    topLeft = topLeft, size = arcSize,
                    style = Stroke(stroke, cap = StrokeCap.Round),
                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "${(target * 100).toInt()}% SPENT",
                    color = readableAccent(if (target >= 1f) LocalTokens.current.negative else LocalTokens.current.positive),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = .6.sp,
                )
                Spacer(Modifier.height(2.dp))
                AnimatedMoneyAmount(
                    if (cap <= 0) 0 else (cap - spent).coerceAtLeast(0),
                    style = MaterialTheme.typography.headlineMedium,
                )
                Text(
                    if (cap <= 0) "set your first budget" else "left this month",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 10.sp,
                )
            }
        }
    }
}

@Composable
private fun AwareBudgetCard(
    budget: BudgetBucketEntity,
    spent: Long,
    categoriesById: Map<Long, CategoryEntity>,
    modifier: Modifier = Modifier,
) {
    val category = budget.categoryId?.let(categoriesById::get)
    val accent = category?.colorArgb?.let(::Color) ?: LocalTokens.current.warn
    val rawProgress = if (budget.capPaise <= 0) 0f else spent.toFloat() / budget.capPaise
    val progress by animateFloatAsState(rawProgress.coerceIn(0f, 1f), spring(dampingRatio = .86f, stiffness = Spring.StiffnessLow), label = "budget-${budget.id}")
    val remaining = (budget.capPaise - spent).coerceAtLeast(0)
    val daysLeft = YearMonth.now().lengthOfMonth() - LocalDate.now().dayOfMonth + 1
    Surface(modifier.height(150.dp), awareShape(12.dp), MaterialTheme.colorScheme.surface) {
        Column(Modifier.padding(13.dp)) {
            Text("${category?.emoji ?: "◫"} ${budget.name}", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text("${budget.scope.name.lowercase().replaceFirstChar(Char::uppercase)} · ${budget.period.name.lowercase()}", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 10.sp)
            Spacer(Modifier.weight(1f))
            Text("${(rawProgress * 100).toInt()}% SPENT", color = readableAccent(if (rawProgress >= 1f) LocalTokens.current.negative else LocalTokens.current.positive), fontSize = 9.sp, fontWeight = FontWeight.Bold)
            Text(money(remaining), fontSize = 20.sp, fontWeight = FontWeight.Medium)
            Text("left this ${budget.period.name.lowercase()}", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 9.sp)
            Spacer(Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth().height(8.dp).clip(awareShape(3.dp)),
                color = accent,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
            )
        }
    }
}
@Composable
private fun RecurringRow(rule: RecurringRuleEntity, categories: List<CategoryEntity>) {
    NeoCard(Modifier.padding(horizontal = 20.dp, vertical = 5.dp).fillMaxWidth(), color = MaterialTheme.colorScheme.surface) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(42.dp).clip(CircleShape).background(if (rule.type == TransactionType.INCOME) LocalTokens.current.positive.copy(.2f) else LocalTokens.current.negative.copy(.2f)), contentAlignment = Alignment.Center) {
                Text(if (rule.type == TransactionType.INCOME) "↗" else "↙", color = if (rule.type == TransactionType.INCOME) LocalTokens.current.positive else LocalTokens.current.negative, fontWeight = FontWeight.SemiBold)
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(rule.name, fontWeight = FontWeight.SemiBold)
                Text("${rule.cadence.name.lowercase().replaceFirstChar(Char::uppercase)} · ${categories.firstOrNull { it.id == rule.categoryId }?.name ?: "Uncategorized"}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text(money(rule.amountPaise), fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun InsightsScreen(
    state: MainUiState,
    onOpenPlan: () -> Unit,
    onOpenAiSetup: () -> Unit,
    onOpenMonthlyReport: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenTransaction: (TransactionEntity) -> Unit,
    modifier: Modifier = Modifier,
) {
    var period by remember { mutableStateOf(SummaryPeriod.WEEK) }
    var dimension by remember { mutableStateOf(InsightDimension.CATEGORY) }
    val periodTransactions = remember(state.transactions, period) { transactionsForPeriod(state.transactions, period) }
    val expenses = remember(periodTransactions) { periodTransactions.filter { it.type == TransactionType.EXPENSE } }
    val income = remember(periodTransactions) { periodTransactions.filter { it.type == TransactionType.INCOME || it.type == TransactionType.REFUND }.sumOf { it.amountPaise } }
    val spent = remember(expenses) { expenses.sumOf { it.amountPaise } }
    val net = income - spent
    val byCategory = remember(expenses) { expenses.groupBy { it.categoryId }.mapValues { (_, values) -> values.sumOf { it.amountPaise } }.toList().sortedByDescending { it.second } }
    val byPayee = remember(expenses) { expenses.groupBy { it.merchant.ifBlank { "Unknown payee" } }.mapValues { (_, values) -> values.sumOf { it.amountPaise } }.toList().sortedByDescending { it.second } }
    val byTag = remember(expenses) {
        expenses.flatMap { transaction -> transaction.tags.split(',').map(String::trim).filter(String::isNotBlank).map { it to transaction.amountPaise } }
            .groupBy({ it.first }, { it.second }).mapValues { it.value.sum() }.toList().sortedByDescending { it.second }
    }
    val bars = remember(expenses, period) { insightBars(expenses, period) }
    val categoriesById = remember(state.categories) { state.categories.associateBy { it.id } }
    val accountsById = remember(state.accounts) { state.accounts.associateBy { it.id } }
    LazyColumn(modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 30.dp)) {
        item {
            Column(Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 20.dp, vertical = 14.dp)) {
                Breadcrumb("insights")
                ScreenTitle("Insights") {
                    IconButton(onClick = onOpenSettings) { Icon(Icons.Default.Tune, "Open settings") }
                }
                PeriodSelector(period, { period = it }, Modifier.padding(top = 12.dp))
            }
        }
        item {
            Column(Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
                Row(verticalAlignment = Alignment.Bottom) {
                    Column(Modifier.weight(1f)) {
                        Text(periodLabel(period), color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 10.sp, fontWeight = FontWeight.Medium)
                        AnimatedMoneyAmount(net, signed = true, style = MaterialTheme.typography.displaySmall)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("SPENT / DAY", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 10.sp)
                        AnimatedMoneyAmount(
                            if (period == SummaryPeriod.TODAY) spent else spent / if (period == SummaryPeriod.WEEK) 7 else LocalDate.now().dayOfMonth,
                            style = MaterialTheme.typography.headlineMedium,
                        )
                    }
                }
                Spacer(Modifier.height(14.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    AwareInsightTile("↗", "Income", income, LocalTokens.current.positive, Modifier.weight(1f))
                    AwareInsightTile("↘", "Expenses", spent, LocalTokens.current.negative, Modifier.weight(1f))
                }
            }
        }
        item { AwareBarChart(bars, Modifier.padding(horizontal = 20.dp, vertical = 12.dp)) }
        item {
            Column(Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
                ChoiceRow(InsightDimension.entries, dimension, label = { it.label }) { dimension = it }
                Spacer(Modifier.height(12.dp))
                when (dimension) {
                    InsightDimension.CATEGORY -> AwareCategoryBreakdown(byCategory, categoriesById)
                    InsightDimension.PAYEE -> AwareNamedBreakdown("Spending by payee", byPayee, spent)
                    InsightDimension.TAG -> AwareNamedBreakdown("Spending by tag", byTag, spent, emptyMessage = "Add tags to transactions to see them here.")
                }
            }
        }
        item {
            Row(Modifier.padding(horizontal = 20.dp, vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = onOpenAiSetup,
                    modifier = Modifier.weight(1f).height(46.dp),
                    shape = awareShape(50.dp),
                ) { Text("AI setup", fontWeight = FontWeight.SemiBold, fontSize = 13.sp) }
                Button(
                    onClick = onOpenMonthlyReport,
                    modifier = Modifier.weight(1f).height(46.dp),
                    shape = awareShape(50.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = LocalTokens.current.affirm,
                        contentColor = LocalTokens.current.onAffirm,
                    ),
                ) { Text("Compare months", fontWeight = FontWeight.SemiBold, fontSize = 13.sp) }
            }
        }
        item { AwareSectionHeader("ACTIVITY", "${periodTransactions.size} ENTRIES") }
        items(periodTransactions.take(5), key = { "insight-${it.id}" }, contentType = { "txn" }) {
            TransactionRow(it, categoriesById, accountsById, onClick = { onOpenTransaction(it) })
        }
        item {
            TextButton(onClick = onOpenPlan, modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) { Text("Open budgets →", fontWeight = FontWeight.SemiBold) }
        }
    }
}

@Composable
private fun AwareInsightTile(symbol: String, label: String, amount: Long, accent: Color, modifier: Modifier = Modifier) {
    Surface(modifier, awareShape(10.dp), MaterialTheme.colorScheme.surface) {
        Row(Modifier.padding(11.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(30.dp).clip(awareShape(8.dp)).background(accent.copy(.2f)), contentAlignment = Alignment.Center) {
                Text(symbol, color = readableAccent(accent, accent.copy(.2f).compositeOver(MaterialTheme.colorScheme.surface)), fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.width(8.dp))
            Column {
                Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 10.sp)
                Text(money(amount), fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            }
        }
    }
}

@Composable
private fun AwareBarChart(bars: List<Pair<String, Long>>, modifier: Modifier = Modifier) {
    val t = LocalTokens.current
    val peak = bars.maxOfOrNull { it.second } ?: 0L
    // Round the top of the scale up to a 1/2/5 step so the axis reads
    // "1.0k, 500" rather than "503.75".
    val max = remember(peak) { niceAxisMax(peak) }
    val plotHeight = 136.dp
    val gridLines = remember { listOf(1f, .75f, .5f, .25f, 0f) }
    val axisLabels = remember(max) { gridLines.map { compactMoney((max * it).toLong()) } }
    val idle = MaterialTheme.colorScheme.onBackground.copy(alpha = .28f)
    Column(modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth()) {
            // Value axis: baseline sits at the bottom of the plot, where the bars stand.
            Column(
                Modifier.width(34.dp).height(plotHeight).padding(end = 8.dp),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.End,
            ) {
                axisLabels.forEach { label ->
                    Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 8.sp, maxLines = 1)
                }
            }
            Box(Modifier.weight(1f).height(plotHeight)) {
                Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
                    gridLines.forEach { _ ->
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 1.dp)
                    }
                }
                Row(
                    Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.Bottom,
                ) {
                    bars.forEachIndexed { index, (_, amount) ->
                        val target = amount.toFloat() / max
                        val progress by animateFloatAsState(
                            target,
                            tween(500, delayMillis = index * 45, easing = FastOutSlowInEasing),
                            label = "bar-$index",
                        )
                        val isPeak = amount > 0L && amount == peak
                        Box(
                            Modifier.weight(1f)
                                .fillMaxHeight(progress.coerceAtLeast(.012f))
                                .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                                .background(if (isPeak) t.accent else idle),
                        )
                    }
                }
            }
        }
        Row(Modifier.fillMaxWidth().padding(top = 6.dp)) {
            // Reserve the axis gutter so the day labels stay under their bars.
            Spacer(Modifier.width(34.dp))
            bars.forEach { (label, _) ->
                Text(
                    label,
                    Modifier.weight(1f),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 10.sp,
                )
            }
        }
    }
}

@Composable
private fun AwareCategoryBreakdown(
    values: List<Pair<Long?, Long>>,
    categoriesById: Map<Long, CategoryEntity>,
    modifier: Modifier = Modifier,
) {
    val t = LocalTokens.current
    val total = remember(values) { values.sumOf { it.second }.coerceAtLeast(1L) }
    val slices = remember(values, categoriesById, t) {
        values.take(5).mapIndexed { index, (categoryId, amount) ->
            val category = categoryId?.let(categoriesById::get)
            CategorySlice(
                label = category?.name ?: "Other",
                emoji = category?.emoji ?: "•",
                amount = amount,
                color = category?.colorArgb?.let(::Color) ?: t.chart[index % t.chart.size],
            )
        }
    }
    val sweep by animateFloatAsState(
        if (slices.isEmpty()) 0f else 1f,
        tween(700, easing = FastOutSlowInEasing),
        label = "donut-reveal",
    )
    val emptyTrack = MaterialTheme.colorScheme.surface
    Surface(
        modifier.fillMaxWidth(),
        shape = RoundedCornerShape(t.cardRadius),
        color = MaterialTheme.colorScheme.surface,
        border = if (t.outlineWidth > 0.dp) BorderStroke(t.outlineWidth, MaterialTheme.colorScheme.outline) else null,
    ) {
        Column(Modifier.padding(16.dp)) {
            Text("Where it went", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            Spacer(Modifier.height(14.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Canvas(Modifier.size(112.dp)) {
                    val stroke = 20.dp.toPx()
                    val side = kotlin.math.min(size.width, size.height) - stroke
                    val topLeft = androidx.compose.ui.geometry.Offset(stroke / 2f, stroke / 2f)
                    val arcSize = androidx.compose.ui.geometry.Size(side, side)
                    if (slices.isEmpty()) {
                        drawArc(emptyTrack, 0f, 360f, false, topLeft = topLeft, size = arcSize, style = Stroke(stroke))
                    } else {
                        var start = -90f
                        slices.forEach { slice ->
                            val degrees = slice.amount / total.toFloat() * 360f * sweep
                            drawArc(
                                slice.color, start + 1.2f, (degrees - 2.4f).coerceAtLeast(0f), false,
                                topLeft = topLeft, size = arcSize,
                                style = Stroke(stroke, cap = StrokeCap.Butt),
                            )
                            start += degrees
                        }
                    }
                }
                Spacer(Modifier.width(16.dp))
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                    if (slices.isEmpty()) {
                        Text(
                            "No spending in this period yet.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 12.sp,
                        )
                    }
                    slices.forEach { slice ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier.size(9.dp).clip(CircleShape).background(slice.color))
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "${slice.emoji} ${slice.label}",
                                Modifier.weight(1f),
                                fontSize = 12.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                            Text(money(slice.amount), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AwareNamedBreakdown(
    title: String,
    values: List<Pair<String, Long>>,
    total: Long,
    emptyMessage: String = "No spending in this period yet.",
) {
    Surface(Modifier.fillMaxWidth(), shape = awareShape(LocalTokens.current.cardRadius), color = MaterialTheme.colorScheme.surface) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(11.dp)) {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            if (values.isEmpty()) Text(emptyMessage, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
            values.take(6).forEachIndexed { index, (label, amount) ->
                val fraction = if (total <= 0) 0f else amount.toFloat() / total
                Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    Row {
                        Text(label, Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        Text(money(amount), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    LinearProgressIndicator(
                        progress = { fraction.coerceIn(0f, 1f) },
                        modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape),
                        color = LocalTokens.current.chart[index % LocalTokens.current.chart.size],
                        trackColor = MaterialTheme.colorScheme.surfaceVariant,
                    )
                }
            }
        }
    }
}

private data class CategorySlice(val label: String, val emoji: String, val amount: Long, val color: Color)
@Composable
private fun SettingsScreen(
    state: MainUiState,
    smsGranted: Boolean,
    paymentNotificationAccessGranted: Boolean,
    groqConfigured: Boolean,
    appLockEnabled: Boolean,
    smartNudgesEnabled: Boolean,
    onRequestSms: () -> Unit,
    onRequestPaymentNotifications: () -> Unit,
    onToggleNudges: () -> Unit,
    onToggleAppLock: () -> Unit,
    onAddAccount: () -> Unit,
    onOpenCategoryManager: (Boolean) -> Unit,
    onAddGroqKey: () -> Unit,
    onImportStatement: () -> Unit,
    onBackup: () -> Unit,
    onExportCsv: () -> Unit,
    onRestore: () -> Unit,
    onStorage: () -> Unit,
    onOpenSourceNotices: () -> Unit,
    appearance: Appearance,
    onAppearance: () -> Unit,
    skin: Skin,
    onSkin: () -> Unit,
    cozyPalette: CozyPalette,
    onCozyPalette: () -> Unit,
    onCheckForUpdates: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 30.dp)) {
        item {
            Column(Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 20.dp, vertical = 18.dp)) {
                Breadcrumb("settings")
                ScreenTitle("Settings")
                Spacer(Modifier.height(8.dp))
                MachineLabel("private // local // entirely yours")
            }
        }
        item { AwareSettingsSection("LOOK") }
        item {
            AwareSettingsGroup {
                AwareSettingsLink(Icons.Default.Palette, "Theme", skin.label, LocalTokens.current.pink, onSkin)
                AwareSettingsLink(Icons.Default.Contrast, "Appearance", appearance.label, LocalTokens.current.violet, onAppearance)
                if (skin == Skin.COZY) {
                    AwareSettingsLink(Icons.Default.Palette, "Cozy palette", cozyPalette.label, LocalTokens.current.accent, onCozyPalette)
                }
            }
        }
        item { AwareSettingsSection("GENERAL") }
        item {
            AwareSettingsGroup {
                AwareSettingsLink(Icons.Default.AccountBalanceWallet, "Accounts & wallets", "${state.accounts.size} saved", LocalTokens.current.hero, onAddAccount)
                val expenseCategories = state.categories.count { !it.isIncome }
                val incomeCategories = state.categories.count { it.isIncome }
                AwareSettingsLink(
                    Icons.Default.ShoppingBag,
                    "Expense categories",
                    "$expenseCategories saved",
                    LocalTokens.current.warn,
                ) { onOpenCategoryManager(false) }
                AwareSettingsLink(
                    Icons.Default.Savings,
                    "Income categories",
                    "$incomeCategories saved",
                    LocalTokens.current.positive,
                ) { onOpenCategoryManager(true) }
                AwareSettingsValue(Icons.Default.CurrencyRupee, "Currency", "Indian rupee", LocalTokens.current.warn)
                AwareSettingsToggle(Icons.Default.Notifications, "Budget nudges", smartNudgesEnabled, LocalTokens.current.negative, onToggleNudges)
                AwareSettingsToggle(Icons.Default.Lock, "Unlock with biometrics", appLockEnabled, LocalTokens.current.info, onToggleAppLock)
            }
        }
        item { AwareSettingsSection("AUTOMATION") }
        item {
            AwareSettingsGroup {
                AwareSettingsLink(Icons.Default.Sms, "Transaction SMS", if (smsGranted) "On · manage" else "Enable", LocalTokens.current.accent, onRequestSms)
                AwareSettingsLink(Icons.Default.Notifications, "Payment notifications", if (paymentNotificationAccessGranted) "On · manage" else "Enable", LocalTokens.current.info, onRequestPaymentNotifications)
                AwareSettingsLink(Icons.Default.AutoGraph, "AI categorisation", if (groqConfigured) "On" else "Off", LocalTokens.current.positive, onAddGroqKey)
                AwareSettingsLink(Icons.Default.SystemUpdate, "App updates", "v${BuildConfig.VERSION_NAME} · weekly checks", LocalTokens.current.hero, onCheckForUpdates)
            }
        }
        item { AwareSettingsSection("DATA") }
        item {
            AwareSettingsGroup {
                AwareSettingsLink(Icons.Default.FileUpload, "Import bank statement", "CSV · XLS · XLSX", LocalTokens.current.accent, onImportStatement)
                AwareSettingsLink(Icons.Default.FileDownload, "Export data", "CSV", LocalTokens.current.hero, onExportCsv)
                AwareSettingsLink(Icons.Default.Shield, "Encrypted backup", "Create", LocalTokens.current.violet, onBackup)
                AwareSettingsLink(Icons.Default.Restore, "Restore backup", "Choose file", LocalTokens.current.warn, onRestore)
                AwareSettingsLink(Icons.Default.Storage, "Storage", "Encrypted ledger", LocalTokens.current.info, onStorage)
                AwareSettingsLink(Icons.Default.Lightbulb, "Open-source notices", "GPL-3.0", LocalTokens.current.positive, onOpenSourceNotices)
            }
        }
        item {
            val t = LocalTokens.current
            Spacer(Modifier.height(22.dp))
            if (t.maximal) {
                Panel(Modifier.padding(horizontal = 20.dp).fillMaxWidth()) {
                    MachineLabel("system // guarantees")
                    Spacer(Modifier.height(10.dp))
                    val ink = MaterialTheme.colorScheme.onSurface
                    ConsoleBullet("storage local_only", color = ink)
                    Spacer(Modifier.height(5.dp))
                    ConsoleBullet("no ads / no analytics", color = ink)
                    Spacer(Modifier.height(5.dp))
                    ConsoleBullet("ledger encrypted at rest", color = ink)
                    Spacer(Modifier.height(5.dp))
                    ConsoleBullet("backups password sealed", color = ink)
                }
            } else {
                Text(
                    "aware · No ads · No analytics",
                    Modifier.fillMaxWidth(),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 10.sp,
                )
            }
        }
    }
}

@Composable
private fun AwareSettingsSection(title: String) {
    val t = LocalTokens.current
    Row(
        Modifier.fillMaxWidth().padding(start = 22.dp, end = 22.dp, top = 20.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (t.maximal) {
            Box(Modifier.size(7.dp).background(t.accent))
            Spacer(Modifier.width(7.dp))
        }
        Text(
            if (t.maximal) title.uppercase().replace(' ', '_') else title,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.labelSmall,
        )
        if (t.maximal) {
            Spacer(Modifier.width(10.dp))
            HorizontalDivider(Modifier.weight(1f), color = t.frame)
        }
    }
}

@Composable
private fun AwareSettingsGroup(content: @Composable ColumnScope.() -> Unit) {
    val t = LocalTokens.current
    Surface(
        Modifier.padding(horizontal = 20.dp).fillMaxWidth(),
        RoundedCornerShape(if (t.maximal) 0.dp else 12.dp),
        if (t.maximal) t.panel else MaterialTheme.colorScheme.surface,
        border = if (t.maximal) BorderStroke(t.outlineWidth, t.frame) else null,
    ) {
        Column(Modifier.padding(horizontal = 12.dp, vertical = 4.dp), content = content)
    }
}

@Composable
private fun AwareSettingsLink(icon: ImageVector, title: String, value: String, color: Color, onClick: () -> Unit) {
    Row(Modifier.fillMaxWidth().clickable(onClick = onClick).padding(vertical = 9.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(30.dp).clip(awareShape(7.dp)).background(color), contentAlignment = Alignment.Center) {
            Icon(icon, null, Modifier.size(17.dp), tint = readableAccent(LocalTokens.current.onAccent, color))
        }
        Spacer(Modifier.width(11.dp))
        Text(title, Modifier.weight(1f), fontSize = 13.sp, fontWeight = FontWeight.Medium)
        if (value.isNotBlank()) Text(value, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
        Spacer(Modifier.width(5.dp))
        Icon(Icons.Default.ChevronRight, null, Modifier.size(17.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

/**
 * A row that flips a boolean. These used to be [AwareSettingsLink]s showing a
 * chevron, which promised a sub-screen that never existed.
 */
@Composable
private fun AwareSettingsToggle(icon: ImageVector, title: String, checked: Boolean, color: Color, onToggle: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().clickable(onClick = onToggle).padding(vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.size(30.dp).clip(awareShape(7.dp)).background(color), contentAlignment = Alignment.Center) {
            Icon(icon, null, Modifier.size(17.dp), tint = readableAccent(LocalTokens.current.onAccent, color))
        }
        Spacer(Modifier.width(11.dp))
        Text(title, Modifier.weight(1f), fontSize = 13.sp, fontWeight = FontWeight.Medium)
        Switch(
            checked = checked,
            onCheckedChange = { onToggle() },
            modifier = Modifier.graphicsLayer { scaleX = .78f; scaleY = .78f },
        )
    }
}

@Composable
private fun AwareSettingsValue(icon: ImageVector, title: String, value: String, color: Color) {
    Row(Modifier.fillMaxWidth().padding(vertical = 9.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(30.dp).clip(awareShape(7.dp)).background(color), contentAlignment = Alignment.Center) {
            Icon(icon, null, Modifier.size(17.dp), tint = readableAccent(LocalTokens.current.onAccent, color))
        }
        Spacer(Modifier.width(11.dp))
        Text(title, Modifier.weight(1f), fontSize = 13.sp, fontWeight = FontWeight.Medium)
        Text(value, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
    }
}

@Composable
private fun OpenSourceNoticeDialog(onDismiss: () -> Unit) {
    AwareDialog("Open-source notices", onDismiss) {
        Text("aware", style = MaterialTheme.typography.titleLarge)
        Text("The current interface uses aware Compose components and standard Material iconography.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        Surface(Modifier.fillMaxWidth(), awareShape(12.dp), MaterialTheme.colorScheme.surface) {
            Column(Modifier.padding(16.dp)) {
                Text("GNU General Public License v3.0", fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(6.dp))
                Text("You may run, study, share and modify this software under GPL-3.0. It is provided without warranty. Source and license notices ship with the project.", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                Spacer(Modifier.height(8.dp))
                Text("See THIRD_PARTY_NOTICES.md for dependencies and repository history.", color = LocalTokens.current.positive, fontSize = 12.sp)
            }
        }
        Text("Manrope", style = MaterialTheme.typography.titleLarge)
        Text("The embedded geometric typeface is Manrope, distributed under the SIL Open Font License 1.1.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text("Apache POI", style = MaterialTheme.typography.titleLarge)
        Text("Excel statement reading uses Apache POI 5.5.1 under the Apache License 2.0.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        Button(onClick = onDismiss, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.onBackground, contentColor = MaterialTheme.colorScheme.background)) { Text("Done") }
    }
}
@Composable
private fun MonthlyReportDialog(state: MainUiState, onDismiss: () -> Unit) {
    val current = remember(state.transactions) { expenseForMonth(state.transactions, 0) }
    val previous = remember(state.transactions) { expenseForMonth(state.transactions, -1) }
    val difference = current - previous
    val direction = when {
        difference > 0 -> "more"
        difference < 0 -> "less"
        else -> "the same"
    }
    AwareDialog("Monthly comparison", onDismiss) {
        NeoCard(Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.surface) {
            Text("THIS MONTH", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 10.sp, letterSpacing = .7.sp)
            Spacer(Modifier.height(5.dp))
            AnimatedMoneyAmount(current, style = MaterialTheme.typography.headlineLarge)
        }
        NeoCard(Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.surface) {
            Text("PREVIOUS MONTH", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 10.sp, letterSpacing = .7.sp)
            Spacer(Modifier.height(5.dp))
            AnimatedMoneyAmount(previous, style = MaterialTheme.typography.headlineLarge)
        }
        Text(
            if (difference == 0L) "Spending is $direction as last month."
            else "You spent ${money(kotlin.math.abs(difference))} $direction than last month.",
            color = if (difference <= 0) LocalTokens.current.positive else LocalTokens.current.warn,
            fontWeight = FontWeight.SemiBold,
        )
        Button(
            onClick = onDismiss,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = LocalTokens.current.affirm, contentColor = LocalTokens.current.onAffirm),
        ) { Text("DONE") }
    }
}

private fun expenseForMonth(transactions: List<TransactionEntity>, offset: Int): Long {
    val target = Calendar.getInstance().apply { add(Calendar.MONTH, offset) }
    val targetMonth = target.get(Calendar.MONTH)
    val targetYear = target.get(Calendar.YEAR)
    val calendar = Calendar.getInstance()
    return transactions.asSequence()
        .filter { it.type == TransactionType.EXPENSE }
        .filter {
            calendar.timeInMillis = it.occurredAt
            calendar.get(Calendar.MONTH) == targetMonth && calendar.get(Calendar.YEAR) == targetYear
        }
        .sumOf { it.amountPaise }
}

private fun summarizePeriod(transactions: List<TransactionEntity>, period: SummaryPeriod): PeriodSnapshot {
    val zone = ZoneId.systemDefault()
    val now = java.time.ZonedDateTime.now(zone)
    val start = when (period) {
        SummaryPeriod.TODAY -> now.toLocalDate().atStartOfDay(zone)
        SummaryPeriod.WEEK -> now.toLocalDate().minusDays((now.dayOfWeek.value - 1).toLong()).atStartOfDay(zone)
        SummaryPeriod.MONTH -> now.toLocalDate().withDayOfMonth(1).atStartOfDay(zone)
    }
    val currentStart = start.toInstant().toEpochMilli()
    val currentEnd = now.toInstant().toEpochMilli() + 1
    val elapsed = (currentEnd - currentStart).coerceAtLeast(1L)
    val previousStartDateTime = when (period) {
        SummaryPeriod.TODAY -> start.minusDays(1)
        SummaryPeriod.WEEK -> start.minusWeeks(1)
        SummaryPeriod.MONTH -> start.minusMonths(1)
    }
    val previousStart = previousStartDateTime.toInstant().toEpochMilli()
    val previousEnd = (previousStart + elapsed).coerceAtMost(currentStart)
    fun inCurrent(item: TransactionEntity) = item.occurredAt in currentStart until currentEnd
    val income = transactions.filter { inCurrent(it) && it.type == TransactionType.INCOME }.sumOf { it.amountPaise }
    val refunds = transactions.filter { inCurrent(it) && it.type == TransactionType.REFUND }.sumOf { it.amountPaise }
    val expense = transactions.filter { inCurrent(it) && it.type == TransactionType.EXPENSE }.sumOf { it.amountPaise }
    val previousExpense = transactions.filter { it.occurredAt in previousStart until previousEnd && it.type == TransactionType.EXPENSE }.sumOf { it.amountPaise }
    return PeriodSnapshot(income, expense, refunds, previousExpense)
}

private fun transactionsForPeriod(transactions: List<TransactionEntity>, period: SummaryPeriod): List<TransactionEntity> {
    val zone = ZoneId.systemDefault()
    val now = java.time.ZonedDateTime.now(zone)
    val start = when (period) {
        SummaryPeriod.TODAY -> now.toLocalDate().atStartOfDay(zone)
        SummaryPeriod.WEEK -> now.toLocalDate().minusDays((now.dayOfWeek.value - 1).toLong()).atStartOfDay(zone)
        SummaryPeriod.MONTH -> now.toLocalDate().withDayOfMonth(1).atStartOfDay(zone)
    }.toInstant().toEpochMilli()
    val end = now.toInstant().toEpochMilli() + 1
    return transactions.filter { it.occurredAt in start until end }.sortedByDescending { it.occurredAt }
}

private fun periodLabel(period: SummaryPeriod): String {
    val today = LocalDate.now()
    return when (period) {
        SummaryPeriod.TODAY -> today.format(DateTimeFormatter.ofPattern("d MMM"))
        SummaryPeriod.WEEK -> {
            val start = today.minusDays((today.dayOfWeek.value - 1).toLong())
            "${start.format(DateTimeFormatter.ofPattern("d MMM"))} – ${today.format(DateTimeFormatter.ofPattern("d MMM"))}"
        }
        SummaryPeriod.MONTH -> today.format(DateTimeFormatter.ofPattern("MMMM yyyy"))
    }
}

private fun insightBars(expenses: List<TransactionEntity>, period: SummaryPeriod): List<Pair<String, Long>> {
    val zone = ZoneId.systemDefault()
    val today = LocalDate.now()
    return when (period) {
        SummaryPeriod.TODAY -> {
            val buckets = List(6) { 0L }.toMutableList()
            expenses.forEach { transaction ->
                val hour = Instant.ofEpochMilli(transaction.occurredAt).atZone(zone).hour
                buckets[(hour / 4).coerceIn(0, 5)] += transaction.amountPaise
            }
            buckets.mapIndexed { index, amount -> listOf("12a", "4a", "8a", "12p", "4p", "8p")[index] to amount }
        }
        SummaryPeriod.WEEK -> {
            val start = today.minusDays((today.dayOfWeek.value - 1).toLong())
            (0L..6L).map { offset ->
                val date = start.plusDays(offset)
                date.dayOfWeek.name.take(1) to expenses.filter { Instant.ofEpochMilli(it.occurredAt).atZone(zone).toLocalDate() == date }.sumOf { it.amountPaise }
            }
        }
        SummaryPeriod.MONTH -> {
            (0..4).map { week ->
                val from = week * 7 + 1
                val to = minOf(from + 6, YearMonth.now().lengthOfMonth())
                "W${week + 1}" to expenses.filter {
                    Instant.ofEpochMilli(it.occurredAt).atZone(zone).toLocalDate().dayOfMonth in from..to
                }.sumOf { it.amountPaise }
            }
        }
    }
}
@Composable
private fun MoneyMoveChooser(onDismiss: () -> Unit, onChoose: (TransactionType) -> Unit) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false),
    ) {
        Box(
            Modifier.fillMaxSize().background(Color.Black.copy(alpha = .62f))
                .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onDismiss),
            contentAlignment = Alignment.BottomCenter,
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth().clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { },
                shape = RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp),
                color = MaterialTheme.colorScheme.surface,
            ) {
                Column(
                    Modifier.navigationBarsPadding().padding(horizontal = 20.dp, vertical = 22.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    Box(Modifier.width(38.dp).height(4.dp).clip(CircleShape).background(MaterialTheme.colorScheme.outline).align(Alignment.CenterHorizontally))
                    Text("What moved?", style = MaterialTheme.typography.headlineMedium)
                    Text("Choose a type. The next screen will already be configured for it.", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        MoneyMoveOption("Expense", "Money out", Icons.Default.ArrowOutward, LocalTokens.current.negative, Modifier.weight(1f)) { onChoose(TransactionType.EXPENSE) }
                        MoneyMoveOption("Income", "Money in", Icons.Default.SouthWest, LocalTokens.current.positive, Modifier.weight(1f)) { onChoose(TransactionType.INCOME) }
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        MoneyMoveOption("Transfer", "Between accounts", Icons.Default.SwapHoriz, LocalTokens.current.hero, Modifier.weight(1f)) { onChoose(TransactionType.TRANSFER) }
                        MoneyMoveOption("Refund", "Money returned", Icons.AutoMirrored.Filled.Undo, LocalTokens.current.warn, Modifier.weight(1f)) { onChoose(TransactionType.REFUND) }
                    }
                    TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) { Text("Cancel") }
                }
            }
        }
    }
}

@Composable
private fun MoneyMoveOption(
    title: String,
    subtitle: String,
    icon: ImageVector,
    accent: Color,
    modifier: Modifier,
    onClick: () -> Unit,
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) .96f else 1f, spring(dampingRatio = .72f, stiffness = Spring.StiffnessMedium), label = "move-$title")
    Surface(
        modifier.graphicsLayer { scaleX = scale; scaleY = scale }
            .clickable(interactionSource = interaction, indication = null, onClick = onClick),
        shape = awareShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
    ) {
        Column(Modifier.padding(16.dp)) {
            Box(Modifier.size(38.dp).clip(CircleShape).background(accent), contentAlignment = Alignment.Center) {
                Icon(icon, null, Modifier.size(19.dp), tint = readableAccent(LocalTokens.current.onAccent, accent))
            }
            Spacer(Modifier.height(16.dp))
            Text(title, fontWeight = FontWeight.SemiBold)
            Text(subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 10.sp)
        }
    }
}

@Composable
private fun TransactionDetailDialog(
    transaction: TransactionEntity,
    state: MainUiState,
    onDismiss: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    var confirmDelete by remember(transaction.id) { mutableStateOf(false) }
    val category = transaction.categoryId?.let { id -> state.categories.firstOrNull { it.id == id } }
    val account = state.accounts.firstOrNull { it.id == transaction.accountId }
    val destination = transaction.destinationAccountId?.let { id -> state.accounts.firstOrNull { it.id == id } }
    val incoming = transaction.type == TransactionType.INCOME || transaction.type == TransactionType.REFUND
    val accent = if (incoming) LocalTokens.current.positive else if (transaction.type == TransactionType.TRANSFER) LocalTokens.current.info else LocalTokens.current.negative
    val dateTime = remember(transaction.occurredAt) {
        DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy · h:mm a", Locale.ENGLISH)
            .format(Instant.ofEpochMilli(transaction.occurredAt).atZone(ZoneId.systemDefault()))
    }

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)) {
        LightDialogSystemBars()
        Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            Column(
                Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding().verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 12.dp),
            ) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onDismiss, modifier = Modifier.size(40.dp)) { Icon(Icons.Default.Close, "Close details") }
                    Spacer(Modifier.weight(1f))
                    Text("Transaction details", fontWeight = FontWeight.Bold)
                    Spacer(Modifier.weight(1f))
                    Box(Modifier.size(40.dp))
                }
                Spacer(Modifier.height(22.dp))
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = awareShape(24.dp),
                    color = accent.copy(alpha = .18f),
                    border = if (LocalTokens.current.maximal) BorderStroke(1.dp, accent) else null,
                ) {
                    Column(Modifier.padding(horizontal = 20.dp, vertical = 24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(transaction.type.name.lowercase().replaceFirstChar(Char::uppercase), color = readableAccent(accent, accent.copy(alpha = .18f).compositeOver(MaterialTheme.colorScheme.background)), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Spacer(Modifier.height(8.dp))
                        AnimatedMoneyAmount(
                            transaction.amountPaise,
                            style = MaterialTheme.typography.displayMedium,
                            color = MaterialTheme.colorScheme.onBackground,
                        )
                        Spacer(Modifier.height(7.dp))
                        Text(transaction.merchant, style = MaterialTheme.typography.titleMedium, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                    }
                }
                Spacer(Modifier.height(20.dp))
                AwareSettingsGroup {
                    TransactionDetailRow("Date", dateTime)
                    TransactionDetailRow("Status", transaction.status.name.lowercase().replace('_', ' ').replaceFirstChar(Char::uppercase))
                    TransactionDetailRow("Account", account?.name ?: "Unknown account")
                    if (transaction.type == TransactionType.TRANSFER) TransactionDetailRow("Destination", destination?.name ?: "Unknown account")
                    if (transaction.type != TransactionType.TRANSFER) TransactionDetailRow("Category", category?.let { "${it.emoji} ${it.name}" } ?: "Uncategorized")
                    TransactionDetailRow("Source", transaction.source.name.lowercase().replaceFirstChar(Char::uppercase))
                }
                if (transaction.note.isNotBlank()) {
                    Spacer(Modifier.height(16.dp))
                    AwareSettingsSection("Note")
                    Surface(Modifier.fillMaxWidth(), awareShape(14.dp), MaterialTheme.colorScheme.surface) {
                        Text(transaction.note, Modifier.padding(16.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                if (transaction.tags.isNotBlank()) {
                    Spacer(Modifier.height(16.dp))
                    AwareSettingsSection("Tags")
                    Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        transaction.tags.split(',').map(String::trim).filter(String::isNotBlank).forEach { tag ->
                            Surface(shape = awareShape(50.dp), color = MaterialTheme.colorScheme.surfaceVariant) {
                                Text(tag, Modifier.padding(horizontal = 12.dp, vertical = 8.dp), color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                            }
                        }
                    }
                }
                Spacer(Modifier.height(24.dp))
                Button(
                    onClick = onEdit,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = awareShape(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = LocalTokens.current.affirm, contentColor = LocalTokens.current.onAffirm),
                ) {
                    Icon(Icons.Default.Edit, null, Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Edit transaction", fontWeight = FontWeight.Bold)
                }
                TextButton(onClick = { confirmDelete = true }, modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
                    Icon(Icons.Default.Delete, null, Modifier.size(18.dp), tint = MaterialTheme.colorScheme.error)
                    Spacer(Modifier.width(8.dp))
                    Text("Delete transaction", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
    if (confirmDelete) AlertDialog(
        onDismissRequest = { confirmDelete = false },
        title = { Text("Delete this transaction?") },
        text = { Text("${transaction.merchant} · ${money(transaction.amountPaise)} will be permanently removed from your ledger.") },
        confirmButton = { TextButton(onClick = onDelete) { Text("Delete", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold) } },
        dismissButton = { TextButton(onClick = { confirmDelete = false }) { Text("Keep it") } },
    )
}

@Composable
private fun TransactionDetailRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 13.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(label, Modifier.width(92.dp), color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
        Text(value, Modifier.weight(1f), fontWeight = FontWeight.SemiBold, textAlign = androidx.compose.ui.text.style.TextAlign.End)
    }
}

@Composable
private fun TransactionConfirmationOverlay(
    confirmation: TransactionConfirmation,
    onFinished: () -> Unit,
) {
    val progress = remember(confirmation) { Animatable(0f) }
    LaunchedEffect(confirmation) {
        progress.animateTo(1f, animationSpec = spring(dampingRatio = .58f, stiffness = Spring.StiffnessLow))
        delay(1_250)
        onFinished()
    }
    val incoming = confirmation.type == TransactionType.INCOME || confirmation.type == TransactionType.REFUND
    val accent = if (incoming) LocalTokens.current.positive else if (confirmation.type == TransactionType.TRANSFER) LocalTokens.current.info else LocalTokens.current.accent
    Dialog(onDismissRequest = onFinished, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = .48f)).padding(28.dp), contentAlignment = Alignment.Center) {
            Surface(
                modifier = Modifier.fillMaxWidth().widthIn(max = 360.dp)
                    .graphicsLayer {
                        val value = progress.value
                        alpha = value.coerceIn(0f, 1f)
                        scaleX = .82f + (.18f * value)
                        scaleY = .82f + (.18f * value)
                    },
                shape = awareShape(26.dp),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 18.dp,
            ) {
                Column(Modifier.padding(horizontal = 24.dp, vertical = 30.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(contentAlignment = Alignment.Center) {
                        Canvas(Modifier.size(108.dp)) {
                            drawCircle(accent.copy(alpha = .14f), radius = size.minDimension * .5f * progress.value)
                            drawCircle(accent, radius = size.minDimension * .34f, style = Stroke(width = 5.dp.toPx()))
                        }
                        Icon(
                            Icons.Default.Check,
                            contentDescription = "Transaction saved",
                            tint = accent,
                            modifier = Modifier.size(48.dp).graphicsLayer {
                                scaleX = progress.value
                                scaleY = progress.value
                                rotationZ = (1f - progress.value) * -18f
                            },
                        )
                    }
                    Spacer(Modifier.height(12.dp))
                    Text(
                        when (confirmation.type) {
                            TransactionType.INCOME -> "Income recorded"
                            TransactionType.TRANSFER -> "Transfer recorded"
                            TransactionType.REFUND -> "Refund recorded"
                            else -> "Payment recorded"
                        },
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                    )
                    Spacer(Modifier.height(7.dp))
                    Text(money(confirmation.amountPaise), style = MaterialTheme.typography.headlineMedium, color = accent, fontWeight = FontWeight.Bold)
                    Text(confirmation.merchant.ifBlank { "Saved to your ledger" }, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
        }
    }
}

@Composable
private fun AddTransactionDialog(
    state: MainUiState,
    initialType: TransactionType,
    transaction: TransactionEntity? = null,
    onDismiss: () -> Unit,
    onAddCategory: (Boolean, (Long) -> Unit) -> Unit,
    onAddAccount: () -> Unit,
    onSave: (Long, String, TransactionType, Long, Long?, Long?, String, String, Long) -> Unit,
) {
    var amount by remember(transaction?.id) { mutableStateOf(transaction?.amountPaise?.let(::editableMoney) ?: "") }
    var merchant by remember(transaction?.id) { mutableStateOf(transaction?.merchant.orEmpty()) }
    var note by remember(transaction?.id) { mutableStateOf(transaction?.note.orEmpty()) }
    var tags by remember(transaction?.id) { mutableStateOf(transaction?.tags.orEmpty()) }
    var type by remember(transaction?.id, initialType) { mutableStateOf(transaction?.type ?: initialType) }
    var accountId by remember(state.accounts, transaction?.id) {
        mutableLongStateOf(transaction?.accountId ?: state.accounts.firstOrNull { it.isDefault }?.id ?: state.accounts.firstOrNull()?.id ?: 0L)
    }
    var destination by remember(transaction?.id) { mutableStateOf(transaction?.destinationAccountId) }
    var categoryId by remember(transaction?.id) { mutableStateOf(transaction?.categoryId) }
    var occurredAt by remember(transaction?.id) { mutableLongStateOf(transaction?.occurredAt ?: startOfTodayMillis()) }
    var showDatePicker by remember { mutableStateOf(false) }
    val paise = parsePaise(amount)
    val canSave = paise != null && paise > 0 && accountId > 0 && (type != TransactionType.TRANSFER || destination != null)
    fun changeType(nextType: TransactionType) {
        if (nextType != type) {
            type = nextType
            categoryId = null
            destination = null
        }
    }
    fun appendAmount(key: String) {
        when (key) {
            "⌫" -> amount = amount.dropLast(1)
            "." -> if (!amount.contains('.')) amount = if (amount.isBlank()) "0." else "$amount."
            else -> {
                val decimals = amount.substringAfter('.', "")
                val digitCount = amount.count(Char::isDigit)
                if (digitCount < 9 && (!amount.contains('.') || decimals.length < 2)) {
                    amount = if (amount == "0") key else amount + key
                }
            }
        }
    }
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)) {
        LightDialogSystemBars()
        Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            Column(
                Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding().verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 12.dp),
            ) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onDismiss, modifier = Modifier.size(40.dp)) {
                        Icon(Icons.Default.Close, "Close")
                    }
                    Spacer(Modifier.weight(1f))
                    Text(if (transaction == null) "New transaction" else "Edit transaction", fontWeight = FontWeight.Bold)
                    Spacer(Modifier.weight(1f))
                    Box(Modifier.size(40.dp))
                }
                Spacer(Modifier.height(10.dp))
                ChoiceRow(
                    listOf(TransactionType.EXPENSE, TransactionType.INCOME, TransactionType.TRANSFER, TransactionType.REFUND),
                    selected = type,
                    label = { it.name.lowercase().replaceFirstChar(Char::uppercase) },
                    onSelected = ::changeType,
                )
                Spacer(Modifier.height(24.dp))
                Text(
                    "₹" + if (amount.isBlank()) "0" else amount,
                    Modifier.fillMaxWidth(),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    style = MaterialTheme.typography.displayLarge,
                    color = if (amount.isBlank()) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onBackground,
                    maxLines = 1,
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    merchant, { merchant = it }, Modifier.fillMaxWidth(),
                    placeholder = { Text(when (type) {
                        TransactionType.INCOME, TransactionType.REFUND -> "Payer or income source"
                        TransactionType.TRANSFER -> "Transfer label (optional)"
                        else -> "Payee or merchant"
                    }) },
                    leadingIcon = { Icon(Icons.Default.Edit, null, Modifier.size(18.dp)) },
                    colors = cozyFieldColors(), shape = awareShape(10.dp), singleLine = true,
                )
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(
                    note, { note = it }, Modifier.fillMaxWidth(),
                    placeholder = { Text("Add a note (optional)") },
                    colors = cozyFieldColors(), shape = awareShape(10.dp), singleLine = true,
                )
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(
                    tags, { tags = it }, Modifier.fillMaxWidth(),
                    placeholder = { Text("Custom tags, separated by commas") },
                    colors = cozyFieldColors(), shape = awareShape(10.dp), singleLine = true,
                )
                Spacer(Modifier.height(14.dp))
                if (type !in listOf(TransactionType.TRANSFER)) {
                    val available = state.categories.filter { if (type == TransactionType.INCOME || type == TransactionType.REFUND) it.isIncome else !it.isIncome }
                    AwareCategoryPicker(
                        available, categoryId,
                        onAdd = {
                            onAddCategory(type == TransactionType.INCOME || type == TransactionType.REFUND) { createdId ->
                                categoryId = createdId
                            }
                        },
                    ) { categoryId = it }
                    Spacer(Modifier.height(13.dp))
                }
                Label("Account")
                ScrollChoices(state.accounts, accountId, { it.id }, { it.name }, onAdd = onAddAccount) { account ->
                    accountId = account.id
                    if (destination == account.id) destination = null
                }
                if (type == TransactionType.TRANSFER) {
                    Spacer(Modifier.height(10.dp))
                    Label("To account / cash wallet")
                    ScrollChoices(state.accounts.filter { it.id != accountId }, destination, { it.id }, { it.name }, onAdd = onAddAccount) { destination = it.id }
                }
                Row(
                    Modifier.fillMaxWidth().padding(top = 14.dp, bottom = 8.dp)
                        .clip(awareShape(10.dp))
                        .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { showDatePicker = true }
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(Icons.AutoMirrored.Filled.ReceiptLong, null, Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.width(7.dp))
                    Text("${friendlyDate(occurredAt)} · ${state.accounts.firstOrNull { it.id == accountId }?.name ?: "Choose account"}", Modifier.weight(1f), color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                    Text("Change", color = LocalTokens.current.accent, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
                AwareNumberPad(
                    enabled = canSave,
                    submitLabel = if (transaction != null) "Save changes" else when (type) {
                        TransactionType.INCOME -> "Add income"
                        TransactionType.TRANSFER -> "Record transfer"
                        TransactionType.REFUND -> "Record refund"
                        else -> "Add expense"
                    },
                    onKey = ::appendAmount,
                    onSubmit = { if (canSave) onSave(paise ?: 0, merchant, type, accountId, destination, categoryId, note, tags, occurredAt) },
                )
            }
        }
    }
    if (showDatePicker) AwareDatePicker(
        title = "Transaction date",
        initialMillis = occurredAt,
        onDismiss = { showDatePicker = false },
        onConfirm = { occurredAt = it; showDatePicker = false },
    )
}

@Composable
private fun AwareCategoryPicker(categories: List<CategoryEntity>, selectedId: Long?, onAdd: (() -> Unit)? = null, onSelected: (Long) -> Unit) {
    val t = LocalTokens.current
    val listState = rememberLazyListState()
    LaunchedEffect(selectedId, categories.map(CategoryEntity::id)) {
        val selectedIndex = categories.indexOfFirst { it.id == selectedId }
        if (selectedIndex >= 0) listState.animateScrollToItem(selectedIndex)
    }
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        state = listState,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(categories, key = CategoryEntity::id) { category ->
            val selected = selectedId == category.id
            val color = Color(category.colorArgb)
            // Maximal follows the reference: outlined chip, category colour in
            // the text and the ring. Cozy keeps its soft tinted fill.
            val fill = when {
                t.maximal && selected -> color.copy(alpha = .18f)
                t.maximal -> Color.Transparent
                else -> color.copy(if (selected) .4f else .22f)
            }
            Surface(
                modifier = Modifier.clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { onSelected(category.id) },
                shape = awareShape(10.dp),
                color = fill,
                border = when {
                    t.maximal -> BorderStroke(if (selected) 1.5.dp else 1.dp, color.copy(alpha = if (selected) 1f else .55f))
                    selected -> BorderStroke(1.5.dp, color)
                    else -> null
                },
            ) {
                Text(
                    if (t.maximal) "${category.emoji} ${category.name.uppercase()}" else "${category.emoji} ${category.name}",
                    Modifier.padding(horizontal = 11.dp, vertical = 8.dp),
                    color = if (t.maximal) color else readableAccent(color, fill.compositeOver(MaterialTheme.colorScheme.background)),
                    style = if (t.maximal) MaterialTheme.typography.labelSmall else MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
        onAdd?.let { add ->
            item(key = "add-category") {
                Surface(
                    modifier = Modifier.clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = add),
                    shape = awareShape(10.dp), color = t.accent,
                ) {
                    Text(
                        if (t.maximal) "+ NEW" else "＋ New",
                        Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        color = t.onAccent,
                        style = if (t.maximal) MaterialTheme.typography.labelSmall else MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }
    }
}

@Composable
private fun AwareNumberPad(enabled: Boolean, submitLabel: String, onKey: (String) -> Unit, onSubmit: () -> Unit) {
    val t = LocalTokens.current
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        listOf(listOf("1", "2", "3"), listOf("4", "5", "6"), listOf("7", "8", "9"), listOf(".", "0", "⌫")).forEach { row ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                row.forEach { key -> AwareKey(key, Modifier.weight(1f)) { onKey(key) } }
            }
        }
        val interaction = remember { MutableInteractionSource() }
        val pressed by interaction.collectIsPressedAsState()
        val scale by animateFloatAsState(
            if (pressed && enabled) .97f else 1f,
            spring(dampingRatio = .72f, stiffness = Spring.StiffnessMedium),
            label = "pad-submit",
        )
        val haptics = LocalHapticFeedback.current
        Surface(
            modifier = Modifier.fillMaxWidth().height(54.dp).padding(top = 4.dp)
                .graphicsLayer { scaleX = scale; scaleY = scale }
                .clickable(enabled = enabled, interactionSource = interaction, indication = null) {
                    haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onSubmit()
                },
            shape = awareShape(50.dp),
            // A disabled submit must read as disabled; it used to keep its full
            // filled styling and silently swallow taps.
            color = if (enabled) t.affirm else MaterialTheme.colorScheme.surfaceVariant,
            contentColor = if (enabled) t.onAffirm else MaterialTheme.colorScheme.onSurfaceVariant,
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    if (enabled) submitLabel else "Enter an amount to continue",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}

@Composable
private fun AwareKey(label: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) .93f else 1f, spring(dampingRatio = .72f, stiffness = Spring.StiffnessMedium), label = "key-$label")
    val haptics = LocalHapticFeedback.current
    Surface(
        modifier = modifier.height(54.dp).graphicsLayer { scaleX = scale; scaleY = scale }
            .clickable(interactionSource = interaction, indication = null) {
                haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onClick()
            },
        shape = awareShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(label, fontSize = if (label == "⌫") 20.sp else 26.sp, fontWeight = FontWeight.Medium)
        }
    }
}

private fun startOfTodayMillis(): Long = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

private fun friendlyDate(millis: Long): String {
    val date = Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate()
    return when (date) {
        LocalDate.now() -> "Today"
        LocalDate.now().minusDays(1) -> "Yesterday"
        else -> date.format(DateTimeFormatter.ofPattern("d MMM yyyy"))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AwareDatePicker(
    title: String,
    initialMillis: Long,
    onDismiss: () -> Unit,
    onConfirm: (Long) -> Unit,
) {
    val localDate = Instant.ofEpochMilli(initialMillis).atZone(ZoneId.systemDefault()).toLocalDate()
    val pickerMillis = localDate.atStartOfDay(ZoneId.of("UTC")).toInstant().toEpochMilli()
    val state = rememberDatePickerState(initialSelectedDateMillis = pickerMillis)
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                state.selectedDateMillis?.let { selected ->
                    val selectedDate = Instant.ofEpochMilli(selected).atZone(ZoneId.of("UTC")).toLocalDate()
                    onConfirm(selectedDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli())
                }
            }, enabled = state.selectedDateMillis != null) {
                Text("Done", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    ) {
        DatePicker(state = state, title = { Text(title, Modifier.padding(start = 24.dp, top = 16.dp)) })
    }
}

@Composable
private fun BudgetDialog(
    state: MainUiState,
    onDismiss: () -> Unit,
    onAddCategory: () -> Unit,
    onSave: (String, Long, BudgetScope, Long?, Long?, String?, BudgetPeriod, Long, Long?, Boolean) -> Unit,
) {
    var name by remember { mutableStateOf("") }
    var cap by remember { mutableStateOf("") }
    var scope by remember { mutableStateOf(BudgetScope.OVERALL) }
    var categoryId by remember { mutableStateOf<Long?>(null) }
    var accountId by remember(state.accounts) { mutableStateOf(state.accounts.firstOrNull { it.isDefault }?.id ?: state.accounts.firstOrNull()?.id) }
    var payee by remember { mutableStateOf("") }
    var period by remember { mutableStateOf(BudgetPeriod.MONTHLY) }
    var isRecurring by remember { mutableStateOf(true) }
    var startAt by remember { mutableLongStateOf(startOfTodayMillis()) }
    var endAt by remember { mutableStateOf<Long?>(null) }
    var pickingStart by remember { mutableStateOf<Boolean?>(null) }
    val paise = parsePaise(cap)
    AwareDialog("Create a budget", onDismiss) {
        OutlinedTextField(name, { name = it }, Modifier.fillMaxWidth(), label = { Text("Cap name") }, colors = cozyFieldColors(), singleLine = true)
        MoneyField(cap) { cap = it }
        Label("Scope")
        ChoiceRow(BudgetScope.entries, scope) { scope = it }
        when (scope) {
            BudgetScope.CATEGORY -> {
                Label("Category")
                val spendingCategories = state.categories.filterNot { it.isIncome }
                AwareCategoryPicker(spendingCategories, categoryId, onAdd = onAddCategory) { selected ->
                    categoryId = selected
                    if (name.isBlank()) name = spendingCategories.firstOrNull { it.id == selected }?.name.orEmpty()
                }
            }
            BudgetScope.ACCOUNT -> {
                Label("Account")
                ScrollChoices(state.accounts, accountId, { it.id }, { it.name }) { accountId = it.id }
            }
            BudgetScope.PAYEE -> OutlinedTextField(payee, { payee = it }, Modifier.fillMaxWidth(), label = { Text("Payee or merchant") }, colors = cozyFieldColors(), singleLine = true)
            BudgetScope.OVERALL -> Text("Tracks all posted expenses.", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
        }
        Label("Period")
        ChoiceRow(BudgetPeriod.entries, period) { period = it }
        ChoiceRow(listOf(true, false), isRecurring, label = { if (it) "Recurring" else "One time" }) { isRecurring = it }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = { pickingStart = true }, Modifier.weight(1f)) { Text("Starts ${friendlyDate(startAt)}", maxLines = 1) }
            OutlinedButton(onClick = { pickingStart = false }, Modifier.weight(1f)) { Text(endAt?.let { "Ends ${friendlyDate(it)}" } ?: "No end", maxLines = 1) }
        }
        Text("Smart nudges fire at 50%, 75%, 90%, and 100%.", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
        val scoped = scope != BudgetScope.CATEGORY || categoryId != null
        val accountReady = scope != BudgetScope.ACCOUNT || accountId != null
        val payeeReady = scope != BudgetScope.PAYEE || payee.isNotBlank()
        Button(onClick = { onSave(name, paise ?: 0, scope, categoryId, accountId, payee, period, startAt, endAt, isRecurring) }, enabled = name.isNotBlank() && paise != null && paise > 0 && scoped && accountReady && payeeReady, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = LocalTokens.current.affirm, contentColor = LocalTokens.current.onAffirm)) { Text("Create budget") }
    }
    pickingStart?.let { start -> AwareDatePicker(
        title = if (start) "Budget starts" else "Budget ends",
        initialMillis = if (start) startAt else endAt ?: startAt,
        onDismiss = { pickingStart = null },
        onConfirm = { if (start) startAt = it else endAt = it; pickingStart = null },
    ) }
}

@Composable
private fun RecurringDialog(
    state: MainUiState,
    onDismiss: () -> Unit,
    onAddCategory: (Boolean) -> Unit,
    onAddAccount: () -> Unit,
    onSave: (String, Long, TransactionType, Long, Long?, RecurrenceCadence, Int, Long, Long?, Int) -> Unit,
) {
    var name by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var type by remember { mutableStateOf(TransactionType.EXPENSE) }
    var accountId by remember(state.accounts) { mutableLongStateOf(state.accounts.firstOrNull { it.isDefault }?.id ?: 0L) }
    var categoryId by remember { mutableStateOf<Long?>(null) }
    var cadence by remember { mutableStateOf(RecurrenceCadence.MONTHLY) }
    var intervalDays by remember { mutableStateOf("14") }
    var startAt by remember { mutableLongStateOf(startOfTodayMillis()) }
    var endAt by remember { mutableStateOf<Long?>(null) }
    var reminder by remember { mutableIntStateOf(0) }
    var pickingStart by remember { mutableStateOf<Boolean?>(null) }
    val paise = parsePaise(amount)
    AwareDialog("Expect it again", onDismiss) {
        ChoiceRow(listOf(TransactionType.EXPENSE, TransactionType.INCOME), type) { nextType ->
            if (nextType != type) {
                type = nextType
                categoryId = null
            }
        }
        OutlinedTextField(name, { name = it }, Modifier.fillMaxWidth(), label = { Text("Name") }, colors = cozyFieldColors(), singleLine = true)
        MoneyField(amount) { amount = it }
        Label("Repeats")
        ChoiceRow(RecurrenceCadence.entries, cadence) { cadence = it }
        if (cadence == RecurrenceCadence.CUSTOM) {
            OutlinedTextField(
                intervalDays, { intervalDays = it.filter(Char::isDigit).take(3) }, Modifier.fillMaxWidth(),
                label = { Text("Every how many days?") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                colors = cozyFieldColors(), singleLine = true,
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = { pickingStart = true }, Modifier.weight(1f)) { Text("Next ${friendlyDate(startAt)}", maxLines = 1) }
            OutlinedButton(onClick = { pickingStart = false }, Modifier.weight(1f)) { Text(endAt?.let { "Ends ${friendlyDate(it)}" } ?: "Never ends", maxLines = 1) }
        }
        Label("Reminder")
        ChoiceRow(listOf(0, 60, 180, 1440), reminder, label = { when (it) { 0 -> "None"; 60 -> "1 hour"; 180 -> "3 hours"; else -> "1 day" } }) { reminder = it }
        Label("Account")
        ScrollChoices(state.accounts, accountId, { it.id }, { it.name }, onAdd = onAddAccount) { accountId = it.id }
        Label("Category")
        AwareCategoryPicker(
            state.categories.filter { it.isIncome == (type == TransactionType.INCOME) },
            categoryId,
            onAdd = { onAddCategory(type == TransactionType.INCOME) },
        ) { categoryId = it }
        Text("aware creates an expected item and waits for a matching SMS or your confirmation.", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
        Button(onClick = { onSave(name, paise ?: 0, type, accountId, categoryId, cadence, intervalDays.toIntOrNull() ?: 1, startAt, endAt, reminder) }, enabled = name.isNotBlank() && paise != null && paise > 0 && accountId > 0 && (cadence != RecurrenceCadence.CUSTOM || (intervalDays.toIntOrNull() ?: 0) > 0), modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = LocalTokens.current.affirm, contentColor = LocalTokens.current.onAffirm)) { Text("Add expectation") }
    }
    pickingStart?.let { start -> AwareDatePicker(
        title = if (start) "Next due" else "Recurrence ends",
        initialMillis = if (start) startAt else endAt ?: startAt,
        onDismiss = { pickingStart = null },
        onConfirm = { if (start) startAt = it else endAt = it; pickingStart = null },
    ) }
}

@Composable
private fun AccountDialog(onDismiss: () -> Unit, onSave: (String, AccountKind, Long) -> Unit) {
    var name by remember { mutableStateOf("") }
    var balance by remember { mutableStateOf("") }
    var kind by remember { mutableStateOf(AccountKind.BANK) }
    val opening = parsePaise(balance) ?: 0L
    AwareDialog("Add money source", onDismiss) {
        ChoiceRow(AccountKind.entries, kind) { kind = it }
        OutlinedTextField(name, { name = it }, Modifier.fillMaxWidth(), label = { Text("Account or wallet name") }, colors = cozyFieldColors(), singleLine = true)
        MoneyField(balance, "Opening balance") { balance = it }
        Button(onClick = { onSave(name, kind, opening) }, enabled = name.isNotBlank(), modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = LocalTokens.current.affirm, contentColor = LocalTokens.current.onAffirm)) { Text("Add source") }
    }
}

@Composable
private fun AccountManagerDialog(
    accounts: List<AccountEntity>,
    onDismiss: () -> Unit,
    onAdd: () -> Unit,
    onEdit: (AccountEntity) -> Unit,
) {
    AwareDialog("Accounts & wallets", onDismiss) {
        Text(
            "Every money source is yours to rename, retype, rebalance, or make the default.",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        accounts.forEach { account ->
            Surface(
                modifier = Modifier.fillMaxWidth().clickable { onEdit(account) },
                shape = awareShape(14.dp),
                color = MaterialTheme.colorScheme.surface,
                border = if (account.isDefault) BorderStroke(1.5.dp, LocalTokens.current.accent) else null,
            ) {
                Row(
                    Modifier.padding(horizontal = 16.dp, vertical = 15.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        Modifier.size(42.dp).clip(awareShape(11.dp)).background(LocalTokens.current.hero.copy(.2f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(Icons.Default.AccountBalanceWallet, null, tint = readableAccent(LocalTokens.current.hero))
                    }
                    Spacer(Modifier.width(13.dp))
                    Column(Modifier.weight(1f)) {
                        Text(account.name, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                        Text(
                            account.kind.name.lowercase().replaceFirstChar(Char::uppercase) +
                                if (account.isDefault) " · Default" else "",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 12.sp,
                        )
                    }
                    Icon(Icons.Default.Edit, "Edit ${account.name}", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        Button(
            onClick = onAdd,
            modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = LocalTokens.current.affirm,
                contentColor = LocalTokens.current.onAffirm,
            ),
        ) {
            Icon(Icons.Default.Add, null)
            Spacer(Modifier.width(8.dp))
            Text("Add account or wallet", fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun AccountEditorDialog(
    account: AccountEntity?,
    onDismiss: () -> Unit,
    onSave: (Long, String, AccountKind, Long, Boolean) -> Unit,
) {
    var name by remember(account?.id) { mutableStateOf(account?.name.orEmpty()) }
    var kind by remember(account?.id) { mutableStateOf(account?.kind ?: AccountKind.BANK) }
    var balance by remember(account?.id) {
        mutableStateOf(
            account?.openingBalancePaise?.let {
                if (it % 100L == 0L) (it / 100L).toString() else String.format(Locale.US, "%.2f", it / 100.0)
            }.orEmpty(),
        )
    }
    var isDefault by remember(account?.id) { mutableStateOf(account?.isDefault ?: false) }
    val opening = parsePaise(balance) ?: 0L
    AwareDialog(if (account == null) "Add money source" else "Edit money source", onDismiss) {
        ChoiceRow(AccountKind.entries, kind) { kind = it }
        OutlinedTextField(
            name,
            { name = it },
            Modifier.fillMaxWidth(),
            label = { Text("Account or wallet name") },
            colors = cozyFieldColors(),
            singleLine = true,
        )
        MoneyField(balance, "Opening balance") { balance = it }
        Row(
            Modifier.fillMaxWidth().clickable { isDefault = true }.padding(vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Text("Default account", fontWeight = FontWeight.SemiBold)
                Text("Used for SMS captures and new entries", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
            }
            Switch(checked = isDefault, onCheckedChange = { checked -> if (checked) isDefault = true })
        }
        Button(
            onClick = { onSave(account?.id ?: 0L, name, kind, opening, isDefault) },
            enabled = name.isNotBlank(),
            modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = LocalTokens.current.affirm,
                contentColor = LocalTokens.current.onAffirm,
            ),
        ) {
            Text(if (account == null) "Add source" else "Save changes")
        }
    }
}

@Composable
private fun CategoryManagerDialog(
    isIncome: Boolean,
    categories: List<CategoryEntity>,
    onDismiss: () -> Unit,
    onAdd: () -> Unit,
) {
    val kind = if (isIncome) "income" else "expense"
    AwareDialog(if (isIncome) "Income categories" else "Expense categories", onDismiss) {
        Text(
            "View every active $kind category here. New categories become available throughout aware immediately.",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        MachineLabel("${categories.size} ${if (categories.size == 1) "category" else "categories"} // $kind")
        Button(
            onClick = onAdd,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = LocalTokens.current.accent,
                contentColor = LocalTokens.current.onAccent,
            ),
        ) {
            Icon(Icons.Default.Add, null, Modifier.size(18.dp))
            Spacer(Modifier.width(7.dp))
            Text("Add ${if (isIncome) "income" else "expense"} category", fontWeight = FontWeight.Bold)
        }
        if (categories.isEmpty()) {
            Panel(Modifier.fillMaxWidth()) {
                Text("No $kind categories yet", fontWeight = FontWeight.Bold)
                Text("Create your first one below.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            categories.forEach { category ->
                val categoryColor = Color(category.colorArgb)
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = awareShape(14.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                ) {
                    Row(
                        Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            Modifier.size(42.dp).clip(awareShape(13.dp)).background(categoryColor.copy(alpha = .25f)),
                            contentAlignment = Alignment.Center,
                        ) { Text(category.emoji, fontSize = 20.sp) }
                        Spacer(Modifier.width(13.dp))
                        Column(Modifier.weight(1f)) {
                            Text(category.name, fontWeight = FontWeight.Bold)
                            Text(
                                if (isIncome) "Available for income and refunds" else "Available for expenses",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 11.sp,
                            )
                        }
                        Box(Modifier.size(10.dp).clip(CircleShape).background(categoryColor))
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryDialog(isIncome: Boolean, onDismiss: () -> Unit, onSave: (String, String, Long) -> Unit) {
    var name by remember { mutableStateOf("") }
    var emoji by remember { mutableStateOf("") }
    val t = LocalTokens.current
    // Category swatches are a choice set, not a token showcase. Some theme
    // roles intentionally share a colour, so deriving this row from role
    // tokens produced duplicate pink and violet options in the maximal skin.
    val palette = remember(t.maximal, MaterialTheme.colorScheme.background) {
        if (t.maximal) listOf(
            NeonMagenta, NeonViolet, NeonLime, NeonOrange,
            NeonCyan, NeonGreen, NeonRed, Color(0xFFFFE45E),
        ) else listOf(
            CozyPowderBlue, CozyLavender, CozyPistachio, CozyApricot,
            CozyDustyRose, CozySage, CozyNegative, Color(0xFFE6C7A6),
        )
    }
    var selectedColor by remember(t) { mutableStateOf(palette.first()) }
    AwareDialog(if (isIncome) "New income option" else "New spending option", onDismiss) {
        Text(
            "Create a reusable category that appears immediately in every matching picker.",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 12.sp,
        )
        OutlinedTextField(
            name, { name = it }, Modifier.fillMaxWidth(),
            label = { Text(if (isIncome) "Income label" else "Category name") },
            colors = cozyFieldColors(), singleLine = true,
        )
        OutlinedTextField(
            emoji, { emoji = it.take(4) }, Modifier.fillMaxWidth(),
            label = { Text("Emoji or symbol") }, placeholder = { Text("✨") },
            colors = cozyFieldColors(), singleLine = true,
        )
        Label("Colour")
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            palette.forEach { color ->
                val active = selectedColor == color
                Box(
                    Modifier.weight(1f).height(38.dp).clip(awareShape(12.dp)).background(color)
                        .border(if (active) 3.dp else 0.dp, if (active) MaterialTheme.colorScheme.onSurface else Color.Transparent, awareShape(12.dp))
                        .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { selectedColor = color },
                )
            }
        }
        Button(
            onClick = { onSave(name.trim(), emoji.ifBlank { "✨" }, selectedColor.value.toLong()) },
            enabled = name.isNotBlank(), modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = LocalTokens.current.accent, contentColor = LocalTokens.current.onAccent),
        ) { Text("Add custom option", fontWeight = FontWeight.Bold) }
    }
}

@Composable
private fun CandidateReviewDialog(
    candidate: CaptureCandidateEntity,
    state: MainUiState,
    onDismiss: () -> Unit,
    onDiscard: () -> Unit,
    onConfirm: (Long, String, TransactionType, Long?, Long?, Long?, Boolean) -> Unit,
    groqConfigured: Boolean,
    aiSuggestion: String?,
    onSuggest: () -> Unit,
    onAddCategory: (Boolean) -> Unit,
    onAddAccount: () -> Unit,
) {
    var amount by remember(candidate.id) { mutableStateOf("%.2f".format(Locale.ENGLISH, candidate.amountPaise / 100.0)) }
    var merchant by remember(candidate.id) { mutableStateOf(candidate.merchant) }
    var type by remember(candidate.id) { mutableStateOf(candidate.type) }
    var categoryId by remember { mutableStateOf<Long?>(null) }
    var accountId by remember(state.accounts) { mutableLongStateOf(state.accounts.firstOrNull { it.isDefault }?.id ?: 0L) }
    var destinationAccountId by remember(candidate.id, state.accounts) {
        mutableStateOf(state.accounts.firstOrNull { candidate.type == TransactionType.TRANSFER && it.kind == AccountKind.CASH }?.id)
    }
    var learn by remember { mutableStateOf(true) }
    val paise = parsePaise(amount)
    val confident = candidate.confidence >= .82f
    val badge = if (confident) LocalTokens.current.positive else LocalTokens.current.accent
    val badgeInk = readableAccent(LocalTokens.current.onAccent, badge)
    val canConfirm = paise != null && paise > 0 && merchant.isNotBlank() && accountId > 0 &&
        (type != TransactionType.TRANSFER || destinationAccountId != null)
    fun changeType(next: TransactionType) {
        if (next != type) {
            type = next
            categoryId = null
            destinationAccountId = null
        }
    }
    AwareDialog("Authorize payment", onDismiss) {
        Surface(shape = awareShape(16.dp), color = badge) {
            Row(Modifier.fillMaxWidth().padding(14.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("DETECTED ${candidate.type.name}", color = badgeInk, fontWeight = FontWeight.Black)
                Text("${(candidate.confidence * 100).toInt()}% CONFIDENCE", color = badgeInk, fontWeight = FontWeight.Black)
            }
        }
        Text("Captured from ${candidate.sender}. This payment will not appear in your ledger until you approve it here.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        Label("Transaction type")
        ChoiceRow(
            listOf(TransactionType.EXPENSE, TransactionType.INCOME, TransactionType.TRANSFER, TransactionType.REFUND),
            selected = type,
            label = { it.name.lowercase().replaceFirstChar(Char::uppercase) },
            onSelected = ::changeType,
        )
        MoneyField(amount) { amount = it }
        OutlinedTextField(merchant, { merchant = it }, Modifier.fillMaxWidth(), label = { Text("Merchant") }, colors = cozyFieldColors(), singleLine = true)
        Label("Account")
        ScrollChoices(state.accounts, accountId, { it.id }, { it.name }, onAdd = onAddAccount) {
            accountId = it.id
            if (destinationAccountId == it.id) destinationAccountId = null
        }
        if (type == TransactionType.TRANSFER) {
            Label("To account / cash wallet")
            ScrollChoices(
                state.accounts.filter { it.id != accountId },
                destinationAccountId,
                { it.id },
                { it.name },
                onAdd = onAddAccount,
            ) { destinationAccountId = it.id }
        } else {
            Label("Category")
            AwareCategoryPicker(
                state.categories.filter { it.isIncome == (type == TransactionType.INCOME || type == TransactionType.REFUND) },
                categoryId,
                onAdd = { onAddCategory(type == TransactionType.INCOME || type == TransactionType.REFUND) },
            ) { categoryId = it }
            if (groqConfigured && type == TransactionType.EXPENSE) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedButton(onClick = onSuggest) { Text("ASK PRIVATE AI") }
                    aiSuggestion?.let { suggestion ->
                        Spacer(Modifier.width(8.dp))
                        AssistChip(onClick = { categoryId = state.categories.firstOrNull { it.name == suggestion }?.id }, label = { Text("Try $suggestion") })
                    }
                }
            }
            Row(Modifier.fillMaxWidth().clickable { learn = !learn }, verticalAlignment = Alignment.CenterVertically) {
                Checkbox(learn, { learn = it }); Text("Remember this merchant next time", fontWeight = FontWeight.Bold)
            }
        }
        Button(onClick = { onConfirm(paise ?: 0, merchant, type, categoryId, accountId, destinationAccountId, learn) }, enabled = canConfirm, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = LocalTokens.current.affirm, contentColor = LocalTokens.current.onAffirm)) { Text("Authorize and add") }
        TextButton(onClick = onDiscard, modifier = Modifier.fillMaxWidth()) { Text("Not a transaction — discard", color = MaterialTheme.colorScheme.error) }
    }
}

@Composable
private fun StatementReadingDialog(fileName: String, onDismiss: () -> Unit) {
    AwareDialog("Reading statement", onDismiss) {
        Row(
            Modifier.fillMaxWidth().padding(vertical = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CircularProgressIndicator(Modifier.size(26.dp), strokeWidth = 3.dp)
            Column {
                Text(fileName, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text("Finding columns and reconciling debits and credits on this device…", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
            }
        }
    }
}

@Composable
private fun StatementPasswordDialog(
    fileName: String,
    error: String?,
    onDismiss: () -> Unit,
    onUnlock: (CharArray) -> Unit,
) {
    var password by remember(fileName) { mutableStateOf("") }
    AwareDialog("Unlock statement", onDismiss) {
        Text(fileName, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Text(
            "Enter the Excel password. It is used once in memory, never saved, and never sent to Groq.",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Statement password") },
            visualTransformation = PasswordVisualTransformation(),
            colors = cozyFieldColors(),
            singleLine = true,
        )
        error?.let { Text(it, color = MaterialTheme.colorScheme.error, fontSize = 12.sp) }
        Button(
            onClick = {
                val oneUsePassword = password.toCharArray()
                password = ""
                onUnlock(oneUsePassword)
            },
            enabled = password.isNotEmpty(),
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = LocalTokens.current.affirm, contentColor = LocalTokens.current.onAffirm),
        ) { Text("Unlock locally") }
    }
}

@Composable
private fun PasswordDissolveOverlay(onFinished: () -> Unit) {
    val context = LocalContext.current
    val progress = remember { Animatable(0f) }
    val ink = MaterialTheme.colorScheme.onBackground
    val accent = LocalTokens.current.accent
    LaunchedEffect(Unit) {
        Toast.makeText(
            context,
            "Password used once and forgotten. It was never saved or sent anywhere.",
            Toast.LENGTH_LONG,
        ).show()
        progress.animateTo(1f, tween(1_450, easing = FastOutSlowInEasing))
        delay(220)
        onFinished()
    }
    Dialog(onDismissRequest = {}, properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)) {
        LightDialogSystemBars()
        Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            Column(
                Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding().padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Canvas(Modifier.fillMaxWidth().height(150.dp).semantics { contentDescription = "The one-use password dissolves after unlocking" }) {
                    val centerY = size.height / 2f
                    val spacing = 24.dp.toPx()
                    val dotRadius = 6.dp.toPx()
                    val startX = size.width / 2f - spacing * 3.5f
                    repeat(8) { dot ->
                        val delayFraction = dot * .045f
                        val local = ((progress.value - delayFraction) / (1f - delayFraction)).coerceIn(0f, 1f)
                        val baseX = startX + dot * spacing
                        drawCircle(
                            color = ink.copy(alpha = (1f - local).coerceAtLeast(0f)),
                            radius = dotRadius * (1f - local * .28f),
                            center = androidx.compose.ui.geometry.Offset(baseX + local * 34.dp.toPx(), centerY - local * (dot % 3 - 1) * 14.dp.toPx()),
                        )
                        repeat(5) { particle ->
                            val particleProgress = ((local - .08f * particle) / .92f).coerceIn(0f, 1f)
                            if (particleProgress > 0f) {
                                val angle = (dot * 1.7f + particle * 1.19f)
                                drawCircle(
                                    color = accent.copy(alpha = (1f - particleProgress) * .75f),
                                    radius = (2.4f - particle * .22f).dp.toPx(),
                                    center = androidx.compose.ui.geometry.Offset(
                                        baseX + particleProgress * (42 + particle * 7).dp.toPx(),
                                        centerY + kotlin.math.sin(angle) * particleProgress * (22 + particle * 4).dp.toPx(),
                                    ),
                                )
                            }
                        }
                    }
                }
                Text("Password forgotten", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                Text("Opening your private review…", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun StatementReviewDialog(
    review: StatementReview,
    state: MainUiState,
    groqConfigured: Boolean,
    onDismiss: () -> Unit,
    onAccount: (Long) -> Unit,
    onToggle: (Int) -> Unit,
    onSelectNew: () -> Unit,
    onType: (Int, TransactionType) -> Unit,
    onCategory: (Int, Long?) -> Unit,
    onDestination: (Int, Long?) -> Unit,
    onGroq: () -> Unit,
    onAddCategory: (Boolean) -> Unit,
    onImport: () -> Unit,
) {
    val selectedCount = review.rows.count(StatementReviewRow::selected)
    val duplicateCount = review.rows.count { it.duplicateReason != null }
    val unresolvedCount = review.rows.count { !it.source.directionVerified }
    val transferMissing = review.rows.any {
        it.selected && it.type == TransactionType.TRANSFER && (it.destinationAccountId == null || it.destinationAccountId == review.accountId)
    }
    val canImport = selectedCount > 0 && review.accountId > 0 && !transferMissing && !review.aiBusy
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false),
    ) {
        LightDialogSystemBars()
        Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding()) {
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Surface(Modifier.size(38.dp).clickable(onClick = onDismiss), CircleShape, MaterialTheme.colorScheme.surfaceVariant) {
                        Icon(Icons.Default.Close, "Close", Modifier.padding(10.dp))
                    }
                    Spacer(Modifier.width(14.dp))
                    Column(Modifier.weight(1f)) {
                        Text("Review statement", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Text(review.fileName, maxLines = 1, overflow = TextOverflow.Ellipsis, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
                    }
                    Text("$selectedCount/${review.rows.size}", fontWeight = FontWeight.Black, color = LocalTokens.current.accent)
                }
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                LazyColumn(
                    Modifier.weight(1f).fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    item {
                        Panel(Modifier.fillMaxWidth()) {
                            MachineLabel("local reconciliation // review required")
                            Spacer(Modifier.height(8.dp))
                            Text("${review.rows.size} transactions found", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black)
                            val checks = when {
                                review.balanceChecks == 0 -> "Debit/credit columns supplied direction"
                                else -> "${review.balancedMatches}/${review.balanceChecks} ambiguous rows matched the running balance"
                            }
                            Text(checks, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                            if (duplicateCount > 0 || unresolvedCount > 0 || review.skippedRows > 0) {
                                Text(
                                    listOfNotNull(
                                        duplicateCount.takeIf { it > 0 }?.let { "$it likely duplicate" },
                                        unresolvedCount.takeIf { it > 0 }?.let { "$it need direction" },
                                        review.skippedRows.takeIf { it > 0 }?.let { "$it non-transaction rows skipped" },
                                    ).joinToString(" · "),
                                    color = LocalTokens.current.warn,
                                    fontSize = 12.sp,
                                )
                            }
                        }
                    }
                    item {
                        Label("Statement account")
                        Spacer(Modifier.height(6.dp))
                        ScrollChoices(state.accounts, review.accountId, { it.id }, { it.name }) { onAccount(it.id) }
                    }
                    item {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(onClick = onSelectNew, modifier = Modifier.weight(1f)) { Text("Select safe rows") }
                            OutlinedButton(onClick = onGroq, enabled = groqConfigured && !review.aiBusy, modifier = Modifier.weight(1f)) {
                                if (review.aiBusy) CircularProgressIndicator(Modifier.size(16.dp), strokeWidth = 2.dp)
                                else Icon(Icons.Default.AutoAwesome, null, Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text(if (groqConfigured) "Suggest categories" else "Groq off", maxLines = 1)
                            }
                        }
                        Text(
                            "Only redacted merchant words and your category names leave the device when you tap Groq.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 10.sp,
                        )
                    }
                    items(review.rows, key = { it.source.rowNumber }) { row ->
                        StatementReviewRowCard(
                            row = row,
                            sourceAccountId = review.accountId,
                            accounts = state.accounts,
                            categories = state.categories,
                            onToggle = { onToggle(row.source.rowNumber) },
                            onType = { onType(row.source.rowNumber, it) },
                            onCategory = { onCategory(row.source.rowNumber, it) },
                            onDestination = { onDestination(row.source.rowNumber, it) },
                            onAddCategory = onAddCategory,
                        )
                    }
                }
                Surface(color = MaterialTheme.colorScheme.surface, tonalElevation = 4.dp) {
                    Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp)) {
                        if (transferMissing) Text("Choose a receiving account for each selected transfer.", color = MaterialTheme.colorScheme.error, fontSize = 11.sp)
                        Button(
                            onClick = onImport,
                            enabled = canImport,
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = LocalTokens.current.affirm, contentColor = LocalTokens.current.onAffirm),
                        ) { Text("Add $selectedCount reviewed ${if (selectedCount == 1) "transaction" else "transactions"}") }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatementReviewRowCard(
    row: StatementReviewRow,
    sourceAccountId: Long,
    accounts: List<AccountEntity>,
    categories: List<CategoryEntity>,
    onToggle: () -> Unit,
    onType: (TransactionType) -> Unit,
    onCategory: (Long?) -> Unit,
    onDestination: (Long?) -> Unit,
    onAddCategory: (Boolean) -> Unit,
) {
    val income = row.type == TransactionType.INCOME || row.type == TransactionType.REFUND
    val rowCategories = categories.filter { it.isIncome == income }
    Surface(
        Modifier.fillMaxWidth(),
        shape = awareShape(14.dp),
        color = if (row.selected) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .62f),
        border = BorderStroke(1.dp, if (row.duplicateReason != null) LocalTokens.current.warn.copy(alpha = .7f) else MaterialTheme.colorScheme.outlineVariant),
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(row.selected, { onToggle() })
                Column(Modifier.weight(1f)) {
                    Text(row.source.merchant, fontWeight = FontWeight.Bold, maxLines = 2, overflow = TextOverflow.Ellipsis)
                    Text(
                        IMPORT_DATE_FORMAT.format(Instant.ofEpochMilli(row.source.occurredAt).atZone(ZoneId.systemDefault())),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp,
                    )
                }
                Text(money(row.source.amountPaise), fontWeight = FontWeight.Black, color = if (income) LocalTokens.current.positive else MaterialTheme.colorScheme.onSurface)
            }
            row.duplicateReason?.let { Text(it + " · leave unchecked unless this is separate", color = LocalTokens.current.warn, fontSize = 11.sp) }
            row.source.warning?.let { Text(it, color = MaterialTheme.colorScheme.error, fontSize = 11.sp) }
            ChoiceRow(
                listOf(TransactionType.EXPENSE, TransactionType.INCOME, TransactionType.REFUND, TransactionType.TRANSFER),
                row.type,
                label = { it.name.lowercase().replaceFirstChar(Char::uppercase) },
                onSelected = onType,
            )
            if (row.type == TransactionType.TRANSFER) {
                Label("Receiving account")
                ScrollChoices(accounts.filter { it.id != sourceAccountId }, row.destinationAccountId, { it.id }, { it.name }) { onDestination(it.id) }
            } else {
                Label("Category")
                Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                    FilterPill("Uncategorised", row.categoryId == null) { onCategory(null) }
                    rowCategories.forEach { category ->
                        FilterPill("${category.emoji} ${category.name}", row.categoryId == category.id) { onCategory(category.id) }
                    }
                    FilterPill("＋ New", false) { onAddCategory(income) }
                }
            }
        }
    }
}

@Composable
private fun StorageDialog(onDismiss: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var snapshot by remember { mutableStateOf<StorageSnapshot?>(null) }
    suspend fun refresh() {
        snapshot = withContext(Dispatchers.IO) { readStorageSnapshot(context) }
    }
    LaunchedEffect(Unit) { refresh() }
    AwareDialog("Storage", onDismiss) {
        Text(
            "Transactions use a compact encrypted database. It grows with your ledger, but the original statement file and its password are never copied into aware.",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Panel(Modifier.fillMaxWidth()) {
            StorageMetric("Encrypted ledger", snapshot?.ledgerBytes)
            HorizontalDivider(Modifier.padding(vertical = 10.dp), color = MaterialTheme.colorScheme.outlineVariant)
            StorageMetric("Temporary files", snapshot?.temporaryBytes)
            HorizontalDivider(Modifier.padding(vertical = 10.dp), color = MaterialTheme.colorScheme.outlineVariant)
            StorageMetric("Total app data shown here", snapshot?.totalBytes)
        }
        Text(
            "Android may report a little more for the app binary and system-managed files. Backups and exported CSV files live wherever you chose to save them.",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 11.sp,
        )
        OutlinedButton(
            onClick = {
                scope.launch {
                    val cleared = withContext(Dispatchers.IO) { clearTemporaryStorage(context) }
                    refresh()
                    Toast.makeText(context, "Cleared ${formatStorageSize(cleared)} of temporary files", Toast.LENGTH_SHORT).show()
                }
            },
            enabled = (snapshot?.temporaryBytes ?: 0L) > 0,
            modifier = Modifier.fillMaxWidth(),
        ) { Text("Clear temporary files") }
        Button(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) { Text("Done") }
    }
}

@Composable
private fun StorageMetric(label: String, bytes: Long?) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(label, Modifier.weight(1f), fontWeight = FontWeight.SemiBold)
        Text(bytes?.let(::formatStorageSize) ?: "…", color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun GroqKeyDialog(onDismiss: () -> Unit, onSave: (String) -> Unit) {
    var key by remember { mutableStateOf("") }
    AwareDialog("Connect Groq", onDismiss) {
        Text("Your key is encrypted with Android Keystore. aware sends only redacted merchant words and category names.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        OutlinedTextField(key, { key = it.trim() }, modifier = Modifier.fillMaxWidth(), label = { Text("Groq API key") }, colors = cozyFieldColors(), singleLine = true)
        Button(onClick = { onSave(key) }, enabled = key.startsWith("gsk_") && key.length > 20, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = LocalTokens.current.affirm, contentColor = LocalTokens.current.onAffirm)) { Text("Save encrypted key") }
    }
}

@Composable
private fun PasswordDialog(title: String, message: String, onDismiss: () -> Unit, onSave: (String) -> Unit) {
    var password by remember { mutableStateOf("") }
    AwareDialog(title, onDismiss) {
        Text(message, color = MaterialTheme.colorScheme.onSurfaceVariant)
        OutlinedTextField(password, { password = it }, modifier = Modifier.fillMaxWidth(), label = { Text("Backup password") }, colors = cozyFieldColors(), singleLine = true)
        Button(onClick = { onSave(password) }, enabled = password.length >= 8, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = LocalTokens.current.affirm, contentColor = LocalTokens.current.onAffirm)) { Text("Continue") }
    }
}

@Composable
private fun AwareDialog(title: String, onDismiss: () -> Unit, content: @Composable ColumnScope.() -> Unit) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false),
    ) {
        LightDialogSystemBars()
        Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            Column(
                Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding()
                    .verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Surface(
                    modifier = Modifier.size(38.dp).clickable(onClick = onDismiss),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surfaceVariant,
                ) { Icon(Icons.Default.Close, "Close", Modifier.padding(10.dp)) }
                Spacer(Modifier.height(20.dp))
                Text(title, style = MaterialTheme.typography.headlineMedium)
                Spacer(Modifier.height(12.dp))
                content()
            }
        }
    }
}

@Composable
private fun LightDialogSystemBars() {
    val view = LocalView.current
    val background = MaterialTheme.colorScheme.background
    val light = background.luminance() > .4f
    SideEffect {
        val window = (view.parent as? androidx.compose.ui.window.DialogWindowProvider)?.window ?: return@SideEffect
        window.statusBarColor = Color.Transparent.toArgb()
        window.navigationBarColor = background.toArgb()
        androidx.core.view.WindowCompat.getInsetsController(window, view).apply {
            isAppearanceLightStatusBars = light
            isAppearanceLightNavigationBars = light
        }
    }
}

@Composable
private fun MoneyField(value: String, label: String = "Amount", onChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = { next -> if (next.matches(Regex("\\d{0,9}(?:\\.\\d{0,2})?"))) onChange(next) },
        modifier = Modifier.fillMaxWidth(),
        label = { Text(label) },
        leadingIcon = { Text("₹", fontWeight = FontWeight.Black, fontSize = 20.sp) },
        colors = cozyFieldColors(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        singleLine = true,
    )
}

@Composable
private fun Label(value: String) { Text(value, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp) }

@Composable
private fun cozyFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
    disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
    focusedBorderColor = MaterialTheme.colorScheme.primary,
    unfocusedBorderColor = Color.Transparent,
)

@Composable
private fun <T> ChoiceRow(values: List<T>, selected: T, label: (T) -> String = { it.toString().lowercase().replaceFirstChar(Char::uppercase) }, onSelected: (T) -> Unit) {
    Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
        values.forEach { value -> FilterPill(label(value), value == selected) { onSelected(value) } }
    }
}

@Composable
private fun <T, K> ScrollChoices(
    values: List<T>,
    selected: K?,
    key: (T) -> K,
    label: (T) -> String,
    onAdd: (() -> Unit)? = null,
    onSelected: (T) -> Unit,
) {
    Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
        values.forEach { item -> FilterPill(label(item), key(item) == selected) { onSelected(item) } }
        onAdd?.let { FilterPill("＋ New", false, it) }
    }
}

@Composable
private fun AnimatedMoneyAmount(
    paise: Long,
    modifier: Modifier = Modifier,
    signed: Boolean = false,
    style: TextStyle = MaterialTheme.typography.headlineLarge,
    color: Color = MaterialTheme.colorScheme.onBackground,
) {
    val progress = remember { Animatable(0f) }
    LaunchedEffect(paise) {
        progress.snapTo(0f)
        progress.animateTo(1f, animationSpec = tween(durationMillis = 310, easing = FastOutSlowInEasing))
    }
    val finalLabel = remember(paise, signed) { signedMoney(paise, signed) }
    // derivedStateOf keeps the Text out of recomposition on frames where the
    // rendered digits happen to be unchanged; the graphicsLayer block below
    // reads progress lazily, so the transform animates without recomposing.
    val visibleLabel by remember(paise, signed) {
        derivedStateOf { signedMoney((paise.toDouble() * progress.value).toLong(), signed) }
    }
    Text(
        visibleLabel,
        modifier = modifier
            .graphicsLayer {
                val value = progress.value
                alpha = .28f + (.72f * value)
                translationY = (1f - value) * 12.dp.toPx()
                scaleX = .94f + (.06f * value)
                scaleY = .94f + (.06f * value)
            }
            .clearAndSetSemantics { contentDescription = finalLabel },
        style = style,
        color = color,
        maxLines = 1,
    )
}

/**
 * Wall-clock time for a row subtitle. Uses [DateTimeFormatter], which is
 * immutable and thread-safe, in place of the shared mutable SimpleDateFormat
 * the rows used to share.
 */
private fun formatTime(epochMillis: Long): String =
    TIME_FORMAT.format(Instant.ofEpochMilli(epochMillis).atZone(ZoneId.systemDefault()))

private val TIME_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("h:mm a", Locale.ENGLISH)
private val IMPORT_DATE_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.ENGLISH)

private fun signedMoney(paise: Long, signed: Boolean): String =
    (if (signed && paise > 0) "+" else if (paise < 0) "−" else "") + money(kotlin.math.abs(paise))

private fun spentForBudget(budget: BudgetBucketEntity, transactions: List<TransactionEntity>): Long {
    val zone = ZoneId.systemDefault()
    val now = LocalDate.now()
    val startDate = when (budget.period) {
        BudgetPeriod.DAILY -> now
        BudgetPeriod.WEEKLY -> now.minusDays((now.dayOfWeek.value - 1).toLong())
        BudgetPeriod.MONTHLY -> now.withDayOfMonth(1)
        BudgetPeriod.YEARLY -> now.withDayOfYear(1)
    }
    val endDate = when (budget.period) {
        BudgetPeriod.DAILY -> startDate.plusDays(1)
        BudgetPeriod.WEEKLY -> startDate.plusWeeks(1)
        BudgetPeriod.MONTHLY -> startDate.plusMonths(1)
        BudgetPeriod.YEARLY -> startDate.plusYears(1)
    }
    val start = startDate.atStartOfDay(zone).toInstant().toEpochMilli()
    val end = endDate.atStartOfDay(zone).toInstant().toEpochMilli()
    return transactions.asSequence()
        .filter { it.occurredAt in start until end }
        .filter { it.type == TransactionType.EXPENSE }
        .filter {
            when (budget.scope) {
                BudgetScope.OVERALL -> true
                BudgetScope.CATEGORY -> budget.categoryId == null || it.categoryId == budget.categoryId
                BudgetScope.ACCOUNT -> budget.accountId == null || it.accountId == budget.accountId
                BudgetScope.PAYEE -> budget.payee.isNullOrBlank() || it.merchant.equals(budget.payee, ignoreCase = true)
            }
        }
        .sumOf { it.amountPaise }
}
private fun parsePaise(value: String): Long? = value.toBigDecimalOrNull()?.movePointRight(2)?.longValueExact()

private fun editableMoney(paise: Long): String = if (paise % 100L == 0L) {
    (paise / 100L).toString()
} else {
    "%.2f".format(Locale.ENGLISH, paise / 100.0)
}

/**
 * Formats paise as Indian-grouped rupees.
 *
 * This runs on every frame of the balance count-up animations, so it builds the
 * string directly instead of going through a shared NumberFormat behind a
 * lock - that path allocated and contended on the UI thread 60 times a second.
 */
private fun money(paise: Long): String {
    val sb = StringBuilder(20)
    if (paise < 0) sb.append('-')
    val abs = if (paise == Long.MIN_VALUE) Long.MAX_VALUE else kotlin.math.abs(paise)
    sb.append('₹')
    appendIndianGrouped(sb, abs / 100L)
    val fraction = (abs % 100L).toInt()
    if (fraction != 0) {
        sb.append('.')
        if (fraction < 10) sb.append('0')
        sb.append(fraction)
    }
    return sb.toString()
}

/** Appends [value] with Indian digit grouping: 12,34,567 rather than 1,234,567. */
private fun appendIndianGrouped(sb: StringBuilder, value: Long) {
    val digits = value.toString()
    val length = digits.length
    if (length <= 3) {
        sb.append(digits)
        return
    }
    val prefixLength = length - 3
    var index = if (prefixLength % 2 == 0) 2 else 1
    sb.append(digits, 0, index)
    while (index < prefixLength) {
        sb.append(',').append(digits, index, index + 2)
        index += 2
    }
    sb.append(',').append(digits, prefixLength, length)
}

/**
 * Top of a four-interval chart axis: rounds each quarter-step up to a round
 * number so every gridline label is clean and the peak still fills most of the
 * plot. Floors at ₹100 so an empty period does not label itself in paise.
 */
private fun niceAxisMax(peak: Long): Long {
    val step = niceStep(kotlin.math.max(1L, (peak + 3L) / 4L))
    return kotlin.math.max(step * 4L, 10_000L)
}

private fun niceStep(value: Long): Long {
    var magnitude = 1L
    while (magnitude * 10L <= value && magnitude <= Long.MAX_VALUE / 10L) magnitude *= 10L
    for (multiple in longArrayOf(1L, 2L, 3L, 4L, 5L, 6L, 8L, 10L)) {
        val candidate = magnitude * multiple
        if (candidate >= value) return candidate
    }
    return magnitude * 10L
}

private fun compactMoney(paise: Long): String = when {
    kotlin.math.abs(paise) >= 100_000_00L -> "₹%.1fL".format(Locale.ENGLISH, paise / 100_000_00.0)
    kotlin.math.abs(paise) >= 100_000L -> "₹%.1fk".format(Locale.ENGLISH, paise / 100_000.0)
    else -> money(paise)
}
