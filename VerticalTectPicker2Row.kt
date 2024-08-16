package com.clappy.caloriesteptracker.ui.customUI.Pickers

import android.util.Log
import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun VerticalTextPicker2Row(
    modifier: Modifier = Modifier,
    items: List<String>,
    initialText: String = items.first(),
    style: VerticalNumberPickerStyle = VerticalNumberPickerStyle(),
    onValueSelected: (String) -> Unit
) {
    var currentIndex by remember { mutableStateOf(items.indexOf(initialText).coerceIn(0, items.size - 1)) }
    val coroutineScope = rememberCoroutineScope()
    val density = LocalDensity.current

    // Scroll sensitivity (e.g., 30dp)
    val scrollThreshold = with(density) { 40.dp.toPx() }
    var accumulatedDrag by remember { mutableStateOf(0f) }
    val animatedOffset = remember { Animatable(0f) }

    Box(
        modifier = modifier
            .clip(androidx.compose.foundation.shape.RoundedCornerShape(style.borderRadius))
            .drawBehind {
                drawRoundRect(
                    color = style.borderColor,
                    size = this.size,
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(style.borderRadius.toPx()),
                    style = Stroke(width = style.borderThickness.toPx())
                )
            }
            .padding(style.borderThickness)
    ) {
        Canvas(modifier = Modifier
            .height(style.pickerHeight)
            .width(style.pickerWidth)
            .pointerInput(Unit) {
                detectVerticalDragGestures(
                    onDragEnd = {
                        // Handle the wrap-around logic for the ending index
                        val closestIndex =
                            (currentIndex + (accumulatedDrag / scrollThreshold).roundToInt())
                                .let {
                                    when {
                                        it > items.size - 1 -> 0  // Wrap around to the start
                                        it < 0 -> items.size - 1  // Wrap around to the end
                                        else -> it.coerceIn(0, items.size - 1)
                                    }
                                }
                        currentIndex = closestIndex
                        Log.d("VerticalTextPicker", "currentIndex: ${currentIndex}")
                        accumulatedDrag = 0f
                    },
                    onVerticalDrag = { _, dragAmount ->
                        accumulatedDrag += dragAmount
                        if (accumulatedDrag >= scrollThreshold) {
                            coroutineScope.launch {
                                currentIndex = (currentIndex + 1).let {
                                    if (it > items.size - 1) 0 else it
                                }
                            }
                            accumulatedDrag = 0f
                        } else if (accumulatedDrag <= -scrollThreshold) {
                            coroutineScope.launch {
                                currentIndex = (currentIndex - 1).let {
                                    if (it < 0) items.size - 1 else it
                                }
                            }
                            accumulatedDrag = 0f

                            Log.d("VerticalTextPicker", "currentIndex: ${currentIndex}")
                        }
                    }
                )
            }) {

            val separatorWidthPx = with(density) { style.separatorWidth.toPx() }
            val separatorHeightPx = with(density) { style.separatorHeight.toPx() }
            val boldTextSizePx = with(density) { style.boldNumberTextSize.toPx() }
            val normalTextSizePx = with(density) { style.numberTextSize.toPx() }
            val textSizeDeduct = normalTextSizePx / 2

            drawIntoCanvas { canvas ->
                val paintBold = android.graphics.Paint().apply {
                    color = style.boldNumberTextColor.toArgb()
                    textSize = boldTextSizePx
                    textAlign = android.graphics.Paint.Align.CENTER
                    typeface = android.graphics.Typeface.DEFAULT_BOLD
                }

                val paintNormal = android.graphics.Paint().apply {
                    color = style.numberTextColor.toArgb()
                    textSize = normalTextSizePx
                    textAlign = android.graphics.Paint.Align.CENTER
                }



                // Oval Separator (Previous text and middle text)
                drawOval(
                    brush = style.separatorBrush,
                    size = Size(width = separatorWidthPx, height = separatorHeightPx),
                    topLeft = Offset(
                        x = (size.width / 2) - (separatorWidthPx / 2),
                        y = size.height / 3
                    )
                )

                // Safely handle the middle index with wrap-around logic
                canvas.nativeCanvas.drawText(
                    items[currentIndex],
                    size.width / 2,
                    ((size.height * 0.5) + textSizeDeduct).toFloat(),
                    paintBold
                ).also { onValueSelected(items[currentIndex])
                    Log.d("VerticalTextPicker", "currentIndex: ${currentIndex}")
                }

                // Oval Separator (Middle text and next text)
                drawOval(
                    brush = style.separatorBrush,
                    size = Size(width = separatorWidthPx, height = separatorHeightPx),
                    topLeft = Offset(
                        x = (size.width / 2) - (separatorWidthPx / 2),
                        y = size.height * 2 / 3
                    )
                )

                // Safely handle the next index with wrap-around logic
                val nextIndex = if (currentIndex == items.size - 1) 0 else currentIndex + 1
                canvas.nativeCanvas.drawText(
                    items[nextIndex],
                    size.width / 2,
                    (size.height * 5 / 6) + textSizeDeduct,
                    paintNormal
                )
            }
        }
    }
}
