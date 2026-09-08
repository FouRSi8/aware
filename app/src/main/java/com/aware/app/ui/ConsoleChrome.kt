package com.aware.app.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.layout
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aware.app.ui.theme.LocalTokens

/**
 * Chrome shared by both skins.
 *
 * Cozy renders these as quiet, rounded, mostly borderless surfaces. The maximal
 * skin renders the same call sites as a hard-framed instrument panel: hairline
 * rules, machine-style labels and stencilled headings. Screens call one set of
 * components and each skin decides how loud to be.
 */

/**
 * Corner radius for a surface: [cosy] under the Cozy skin, square under the
 * maximal skin, whose whole grammar is hard-edged frames.
 */
@Composable
fun awareShape(cosy: Dp): RoundedCornerShape =
    RoundedCornerShape(if (LocalTokens.current.maximal) 0.dp else cosy)

/**
 * Heavy uppercase heading with a hard offset shadow - the signature treatment
 * of the maximal skin. Under Cozy it is an ordinary heading.
 */
@Composable
fun StencilText(
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.headlineMedium,
    color: Color = MaterialTheme.colorScheme.onBackground,
    shadowOffset: Dp = 3.dp,
    maxLines: Int = 2,
) {
    val t = LocalTokens.current
    if (!t.maximal) {
        Text(text, modifier, color = color, style = style, maxLines = maxLines, overflow = TextOverflow.Ellipsis)
        return
    }
    // Derived from the text colour so the offset stays legible on whatever
    // surface the heading lands on.
    val shadow = color.copy(alpha = .32f)
    Box(modifier) {
        Text(
            text.uppercase(),
            Modifier.offset(shadowOffset, shadowOffset),
            color = shadow,
            style = style,
            maxLines = maxLines,
            overflow = TextOverflow.Ellipsis,
        )
        Text(text.uppercase(), color = color, style = style, maxLines = maxLines, overflow = TextOverflow.Ellipsis)
    }
}

/** Offset that does not reserve extra space, so the shadow overlaps freely. */
private fun Modifier.offset(x: Dp, y: Dp) = layout { measurable, constraints ->
    val placeable = measurable.measure(constraints)
    layout(placeable.width, placeable.height) {
        placeable.place(x.roundToPx(), y.roundToPx())
    }
}

/**
 * A content container. Cozy gets a soft filled card; maximal gets a hairline
 * frame over a near-black plate.
 */
@Composable
fun Panel(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.surface,
    emphasis: Boolean = false,
    contentPadding: Dp = 16.dp,
    content: @Composable ColumnScope.() -> Unit,
) {
    val t = LocalTokens.current
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(t.cardRadius),
        color = if (t.maximal) t.panel else color,
        border = when {
            t.maximal && emphasis -> BorderStroke(2.dp, MaterialTheme.colorScheme.onBackground)
            t.maximal -> BorderStroke(t.outlineWidth, t.frame)
            else -> null
        },
    ) {
        Column(Modifier.padding(contentPadding), content = content)
    }
}

/**
 * Dashed frame used for empty and drop-target states, straight from the
 * reference's input-target panel.
 */
@Composable
fun DashedPanel(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    val t = LocalTokens.current
    if (!t.maximal) {
        Column(modifier, content = content)
        return
    }
    val stroke = t.frame
    Column(
        modifier.drawBehind {
            drawRect(
                color = stroke,
                style = Stroke(
                    width = 1.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(8.dp.toPx(), 6.dp.toPx())),
                ),
            )
        },
        content = content,
    )
}

/** `AWARE_OS / LEDGER` path line. Renders nothing under Cozy. */
@Composable
fun Breadcrumb(segment: String, modifier: Modifier = Modifier) {
    val t = LocalTokens.current
    if (!t.maximal) return
    Text(
        "AWARE_OS / ${segment.uppercase().replace(' ', '_')}",
        modifier.padding(bottom = 10.dp),
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        style = MaterialTheme.typography.labelSmall,
    )
}

/**
 * Screen title. Maximal prefixes `//` in the accent colour and stencils the
 * label; Cozy keeps its plain headline.
 */
@Composable
fun ScreenTitle(
    title: String,
    modifier: Modifier = Modifier,
    trailing: (@Composable () -> Unit)? = null,
) {
    val t = LocalTokens.current
    Row(modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        if (t.maximal) {
            Text(
                "//",
                color = t.accent,
                style = MaterialTheme.typography.headlineMedium,
            )
            Spacer(Modifier.width(8.dp))
        }
        StencilText(
            title,
            Modifier.weight(1f),
            style = MaterialTheme.typography.headlineMedium,
            maxLines = 1,
        )
        trailing?.invoke()
    }
    if (t.maximal) {
        Spacer(Modifier.height(10.dp))
        HorizontalDivider(color = t.accent, thickness = 2.dp)
    }
}

/**
 * [ScreenTitle] without the underline, for placing inside a row that already
 * has its own trailing controls and rule.
 */
@Composable
fun ScreenTitleInline(title: String, modifier: Modifier = Modifier) {
    val t = LocalTokens.current
    Row(modifier, verticalAlignment = Alignment.CenterVertically) {
        if (t.maximal) {
            Text("//", color = t.accent, style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.width(8.dp))
        }
        StencilText(
            title,
            style = MaterialTheme.typography.headlineMedium,
            maxLines = 1,
        )
    }
}

/**
 * `▪ SYSTEM // NEURAL RENDERING CONTROL` - a small square marker plus a
 * letterspaced machine label. Cozy renders it as a plain caption.
 */
@Composable
fun MachineLabel(
    text: String,
    modifier: Modifier = Modifier,
    markerColor: Color = LocalTokens.current.accent,
) {
    val t = LocalTokens.current
    Row(modifier, verticalAlignment = Alignment.CenterVertically) {
        if (t.maximal) {
            Box(Modifier.size(8.dp).background(markerColor))
            Spacer(Modifier.width(8.dp))
        }
        Text(
            if (t.maximal) text.uppercase() else text,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.labelSmall,
        )
    }
}

/** One `KEY_NAME` / `VALUE` cell inside a [MetaStrip]. */
data class MetaItem(val key: String, val value: String)

/**
 * The bordered three-up readout under the reference's hero panel. Maximal only:
 * Cozy has no equivalent and renders nothing.
 */
@Composable
fun MetaStrip(items: List<MetaItem>, modifier: Modifier = Modifier) {
    val t = LocalTokens.current
    if (!t.maximal || items.isEmpty()) return
    Surface(
        modifier.fillMaxWidth(),
        shape = RoundedCornerShape(0.dp),
        color = t.panel,
        border = BorderStroke(t.outlineWidth, t.frame),
    ) {
        Row(Modifier.fillMaxWidth()) {
            items.forEachIndexed { index, item ->
                if (index > 0) {
                    Box(Modifier.width(1.dp).height(56.dp).background(t.frame))
                }
                Column(Modifier.weight(1f).padding(horizontal = 14.dp, vertical = 12.dp)) {
                    Text(
                        item.key.uppercase().replace(' ', '_'),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.labelSmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Spacer(Modifier.height(5.dp))
                    Text(
                        item.value.uppercase(),
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.labelMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

/** `> DO A THING` bullet, as used in the reference hero. Maximal only. */
@Composable
fun ConsoleBullet(text: String, modifier: Modifier = Modifier, color: Color) {
    Row(modifier, verticalAlignment = Alignment.CenterVertically) {
        Text(
            ">",
            color = color.copy(alpha = .55f),
            style = MaterialTheme.typography.labelMedium,
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text.uppercase(),
            color = color.copy(alpha = .82f),
            style = MaterialTheme.typography.labelMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

/**
 * Small hard-edged chip. Selected state fills with the accent; unselected keeps
 * a hairline frame under maximal and a soft fill under Cozy.
 */
@Composable
fun ConsoleChip(
    label: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
) {
    val t = LocalTokens.current
    Surface(
        modifier = if (onClick == null) modifier else modifier.clip(RoundedCornerShape(t.chipRadius)).then(Modifier),
        shape = RoundedCornerShape(t.chipRadius),
        color = when {
            selected -> if (t.maximal) t.accent else MaterialTheme.colorScheme.primary
            t.maximal -> Color.Transparent
            else -> MaterialTheme.colorScheme.surface
        },
        contentColor = when {
            selected -> if (t.maximal) t.onAccent else MaterialTheme.colorScheme.onPrimary
            else -> MaterialTheme.colorScheme.onSurfaceVariant
        },
        border = if (t.maximal && !selected) BorderStroke(t.outlineWidth, t.frame) else null,
    ) {
        Text(
            if (t.maximal) label.uppercase() else label,
            Modifier.padding(horizontal = 13.dp, vertical = 8.dp),
            style = if (t.maximal) MaterialTheme.typography.labelSmall else MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
        )
    }
}
/** Hairline rule that only appears under the maximal skin. */
@Composable
fun ConsoleRule(modifier: Modifier = Modifier) {
    val t = LocalTokens.current
    if (!t.maximal) return
    HorizontalDivider(modifier, color = t.frame)
}

/** Right-aligned stack of `KEY_VALUE` corner annotations from the hero panel. */
@Composable
fun CornerMeta(lines: List<String>, modifier: Modifier = Modifier) {
    val t = LocalTokens.current
    if (!t.maximal) return
    Column(modifier, horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(2.dp)) {
        lines.forEach { line ->
            Text(
                line.uppercase().replace(' ', '_'),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.labelSmall,
                fontSize = 8.sp,
                maxLines = 1,
            )
        }
    }
}
