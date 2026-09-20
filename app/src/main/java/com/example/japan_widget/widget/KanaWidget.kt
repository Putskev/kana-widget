package com.example.japan_widget.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.LocalSize
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.RowScope
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.example.japan_widget.data.KanaStateManager
import com.example.japan_widget.data.Script
import com.example.japan_widget.data.WidgetCard
import kotlin.math.max
import kotlin.math.min

private val ScrimColor = Color(0xAA1A1A1A)
private val WhiteText = ColorProvider(Color.White)
private val MutedWhiteText = ColorProvider(Color(0xCCFFFFFF))

/** Below this height there isn't room for the multi-line layout: switch to the 2-line compact one. */
private val RowHeightThreshold = 70.dp

private val NarrowWidthThreshold = 180.dp

/** Small and constant so the text gets as much of the widget as possible, even at 1 grid cell. */
private val WidgetPadding = 8.dp
private val WidgetCornerRadius = 12.dp
private val CharEndPadding = 6.dp

class KanaWidget : GlanceAppWidget() {

    // Recomposes for the app widget's actual current size instead of a handful of fixed
    // breakpoints, so font sizes can be derived directly from the real LocalSize.
    override val sizeMode = SizeMode.Exact

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val card = KanaStateManager(context).current()
        provideContent {
            WidgetContent(card)
        }
    }
}

@Composable
private fun WidgetContent(card: WidgetCard?) {
    val size = LocalSize.current
    val isNarrow = size.width < NarrowWidthThreshold
    val isRowHeight = size.height < RowHeightThreshold

    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(ScrimColor)
            .cornerRadius(WidgetCornerRadius)
            .padding(WidgetPadding)
            .clickable(actionRunCallback<NextCharacterAction>()),
        contentAlignment = Alignment.CenterStart
    ) {
        if (card == null) {
            Text(
                text = "Keine Zeichen verfügbar",
                style = TextStyle(color = WhiteText, fontSize = 14.sp),
                maxLines = 1
            )
            return@Box
        }

        Row(
            modifier = GlanceModifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isRowHeight) {
                CompactRow(card, size)
            } else {
                FullRow(card, size, isNarrow)
            }
        }
    }
}

/**
 * 2-line layout for row-height (1 grid cell) placements. Font sizes scale directly off the
 * widget's actual height/width (from [SizeMode.Exact]) so the text fills the available space
 * instead of relying on a handful of hand-picked breakpoints.
 */
@Composable
private fun RowScope.CompactRow(card: WidgetCard, size: DpSize) {
    val heightValue = size.height.value
    val line1Sp = max(13f, heightValue * 0.18f)
    val line2Sp = max(18f, heightValue * 0.25f)
    var charSp = max(36f, heightValue * 0.6f)

    // For 3+ character kanji words, cap the font by the width actually available so the word
    // never gets clipped; full-width (CJK) glyphs are roughly as wide as they are tall.
    var charMaxWidth: Dp? = null
    val charCount = card.char.length
    if (charCount >= 3) {
        val innerWidth = (size.width - WidgetPadding * 2 - CharEndPadding).coerceAtLeast(24.dp)
        val budget = innerWidth * 0.6f
        charMaxWidth = budget
        val widthBasedSp = (budget.value / charCount) / 1.1f
        charSp = min(charSp, widthBasedSp).coerceAtLeast(16f)
    }

    Text(
        text = card.char,
        style = TextStyle(color = WhiteText, fontSize = charSp.sp, fontWeight = FontWeight.Bold),
        maxLines = 1,
        modifier = GlanceModifier
            .let { if (charMaxWidth != null) it.width(charMaxWidth) else it }
            .padding(end = CharEndPadding)
    )

    Column(modifier = GlanceModifier.defaultWeight()) {
        Text(
            text = card.line1,
            style = TextStyle(color = MutedWhiteText, fontSize = line1Sp.sp),
            maxLines = 1
        )
        if (card.line2Bold != null) {
            Text(
                text = card.line2Bold,
                style = TextStyle(color = WhiteText, fontSize = line2Sp.sp, fontWeight = FontWeight.Bold),
                // Long translations wrap to a 2nd line rather than getting cut off; anything
                // still too long is ellipsized (the default Glance text style already sets that).
                maxLines = 2
            )
        }
    }
}

/** Full multi-line layout for medium/large placements with enough vertical room. */
@Composable
private fun RowScope.FullRow(card: WidgetCard, size: DpSize, isNarrow: Boolean) {
    val charFontSp = fullCharFontSize(card, isNarrow)

    Text(
        text = card.char,
        style = TextStyle(color = WhiteText, fontSize = charFontSp, fontWeight = FontWeight.Bold),
        maxLines = 1,
        modifier = GlanceModifier.padding(end = 12.dp)
    )

    Column(modifier = GlanceModifier.defaultWeight()) {
        Text(
            text = card.line1,
            style = TextStyle(color = MutedWhiteText, fontSize = 13.sp),
            maxLines = 1
        )

        // The translation must stay visible at every size, regardless of width.
        if (card.line2Bold != null) {
            Text(
                text = card.line2Bold,
                style = TextStyle(
                    color = WhiteText,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                ),
                maxLines = 2
            )
        }

        if (!isNarrow) {
            if (card.line3 != null) {
                Text(
                    text = card.line3,
                    style = TextStyle(color = MutedWhiteText, fontSize = 13.sp),
                    maxLines = 1
                )
                // Extra note line, shown only when there is room for it.
                if (card.note != null) {
                    Text(
                        text = card.note,
                        style = TextStyle(color = MutedWhiteText, fontSize = 10.sp),
                        maxLines = 1
                    )
                }
            } else if (card.note != null) {
                Text(
                    text = card.note,
                    style = TextStyle(color = MutedWhiteText, fontSize = 11.sp, textAlign = TextAlign.Start),
                    maxLines = 2
                )
            }
        }
    }
}

/**
 * Kana chars are always a single character, so they keep the fixed size. Kanji words can be
 * 1-4+ characters long, so the size steps down as the word grows to avoid clipping.
 */
private fun fullCharFontSize(card: WidgetCard, isNarrow: Boolean) = when {
    card.script != Script.KANJI -> if (isNarrow) 32.sp else 44.sp
    card.char.length == 1 -> if (isNarrow) 32.sp else 44.sp
    card.char.length == 2 -> if (isNarrow) 26.sp else 36.sp
    else -> if (isNarrow) 18.sp else 26.sp
}
