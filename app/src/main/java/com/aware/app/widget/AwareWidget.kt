package com.aware.app.widget

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionParametersOf
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.updateAll
import androidx.glance.background
import androidx.glance.color.ColorProvider
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.aware.app.AwareApplication
import com.aware.app.MainActivity
import com.aware.app.data.CaptureCandidateEntity

import com.aware.app.ui.theme.Appearance
import com.aware.app.ui.theme.CozyPalette
import com.aware.app.ui.theme.Skin
import java.text.NumberFormat
import java.util.Locale

class AwareWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = AwareWidget()
}

class AwareWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val repository = (context.applicationContext as AwareApplication).container.repository
        val candidate = repository.latestPending()
        val count = repository.pendingCount()
        val prefs = context.getSharedPreferences("appearance", Context.MODE_PRIVATE)
        val appearance = Appearance.fromKey(prefs.getString("mode_key", null) ?: prefs.getString("mode", null))
        val skin = Skin.fromKey(prefs.getString("skin_key", null) ?: prefs.getString("skin", null))
        val cozyPalette = CozyPalette.fromKey(prefs.getString("cozy_palette_key", null))
        provideContent { WidgetContent(candidate, count, appearance, skin, cozyPalette) }
    }
}

@Composable
private fun WidgetContent(candidate: CaptureCandidateEntity?, count: Int, appearance: Appearance, skin: Skin, cozyPalette: CozyPalette) {
    fun themed(light: Color, dark: Color) = when (appearance) {
        Appearance.LIGHT -> ColorProvider(light, light)
        Appearance.DARK -> ColorProvider(dark, dark)
        Appearance.SYSTEM -> ColorProvider(light, dark)
    }
    val maximal = skin == Skin.MAXIMAL
    val background = if (maximal) themed(Color(0xFFFFFFFF), Color(0xFF08080A)) else when (cozyPalette) {
        CozyPalette.OAT_GARDEN -> themed(Color(0xFFFFF9F0), Color(0xFF1B1816))
        CozyPalette.SAGE_ROSE -> themed(Color(0xFFFBF8F1), Color(0xFF141914))
        CozyPalette.PLUM_HEARTH -> themed(Color(0xFFFFF7F4), Color(0xFF130E11))
    }
    val foreground = if (maximal) themed(Color(0xFF08080A), Color(0xFFF7F7FA)) else when (cozyPalette) {
        CozyPalette.OAT_GARDEN -> themed(Color(0xFF312B27), Color(0xFFF6EEE4))
        CozyPalette.SAGE_ROSE -> themed(Color(0xFF30342E), Color(0xFFF2EFE8))
        CozyPalette.PLUM_HEARTH -> themed(Color(0xFF382A31), Color(0xFFF7ECEE))
    }
    val secondary = if (maximal) themed(Color(0xFF54545E), Color(0xFF9A9AA6)) else when (cozyPalette) {
        CozyPalette.OAT_GARDEN -> themed(Color(0xFF6E625A), Color(0xFFCABDB1))
        CozyPalette.SAGE_ROSE -> themed(Color(0xFF665F57), Color(0xFFCEC4B8))
        CozyPalette.PLUM_HEARTH -> themed(Color(0xFF6F5C63), Color(0xFFD5C0C6))
    }
    val accent = if (maximal) themed(Color(0xFF9BE000), Color(0xFFC6FF3D)) else when (cozyPalette) {
        CozyPalette.OAT_GARDEN -> themed(Color(0xFFD9E8B5), Color(0xFFC9DDAA))
        CozyPalette.SAGE_ROSE -> themed(Color(0xFFD5B2AC), Color(0xFFD4AAA5))
        CozyPalette.PLUM_HEARTH -> themed(Color(0xFFE7C48F), Color(0xFFD9B982))
    }
    val onAccent = if (maximal) themed(Color(0xFF08080A), Color(0xFF08080A)) else when (cozyPalette) {
        CozyPalette.OAT_GARDEN -> themed(Color(0xFF312B27), Color(0xFF312B27))
        CozyPalette.SAGE_ROSE -> themed(Color(0xFF332927), Color(0xFF2A201F))
        CozyPalette.PLUM_HEARTH -> themed(Color(0xFF382A23), Color(0xFF2A1D18))
    }
    Column(
        modifier = GlanceModifier.fillMaxSize().background(background).padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(modifier = GlanceModifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text("AWARE", style = TextStyle(color = secondary, fontSize = 10.sp, fontWeight = FontWeight.Bold))
            Spacer(GlanceModifier.defaultWeight())
            if (count > 0) Text("$count PENDING", style = TextStyle(color = secondary, fontSize = 10.sp, fontWeight = FontWeight.Bold))
        }
        Spacer(GlanceModifier.height(10.dp))
        if (candidate == null) {
            Text("Your next money move will land here", style = TextStyle(color = foreground, fontSize = 16.sp, fontWeight = FontWeight.Medium))
        } else {
            Row(modifier = GlanceModifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = GlanceModifier.defaultWeight()) {
                    Text(formatMoney(candidate.amountPaise), style = TextStyle(color = foreground, fontSize = 24.sp, fontWeight = FontWeight.Bold))
                    Text(candidate.merchant.take(28), style = TextStyle(color = secondary, fontSize = 13.sp))
                }
                Spacer(GlanceModifier.width(10.dp))
                val label = if (candidate.confidence >= HIGH_CONFIDENCE) "ADD  +" else "REVIEW  →"
                Text(
                    label,
                    modifier = GlanceModifier
                        .background(accent)
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                        .clickable(actionRunCallback<WidgetCandidateAction>(actionParametersOf(CandidateIdKey to candidate.id))),
                    style = TextStyle(color = onAccent, fontSize = 12.sp, fontWeight = FontWeight.Bold),
                )
            }
        }
    }
}

class WidgetCandidateAction : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        val id = parameters[CandidateIdKey] ?: return
        val repository = (context.applicationContext as AwareApplication).container.repository
        val candidate = repository.latestPending()?.takeIf { it.id == id } ?: return
        val transactionId = if (candidate.confidence >= HIGH_CONFIDENCE) repository.postCandidate(id) else -1L
        AwareWidget().updateAll(context)
        context.startActivity(Intent(context, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            putExtra(if (transactionId > 0) "transactionId" else "reviewCandidateId", if (transactionId > 0) transactionId else id)
            putExtra("showUndo", transactionId > 0)
        })
    }
}

private val CandidateIdKey = ActionParameters.Key<Long>("candidate_id")
const val HIGH_CONFIDENCE = 0.82f
private fun formatMoney(paise: Long): String = NumberFormat.getCurrencyInstance(Locale("en", "IN")).apply {
    maximumFractionDigits = if (paise % 100L == 0L) 0 else 2
}.format(paise / 100.0)
