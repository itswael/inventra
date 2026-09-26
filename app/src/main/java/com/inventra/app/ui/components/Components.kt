package com.inventra.app.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.inventra.app.domain.model.DailySalesTrend
import com.inventra.app.ui.theme.LossRedLight
import com.inventra.app.ui.theme.ProfitGreenLight
import com.inventra.app.ui.theme.WarningYellow
import java.text.NumberFormat
import java.util.Locale

// ── Stat Card ─────────────────────────────────────────────────────────────────

@Composable
fun StatCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    valueColor: Color = MaterialTheme.colorScheme.onSurface,
    subtitle: String? = null
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = valueColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// ── Stock Level Indicator ─────────────────────────────────────────────────────

@Composable
fun StockDot(color: Color, size: Dp = 10.dp) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(color)
    )
}

fun stockDotColor(qty: Double, minLevel: Double): Color = when {
    qty <= 0 -> LossRedLight
    minLevel > 0 && qty <= minLevel -> WarningYellow
    else -> ProfitGreenLight
}

// ── Currency formatting ────────────────────────────────────────────────────────

fun formatCurrency(amount: Double, symbol: String = "$"): String {
    val fmt = NumberFormat.getNumberInstance(Locale.getDefault())
    fmt.maximumFractionDigits = 2
    fmt.minimumFractionDigits = 2
    return "$symbol${fmt.format(amount)}"
}

fun formatQty(qty: Double): String {
    val fmt = NumberFormat.getNumberInstance(Locale.getDefault())
    fmt.maximumFractionDigits = if (qty % 1.0 == 0.0) 0 else 2
    return fmt.format(qty)
}

// ── Section Header ─────────────────────────────────────────────────────────────

@Composable
fun SectionHeader(title: String, modifier: Modifier = Modifier) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
        modifier = modifier
    )
}

// ── Empty State ────────────────────────────────────────────────────────────────

@Composable
fun EmptyState(
    icon: String = "📦",
    title: String,
    subtitle: String? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = icon, fontSize = 48.sp)
        Spacer(Modifier.height(16.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.SemiBold
        )
        if (subtitle != null) {
            Spacer(Modifier.height(8.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// ── Bar Chart (Canvas-based, no external library) ─────────────────────────────

@Composable
fun TrendBarChart(
    trends: List<DailySalesTrend>,
    modifier: Modifier = Modifier,
    revenueColor: Color = MaterialTheme.colorScheme.primary,
    profitColor: Color = ProfitGreenLight
) {
    if (trends.isEmpty()) {
        EmptyState("📊", "No trend data", "Add sales to see trends here", modifier)
        return
    }
    val maxValue = trends.maxOf { maxOf(it.totalRevenue, it.totalProfit) }.coerceAtLeast(1.0)

    // Animate bars on first composition
    var animTarget by remember { mutableFloatStateOf(0f) }
    val animProgress by animateFloatAsState(
        targetValue = animTarget,
        animationSpec = tween(durationMillis = 600),
        label = "barAnim"
    )
    LaunchedEffect(trends) { animTarget = 1f }

    Column(modifier = modifier) {
        androidx.compose.foundation.Canvas(
            modifier = Modifier.fillMaxWidth().height(160.dp)
        ) {
            val count = trends.size
            val groupWidth = size.width / count
            val barWidth = groupWidth * 0.35f
            val gap = 2f

            trends.forEachIndexed { i, trend ->
                val x = i * groupWidth + groupWidth * 0.1f

                // Revenue bar
                val rh = (trend.totalRevenue / maxValue * size.height * animProgress).toFloat()
                drawRoundRectSafe(
                    color = revenueColor.copy(alpha = 0.85f),
                    left = x,
                    height = rh,
                    barWidth = barWidth,
                    canvasHeight = size.height
                )

                // Profit bar (overlaid, narrower)
                val ph = (trend.totalProfit / maxValue * size.height * animProgress).toFloat()
                drawRoundRectSafe(
                    color = profitColor.copy(alpha = 0.9f),
                    left = x + barWidth + gap,
                    height = ph,
                    barWidth = barWidth * 0.7f,
                    canvasHeight = size.height
                )
            }
        }
        Spacer(Modifier.height(6.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            trends.forEach { trend ->
                Text(
                    text = trend.label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )
            }
        }
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            LegendDot(revenueColor, "Revenue")
            LegendDot(profitColor, "Profit")
        }
    }
}

private fun DrawScope.drawRoundRectSafe(color: Color, left: Float, height: Float, barWidth: Float, canvasHeight: Float) {
    if (height <= 0f) return
    drawRoundRect(
        color = color,
        topLeft = Offset(left, canvasHeight - height),
        size = Size(barWidth, height),
        cornerRadius = CornerRadius(4f)
    )
}

@Composable
fun LegendDot(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Box(Modifier.size(8.dp).clip(CircleShape).background(color))
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

// ── Horizontal bar for product rankings ───────────────────────────────────────

@Composable
fun HorizontalBar(fraction: Float, color: Color, modifier: Modifier = Modifier) {
    val anim by animateFloatAsState(targetValue = fraction.coerceIn(0f, 1f), animationSpec = tween(500), label = "hbar")
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(6.dp)
            .clip(RoundedCornerShape(3.dp))
            .background(color.copy(alpha = 0.15f))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(anim)
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(color)
        )
    }
}

// ── Section Chip ──────────────────────────────────────────────────────────────

@Composable
fun PeriodChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
        contentColor = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
        tonalElevation = if (selected) 2.dp else 0.dp
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}
