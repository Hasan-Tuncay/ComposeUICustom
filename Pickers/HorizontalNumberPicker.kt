package com.clappy.caloriesteptracker.ui.customUI.Pickers

import android.util.Log
import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalDensity
import com.clappy.caloriesteptracker.ui.customUI.Pickers.VerticalNumberPickerStyle
import kotlin.math.roundToInt
import androidx.compose.runtime.Composable

val TAG1 = "HorizontalNumberPicker"


@Composable
fun HorizontalNumberPicker(
    modifier: Modifier = Modifier,
    range: IntRange,
    initialValue: Int = 16,
    style: HorizontalNumberPickerStyle = HorizontalNumberPickerStyle(),
    onValueSelected: (Int) -> Unit
) {

//// Ensure initialIndex is within the valid range
//    val safeInitialIndex =
//       safeInitialIndex.coerceIn(0, range.last - 1)
//
//
//// Now you can safely use `safeInitialIndex`
    val initialIndex =  range.indexOf(initialValue).takeIf { it >= 0 } ?: (range.count() / 2)
    var currentIndex by remember { mutableStateOf(initialIndex) }
    val coroutineScope = rememberCoroutineScope()
    val density = LocalDensity.current

    // Scroll sensitivity (e.g., 30dp)
    val scrollThreshold = with(density) { 4.dp.toPx() }
    var accumulatedDrag by remember { mutableStateOf(0f) }
    val animatedOffset = remember { Animatable(0f) }

    Box(
        modifier = modifier
            .then(
                Modifier
                    .width(style.pickerWidth)
                    .height(style.pickerHeight)
            )
            .background(
                color = style.containerColor,
                shape = RoundedCornerShape(style.borderRadius)
            )

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
        Canvas(modifier = modifier
            .then(
                Modifier
                    .width(style.pickerWidth)
                    .height(style.pickerHeight)
            )  // Ekstra genişlik/yükseklik ayarları

            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onDragEnd = {
                        // Handle the wrap-around logic for the ending index
                        val closestIndex =
                            (currentIndex + (accumulatedDrag / scrollThreshold).roundToInt())
                                .let {
                                    when {
                                        it > range.last -> range.first  // Wrap around to the start
                                        it < range.first -> range.last  // Wrap around to the end
                                        else -> it.coerceIn(range.first, range.last)
                                    }
                                }
                        currentIndex = closestIndex
                        accumulatedDrag = 0f
                    },
                    onHorizontalDrag = { _, dragAmount ->
                        accumulatedDrag += dragAmount
                        if (accumulatedDrag >= scrollThreshold) {
                            coroutineScope.launch {
                                currentIndex = (currentIndex + 1).let {
                                    if (it > range.last) range.first else it
                                }
                            }
                            accumulatedDrag = 0f
                        } else if (accumulatedDrag <= -scrollThreshold) {
                            coroutineScope.launch {
                                currentIndex = (currentIndex - 1).let {
                                    if (it < range.first) range.last else it
                                }
                            }
                            accumulatedDrag = 0f
                        }
                    }
                )
            }) {

            val separatorWidthPx = with(density) { style.separatorWidth.toPx() }
            val separatorHeightPx = with(density) { style.separatorHeight.toPx() }
            val boldNumberTextSizePx = with(density) { style.boldNumberTextSize.toPx() }
            val numberTextSizePx = with(density) { style.numberTextSize.toPx() }
            val numberTextSizeDeduct = numberTextSizePx / 2

            drawIntoCanvas { canvas ->
                val paintBold = android.graphics.Paint().apply {
                    color = style.boldNumberTextColor.toArgb()
                    textSize = boldNumberTextSizePx
                    textAlign = android.graphics.Paint.Align.CENTER
                    typeface = android.graphics.Typeface.DEFAULT_BOLD
                }

                val paintNormal = android.graphics.Paint().apply {
                    color = style.numberTextColor.toArgb()
                    textSize = numberTextSizePx
                    textAlign = android.graphics.Paint.Align.CENTER
                }

                // Safely handle the previous index with wrap-around logic
                val previousIndex =
                    if (currentIndex == range.first) range.last else currentIndex - 1
                canvas.nativeCanvas.drawText(
                    range.elementAt(previousIndex % range.count()).toString(),
                    (size.width * 1 / 6),
                    size.height / 2 + numberTextSizeDeduct,
                    paintNormal
                )

                drawOval(
                    brush = style.separatorBrush,
                    topLeft = Offset(x = size.width * 2 / 3 - separatorWidthPx / 2, y = (0f)),
                    size = Size(width = separatorWidthPx, height = size.height)
                )
                // Safely handle the middle index with wrap-around logic
                canvas.nativeCanvas.drawText(
                    range.elementAt(currentIndex % range.count()).toString(),
                    (size.width * 0.5).toFloat(),
                    size.height / 2 + numberTextSizeDeduct,
                    paintBold
                ).also { onValueSelected(range.elementAt(currentIndex % range.count())) }


// Vertical Oval (Previous number and middle number)
                drawOval(
                    brush = style.separatorBrush,
                    topLeft = Offset(x = size.width / 3 - separatorWidthPx / 2, y = (0f)),
                    size = Size(width = separatorWidthPx, height = size.height)
                )
                // Safely handle the next index with wrap-around logic
                val nextIndex = if (currentIndex == range.last) range.first else currentIndex + 1
                canvas.nativeCanvas.drawText(
                    range.elementAt(nextIndex % range.count()).toString(),
                    (size.width * 5 / 6),
                    size.height / 2 + numberTextSizeDeduct,
                    paintNormal
                )
            }
        }
    }
}


@Preview
@Composable
fun HorizontalNumberPickerPreview() {


    Column(
        Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        HorizontalNumberPicker(
            modifier = Modifier,
            range = 1..99, // 18 ile 99 arası yaş
            initialValue = 12,

            ) { selectedAge ->

        }


    }


}

