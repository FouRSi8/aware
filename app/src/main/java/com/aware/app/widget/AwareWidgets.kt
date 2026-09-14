package com.aware.app.widget

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.LocalContext
import androidx.glance.LocalSize
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.updateAll
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxHeight
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.aware.app.AwareApplication
import com.aware.app.MainActivity
import com.aware.app.data.BudgetBucketEntity
import com.aware.app.data.BudgetScope
import com.aware.app.data.CategoryEntity
import com.aware.app.data.DashboardSummary
import com.aware.app.data.TransactionEntity
import com.aware.app.data.TransactionStatus
import com.aware.app.data.TransactionType
import com.aware.app.ui.theme.Appearance
import com.aware.app.ui.theme.CozyPalette
import com.aware.app.ui.theme.Skin
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first

const val EXTRA_WIDGET_ADD_TYPE = "aware.widget.ADD_TYPE"

private enum class WidgetShape { COMPACT, WIDE, LARGE }

private data class WidgetSnapshot(
    val summary: DashboardSummary = DashboardSummary(),
    val recent: List<TransactionEntity> = emptyList(),
    val categories: Map<Long, CategoryEntity> = emptyMap(),
    val budgetName: String? = null,
    val budgetProgress: Float = 0f,
    val todaySpentPaise: Long = 0,
    val locked: Boolean = false,
)

private data class WidgetPalette(
    val background: Color,
    val hero: Color,
    val card: Color,
    val primary: Color,
    val onBackground: Color,
    val onHero: Color,
    val muted: Color,
    val positive: Color,
    val negative: Color,
    val isMaximal: Boolean,
)

private abstract class AwareWidget(private val shape: WidgetShape) : GlanceAppWidget() {
    // Launcher cell sizes vary substantially by grid and device. Exact mode
    // composes against the real host bounds instead of selecting a small
    // responsive bucket that some launchers then stretch, leaving dead space.
    override val sizeMode: SizeMode = SizeMode.Exact

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val palette = widgetPalette(context)
        val snapshot = loadSnapshot(context)
        provideContent { WidgetSurface(shape, snapshot, palette) }
    }
}

private class CompactAwareWidget : AwareWidget(WidgetShape.COMPACT)
private class WideAwareWidget : AwareWidget(WidgetShape.WIDE)
private class LargeAwareWidget : AwareWidget(WidgetShape.LARGE)

class CompactAwareWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = CompactAwareWidget()
}

class WideAwareWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = WideAwareWidget()
}

class LargeAwareWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = LargeAwareWidget()
}

object AwareWidgets {
    suspend fun refresh(context: Context) {
        CompactAwareWidget().updateAll(context)
        WideAwareWidget().updateAll(context)
        LargeAwareWidget().updateAll(context)
    }
}

private suspend fun loadSnapshot(context: Context): WidgetSnapshot {
    val app = context.applicationContext as? AwareApplication ?: return WidgetSnapshot()
    if (app.container.secureStore.getBoolean("app_lock")) return WidgetSnapshot(locked = true)
    val repository = app.container.repository
    return runCatching {
        combine(repository.dashboard(), repository.transactions, repository.categories, repository.budgets()) { summary, transactions, categories, budgets ->
            val posted = transactions.filter { it.status == TransactionStatus.POSTED }
            val budget = mostRelevantBudget(budgets, posted)
            WidgetSnapshot(
                summary = summary,
                recent = posted.filter { it.type != TransactionType.TRANSFER }.take(3),
                categories = categories.associateBy { it.id },
                budgetName = budget?.first,
                budgetProgress = budget?.second ?: 0f,
                todaySpentPaise = posted.filter { item ->
                    item.type == TransactionType.EXPENSE &&
                        Instant.ofEpochMilli(item.occurredAt).atZone(ZoneId.systemDefault()).toLocalDate() == LocalDate.now()
                }.sumOf { it.amountPaise },
            )
        }.first()
    }.getOrDefault(WidgetSnapshot())
}

private fun mostRelevantBudget(
    budgets: List<BudgetBucketEntity>,
    transactions: List<TransactionEntity>,
): Pair<String, Float>? {
    if (budgets.isEmpty()) return null
    val zone = ZoneId.systemDefault()
    val month = YearMonth.now()
    val start = month.atDay(1).atStartOfDay(zone).toInstant().toEpochMilli()
    val end = month.plusMonths(1).atDay(1).atStartOfDay(zone).toInstant().toEpochMilli()
    val monthItems = transactions.filter { it.occurredAt in start until end }
    return budgets.mapNotNull { budget ->
        if (budget.capPaise <= 0L) return@mapNotNull null
        val expenseIds = monthItems.filter { item ->
            item.type == TransactionType.EXPENSE && when (budget.scope) {
                BudgetScope.OVERALL -> true
                BudgetScope.CATEGORY -> item.categoryId == budget.categoryId
                BudgetScope.ACCOUNT -> item.accountId == budget.accountId
                BudgetScope.PAYEE -> item.merchant.equals(budget.payee, ignoreCase = true)
            }
        }.mapTo(mutableSetOf()) { it.id }
        val spent = monthItems.filter { it.id in expenseIds }.sumOf { it.amountPaise } -
            monthItems.filter { it.type == TransactionType.REFUND && it.linkedTransactionId in expenseIds }.sumOf { it.amountPaise }
        budget.name to (spent.coerceAtLeast(0L).toFloat() / budget.capPaise).coerceIn(0f, 1.5f)
    }.maxByOrNull { it.second }
}

@Composable
private fun WidgetSurface(shape: WidgetShape, snapshot: WidgetSnapshot, palette: WidgetPalette) {
    val context = LocalContext.current
    val radius = if (palette.isMaximal) 8.dp else 26.dp
    val openApp = actionStartActivity(Intent(context, MainActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
    })
    Box(
        modifier = GlanceModifier.fillMaxSize().cornerRadius(radius).background(palette.background).clickable(openApp),
    ) {
        if (snapshot.locked) LockedWidget(palette)
        else when (shape) {
            WidgetShape.COMPACT -> CompactWidget(snapshot, palette)
            WidgetShape.WIDE -> WideWidget(snapshot, palette)
            WidgetShape.LARGE -> LargeWidget(snapshot, palette)
        }
    }
}

@Composable
private fun LockedWidget(palette: WidgetPalette) {
    Column(
        GlanceModifier.fillMaxSize().padding(18.dp),
        verticalAlignment = Alignment.Vertical.CenterVertically,
        horizontalAlignment = Alignment.Horizontal.CenterHorizontally,
    ) {
        Text("◉", style = widgetText(palette.primary, 24, FontWeight.Bold))
        Spacer(GlanceModifier.height(8.dp))
        Text("aware is locked", style = widgetText(palette.onBackground, 16, FontWeight.Bold))
        Text("Tap to unlock", style = widgetText(palette.muted, 11))
    }
}

@Composable
private fun CompactWidget(snapshot: WidgetSnapshot, palette: WidgetPalette) {
    val contentWidth = (LocalSize.current.width.value - 26f).coerceAtLeast(96f)
    val miniWidth = ((contentWidth - 6f) / 2f).coerceAtLeast(42f)
    Column(GlanceModifier.fillMaxSize().padding(13.dp)) {
        BrandRow(palette, compact = true)
        Spacer(GlanceModifier.height(10.dp))
        Text("SAFE TO SPEND", style = widgetText(palette.muted, 9, FontWeight.Bold))
        AutoAmountText(compactMoney(snapshot.summary.safeToSpendPaise), palette.onBackground, contentWidth, maxSp = 32, minSp = 18)
        Spacer(GlanceModifier.height(3.dp))
        Row(GlanceModifier.fillMaxWidth()) {
            Text("Today ${compactMoney(snapshot.todaySpentPaise)}", style = widgetText(palette.negative, 9, FontWeight.Bold))
            Spacer(GlanceModifier.defaultWeight())
            Text("${snapshot.summary.daysRemaining}d left", style = widgetText(palette.muted, 9))
        }
        Spacer(GlanceModifier.height(7.dp))
        Row(GlanceModifier.fillMaxWidth()) {
            MiniMetric("BAL", snapshot.summary.actualBalancePaise, palette.positive, palette, miniWidth, GlanceModifier.defaultWeight())
            Spacer(GlanceModifier.width(6.dp))
            MiniMetric("/ DAY", if (snapshot.summary.daysRemaining > 0) snapshot.summary.safeToSpendPaise / snapshot.summary.daysRemaining else snapshot.summary.safeToSpendPaise, palette.primary, palette, miniWidth, GlanceModifier.defaultWeight())
        }
        Spacer(GlanceModifier.height(7.dp))
        QuickAction("＋  add expense", TransactionType.EXPENSE, palette, fill = true, modifier = GlanceModifier.fillMaxWidth())
    }
}

@Composable
private fun WideWidget(snapshot: WidgetSnapshot, palette: WidgetPalette) {
    val contentWidth = (LocalSize.current.width.value - 26f).coerceAtLeast(160f)
    val mainAmountWidth = (contentWidth - 158f).coerceAtLeast(64f)
    Column(GlanceModifier.fillMaxSize().padding(13.dp)) {
        BrandRow(palette, compact = true)
        Spacer(GlanceModifier.height(7.dp))
        Row(GlanceModifier.fillMaxWidth(), verticalAlignment = Alignment.Vertical.CenterVertically) {
            Column(GlanceModifier.defaultWeight()) {
                Text("SAFE THIS MONTH", style = widgetText(palette.muted, 8, FontWeight.Bold))
                AutoAmountText(compactMoney(snapshot.summary.safeToSpendPaise), palette.onBackground, mainAmountWidth, maxSp = 30, minSp = 15)
                Text("${dailyAllowance(snapshot.summary)} / day", style = widgetText(palette.muted, 9))
            }
            Spacer(GlanceModifier.width(8.dp))
            Row(
                GlanceModifier.width(150.dp).cornerRadius(if (palette.isMaximal) 5.dp else 15.dp).background(palette.card).padding(9.dp),
            ) {
                Column(GlanceModifier.defaultWeight()) {
                    Metric("IN", snapshot.summary.incomePaise + snapshot.summary.otherIncomePaise, palette.positive, palette, 58f)
                    Spacer(GlanceModifier.height(5.dp))
                    Metric("TODAY", snapshot.todaySpentPaise, palette.negative, palette, 58f)
                }
                Spacer(GlanceModifier.width(8.dp))
                Column(GlanceModifier.defaultWeight()) {
                    Metric("OUT", snapshot.summary.spendingPaise, palette.negative, palette, 58f)
                    Spacer(GlanceModifier.height(5.dp))
                    Text("${snapshot.summary.daysRemaining} DAYS", style = widgetText(palette.primary, 8, FontWeight.Bold))
                    Text("remaining", style = widgetText(palette.muted, 9))
                }
            }
        }
        Spacer(GlanceModifier.height(8.dp))
        Row(GlanceModifier.fillMaxWidth()) {
            QuickAction("＋ expense", TransactionType.EXPENSE, palette, fill = true, modifier = GlanceModifier.defaultWeight())
            Spacer(GlanceModifier.width(7.dp))
            QuickAction("↗ income", TransactionType.INCOME, palette, fill = false, modifier = GlanceModifier.defaultWeight())
        }
    }
}

@Composable
private fun LargeWidget(snapshot: WidgetSnapshot, palette: WidgetPalette) {
    val contentWidth = (LocalSize.current.width.value - 60f).coerceAtLeast(120f)
    Column(GlanceModifier.fillMaxSize().padding(15.dp)) {
        BrandRow(palette, compact = false)
        Spacer(GlanceModifier.height(9.dp))
        Column(
            GlanceModifier.fillMaxWidth().cornerRadius(if (palette.isMaximal) 6.dp else 22.dp).background(palette.hero).padding(15.dp),
        ) {
            Text("SAFE TO SPEND · ${YearMonth.now().month.name.take(3)}", style = widgetText(palette.onHero, 9, FontWeight.Bold))
            AutoAmountText(money(snapshot.summary.safeToSpendPaise), palette.onHero, contentWidth, maxSp = 38, minSp = 19)
            Row(GlanceModifier.fillMaxWidth()) {
                Text("Balance ${compactMoney(snapshot.summary.actualBalancePaise)}", style = widgetText(palette.onHero, 10))
                Spacer(GlanceModifier.defaultWeight())
                Text("${dailyAllowance(snapshot.summary)} / day", style = widgetText(palette.onHero, 10, FontWeight.Bold))
            }
        }
        Spacer(GlanceModifier.height(8.dp))
        Row(GlanceModifier.fillMaxWidth()) {
            MetricCard("MONEY IN", snapshot.summary.incomePaise + snapshot.summary.otherIncomePaise, palette.positive, palette, GlanceModifier.defaultWeight())
            Spacer(GlanceModifier.width(8.dp))
            MetricCard("MONEY OUT", snapshot.summary.spendingPaise, palette.negative, palette, GlanceModifier.defaultWeight())
        }
        if (snapshot.budgetName != null) {
            Spacer(GlanceModifier.height(7.dp))
            BudgetStrip(snapshot.budgetName, snapshot.budgetProgress, palette)
        }
        Spacer(GlanceModifier.height(7.dp))
        MoneyMap(snapshot, palette)
        Spacer(GlanceModifier.height(8.dp))
        Text("RECENT MOVES", style = widgetText(palette.muted, 9, FontWeight.Bold))
        Spacer(GlanceModifier.height(3.dp))
        if (snapshot.recent.isEmpty()) {
            Row(
                GlanceModifier.fillMaxWidth().cornerRadius(if (palette.isMaximal) 4.dp else 13.dp).background(palette.card).padding(9.dp),
                verticalAlignment = Alignment.Vertical.CenterVertically,
            ) {
                Text("✦", style = widgetText(palette.primary, 15, FontWeight.Bold))
                Spacer(GlanceModifier.width(7.dp))
                Column {
                    Text("A clean slate", style = widgetText(palette.onBackground, 10, FontWeight.Bold))
                    Text("Log your first move below", style = widgetText(palette.muted, 9))
                }
            }
        } else {
            snapshot.recent.take(if (snapshot.budgetName == null) 3 else 2).forEach { RecentRow(it, snapshot.categories[it.categoryId], palette) }
        }
        Spacer(GlanceModifier.height(8.dp))
        Row(GlanceModifier.fillMaxWidth()) {
            QuickAction("＋ expense", TransactionType.EXPENSE, palette, fill = true, modifier = GlanceModifier.defaultWeight())
            Spacer(GlanceModifier.width(8.dp))
            QuickAction("↗ income", TransactionType.INCOME, palette, fill = false, modifier = GlanceModifier.defaultWeight())
        }
    }
}

@Composable
private fun MoneyMap(snapshot: WidgetSnapshot, palette: WidgetPalette) {
    val itemWidth = ((LocalSize.current.width.value - 42f) / 3f).coerceAtLeast(45f)
    Row(GlanceModifier.fillMaxWidth()) {
        MiniMetric("SAVE", snapshot.summary.plannedSavingsPaise, palette.primary, palette, itemWidth, GlanceModifier.defaultWeight())
        Spacer(GlanceModifier.width(6.dp))
        MiniMetric("COMMIT", snapshot.summary.commitmentSpendingPaise, palette.negative, palette, itemWidth, GlanceModifier.defaultWeight())
        Spacer(GlanceModifier.width(6.dp))
        MiniMetric("CASH", snapshot.summary.unresolvedCashPaise, palette.positive, palette, itemWidth, GlanceModifier.defaultWeight())
    }
}

@Composable
private fun MiniMetric(label: String, amount: Long, accent: Color, palette: WidgetPalette, availableWidthDp: Float, modifier: GlanceModifier) {
    Column(modifier.cornerRadius(if (palette.isMaximal) 3.dp else 11.dp).background(palette.card).padding(horizontal = 8.dp, vertical = 6.dp)) {
        Text(label, style = widgetText(accent, 7, FontWeight.Bold))
        AutoAmountText(compactMoney(amount), palette.onBackground, availableWidthDp - 16f, maxSp = 13, minSp = 8)
    }
}

@Composable
private fun BrandRow(palette: WidgetPalette, compact: Boolean) {
    Row(GlanceModifier.fillMaxWidth(), verticalAlignment = Alignment.Vertical.CenterVertically) {
        Text(if (palette.isMaximal) "AWARE//" else "aware", style = widgetText(palette.onBackground, if (compact) 15 else 17, FontWeight.Bold))
        Spacer(GlanceModifier.defaultWeight())
        Text(if (palette.isMaximal) "● LIVE" else LocalDate.now().format(DateTimeFormatter.ofPattern("d MMM")), style = widgetText(palette.primary, 9, FontWeight.Bold))
    }
}

@Composable
private fun Metric(label: String, amount: Long, accent: Color, palette: WidgetPalette, availableWidthDp: Float) {
    Text(label, style = widgetText(accent, 8, FontWeight.Bold))
    AutoAmountText(compactMoney(amount), palette.onBackground, availableWidthDp, maxSp = 17, minSp = 9)
}

@Composable
private fun MetricCard(label: String, amount: Long, accent: Color, palette: WidgetPalette, modifier: GlanceModifier) {
    val availableWidth = ((LocalSize.current.width.value - 48f) / 2f).coerceAtLeast(58f)
    Column(modifier.cornerRadius(if (palette.isMaximal) 5.dp else 15.dp).background(palette.card).padding(10.dp)) {
        Text(label, style = widgetText(accent, 8, FontWeight.Bold))
        AutoAmountText(compactMoney(amount), palette.onBackground, availableWidth, maxSp = 19, minSp = 10)
    }
}

@Composable
private fun BudgetStrip(name: String, progress: Float, palette: WidgetPalette) {
    val label = when {
        progress >= 1f -> "cap reached"
        progress >= .9f -> "nearly full"
        else -> "${(progress * 100).toInt()}% used"
    }
    Row(
        GlanceModifier.fillMaxWidth().cornerRadius(if (palette.isMaximal) 4.dp else 13.dp).background(palette.card).padding(horizontal = 10.dp, vertical = 7.dp),
        verticalAlignment = Alignment.Vertical.CenterVertically,
    ) {
        Text(if (progress >= .9f) "●" else "◆", style = widgetText(if (progress >= .9f) palette.negative else palette.primary, 10, FontWeight.Bold))
        Spacer(GlanceModifier.width(7.dp))
        Text(name, style = widgetText(palette.onBackground, 10, FontWeight.Bold), maxLines = 1, modifier = GlanceModifier.defaultWeight())
        Text(label, style = widgetText(palette.muted, 9))
    }
}

@Composable
private fun RecentRow(item: TransactionEntity, category: CategoryEntity?, palette: WidgetPalette) {
    val incoming = item.type == TransactionType.INCOME || item.type == TransactionType.REFUND
    Row(GlanceModifier.fillMaxWidth().padding(vertical = 3.dp), verticalAlignment = Alignment.Vertical.CenterVertically) {
        Text(category?.emoji ?: if (incoming) "↗" else "↙", style = widgetText(palette.onBackground, 14))
        Spacer(GlanceModifier.width(7.dp))
        Text(item.merchant.ifBlank { item.type.name.lowercase().replaceFirstChar(Char::uppercase) }, style = widgetText(palette.onBackground, 11, FontWeight.Bold), maxLines = 1, modifier = GlanceModifier.defaultWeight())
        Text((if (incoming) "+" else "−") + compactMoney(item.amountPaise), style = widgetText(if (incoming) palette.positive else palette.negative, 11, FontWeight.Bold), maxLines = 1)
    }
}

@Composable
private fun QuickAction(
    label: String,
    type: TransactionType,
    palette: WidgetPalette,
    fill: Boolean,
    modifier: GlanceModifier = GlanceModifier,
) {
    val context = LocalContext.current
    val action = actionStartActivity(Intent(context, MainActivity::class.java).apply {
        this.action = "com.aware.app.widget.ADD_${type.name}"
        data = Uri.parse("aware://quick-add/${type.name.lowercase()}")
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        putExtra(EXTRA_WIDGET_ADD_TYPE, type.name)
    })
    Box(
        modifier = modifier.cornerRadius(if (palette.isMaximal) 3.dp else 14.dp)
            .background(if (fill) palette.primary else palette.card)
            .clickable(action)
            .padding(horizontal = 10.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(label, style = widgetText(if (fill) palette.onHero else palette.onBackground, 11, FontWeight.Bold), maxLines = 1)
    }
}

private fun widgetText(color: Color, size: Int, weight: FontWeight = FontWeight.Normal): TextStyle = TextStyle(
    color = ColorProvider(color),
    fontSize = size.sp,
    fontWeight = weight,
    textAlign = TextAlign.Start,
)

@Composable
private fun AutoAmountText(
    value: String,
    color: Color,
    availableWidthDp: Float,
    maxSp: Int,
    minSp: Int,
) {
    // Glance has no auto-size text primitive. Estimate the rendered width using
    // character-specific weights, then choose the largest size that fits the
    // actual launcher bounds. Short values grow; long Indian-formatted values
    // shrink without clipping.
    val glyphUnits = value.sumOf { character ->
        when (character) {
            ',', '.', ' ' -> 0.30
            '₹', '+', '-', '−' -> 0.64
            'k', 'K', 'L', 'C', 'r' -> 0.52
            '1' -> 0.43
            else -> 0.59
        }
    }.coerceAtLeast(1.0)
    val fittedSize = (availableWidthDp / glyphUnits)
        .toInt()
        .coerceIn(minSp, maxSp)
    Text(value, style = widgetText(color, fittedSize, FontWeight.Bold), maxLines = 1)
}

private fun dailyAllowance(summary: DashboardSummary): String = compactMoney(
    if (summary.daysRemaining > 0) summary.safeToSpendPaise / summary.daysRemaining else summary.safeToSpendPaise,
)

private fun money(paise: Long): String {
    val whole = paise / 100L
    val fraction = kotlin.math.abs(paise % 100L)
    val grouped = String.format(Locale.ENGLISH, "%,d", whole)
    return if (fraction == 0L) "₹$grouped" else "₹$grouped.${fraction.toString().padStart(2, '0')}"
}

private fun compactMoney(paise: Long): String {
    val rupees = paise / 100.0
    val abs = kotlin.math.abs(rupees)
    return when {
        abs >= 10_000_000 -> String.format(Locale.ENGLISH, "₹%.1fCr", rupees / 10_000_000)
        abs >= 100_000 -> String.format(Locale.ENGLISH, "₹%.1fL", rupees / 100_000)
        abs >= 1_000 -> String.format(Locale.ENGLISH, "₹%.1fk", rupees / 1_000)
        else -> String.format(Locale.ENGLISH, "₹%.0f", rupees)
    }
}

private fun widgetPalette(context: Context): WidgetPalette {
    val prefs = context.getSharedPreferences("appearance", Context.MODE_PRIVATE)
    val skin = Skin.fromKey(prefs.getString("skin_key", null) ?: prefs.getString("skin", null))
    val appearance = Appearance.fromKey(prefs.getString("mode_key", null) ?: prefs.getString("mode", null))
    val systemDark = context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES
    val dark = appearance == Appearance.DARK || appearance == Appearance.OLED || appearance == Appearance.SYSTEM && systemDark
    if (skin == Skin.MAXIMAL) return if (dark) {
        WidgetPalette(Color(0xFF070807), Color(0xFFBFFF29), Color(0xFF171917), Color(0xFFBFFF29), Color(0xFFF6F7F2), Color(0xFF090A08), Color(0xFF9AA096), Color(0xFFBFFF29), Color(0xFFFF4F8B), true)
    } else {
        WidgetPalette(Color(0xFFF5F6F0), Color(0xFF171917), Color(0xFFE3E6DC), Color(0xFF171917), Color(0xFF10120F), Color(0xFFBFFF29), Color(0xFF60665D), Color(0xFF2A7D52), Color(0xFFC72E5D), true)
    }
    val palette = CozyPalette.fromKey(prefs.getString("cozy_palette_key", null))
    return cozyWidgetPalette(palette, dark, appearance == Appearance.OLED)
}

private fun cozyWidgetPalette(palette: CozyPalette, dark: Boolean, oled: Boolean): WidgetPalette {
    if (dark) {
        val base = if (oled) Color.Black else when (palette) {
            CozyPalette.OAT_GARDEN -> Color(0xFF20231F)
            CozyPalette.SAGE_ROSE -> Color(0xFF252320)
            CozyPalette.PLUM_HEARTH -> Color(0xFF261F25)
            CozyPalette.LINEN_CAFE -> Color(0xFF24201D)
            CozyPalette.NAVY_TIDE -> Color(0xFF111E28)
            CozyPalette.CHARCOAL_LEATHER -> Color(0xFF202020)
        }
        val accent = when (palette) {
            CozyPalette.OAT_GARDEN -> Color(0xFFD5E8A8)
            CozyPalette.SAGE_ROSE -> Color(0xFFD9A6AC)
            CozyPalette.PLUM_HEARTH -> Color(0xFFE0A8C8)
            CozyPalette.LINEN_CAFE -> Color(0xFFD1A77F)
            CozyPalette.NAVY_TIDE -> Color(0xFF8FC6D1)
            CozyPalette.CHARCOAL_LEATHER -> Color(0xFFC78E64)
        }
        return WidgetPalette(base, accent, Color(0xFF30302E), accent, Color(0xFFF7F2E9), Color(0xFF20211E), Color(0xFFB8B0A6), Color(0xFFA7D7B8), Color(0xFFF0A39B), false)
    }
    val colors = when (palette) {
        CozyPalette.OAT_GARDEN -> Triple(Color(0xFFF9F4EA), Color(0xFFBFDCEB), Color(0xFFE5EBD0))
        CozyPalette.SAGE_ROSE -> Triple(Color(0xFFF6F0EA), Color(0xFFD8A4AA), Color(0xFFDDE5D6))
        CozyPalette.PLUM_HEARTH -> Triple(Color(0xFFFFF3E4), Color(0xFFB97A9D), Color(0xFFF0C38E))
        CozyPalette.LINEN_CAFE -> Triple(Color(0xFFF4EDE3), Color(0xFFB78866), Color(0xFFE4D2BE))
        CozyPalette.NAVY_TIDE -> Triple(Color(0xFFF2F1EA), Color(0xFF8EC5D6), Color(0xFFC8D9E6))
        CozyPalette.CHARCOAL_LEATHER -> Triple(Color(0xFFF1EBE3), Color(0xFFA96F49), Color(0xFFD8C7B6))
    }
    return WidgetPalette(colors.first, colors.second, colors.third, colors.second, Color(0xFF2D302B), Color(0xFF202B28), Color(0xFF77756E), Color(0xFF438266), Color(0xFFC35F5A), false)
}
