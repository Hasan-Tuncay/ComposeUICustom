package com.clappy.caloriesteptracker.ui.customUI.Pickers

import android.util.Log
import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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

  val TAG2 = "HorizontalTextPicker"

@Composable
fun HorizontalTextPicker3Column(
    modifier: Modifier = Modifier,
    items: List<String>,
    initialIndex: Int = 0,
    style: HorizontalNumberPickerStyle = HorizontalNumberPickerStyle(),
    onValueSelected: (String) -> Unit
) {
    var currentIndex by remember { mutableStateOf(initialIndex) }
    val coroutineScope = rememberCoroutineScope()
    val density = LocalDensity.current

    // Scroll sensitivity (e.g., 30dp)
    val scrollThreshold = with(density) { 60.dp.toPx() }
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
            .width(style.pickerWidth)
            .height(style.pickerHeight)
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
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
                        accumulatedDrag = 0f
                    },
                    onHorizontalDrag = { _, dragAmount ->
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
                val previousIndex = if (currentIndex == 0) items.size - 1 else currentIndex - 1
                canvas.nativeCanvas.drawText(
                    items[previousIndex],
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
                    items[currentIndex],
                    (size.width * 0.5).toFloat(),
                    size.height / 2 + numberTextSizeDeduct,
                    paintBold
                ).also { onValueSelected(items[currentIndex]) }

                // Vertical Oval (Previous number and middle number)
                drawOval(
                    brush = style.separatorBrush,
                    topLeft = Offset(x = size.width / 3 - separatorWidthPx / 2, y = (0f)),
                    size = Size(width = separatorWidthPx, height = size.height)
                )
                // Safely handle the next index with wrap-around logic
                val nextIndex = if (currentIndex == items.size - 1) 0 else currentIndex + 1
                canvas.nativeCanvas.drawText(
                    items[nextIndex],
                    (size.width * 5 / 6),
                    size.height / 2 + numberTextSizeDeduct,
                    paintNormal
                )
            }
        }
    }
}
@Composable
fun HorizontalTextPicker2Column(
    modifier: Modifier = Modifier,
    items: List<String>,
    initialIndex: Int = 0,
    style: HorizontalNumberPickerStyle = HorizontalNumberPickerStyle(),
    onValueSelected: (String) -> Unit
) {
    var currentIndex by remember { mutableStateOf(initialIndex) }
    val coroutineScope = rememberCoroutineScope()
    val density = LocalDensity.current

    // Scroll sensitivity (e.g., 30dp)
    val scrollThreshold = with(density) { 15.dp.toPx() }
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
            .width(style.pickerWidth)
            .height(style.pickerHeight)
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
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
                        accumulatedDrag = 0f
                    },
                    onHorizontalDrag = { _, dragAmount ->
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


                canvas.nativeCanvas.drawText(
                    items[currentIndex],
                    (size.width * 1/4).toFloat(),
                    size.height / 2 + numberTextSizeDeduct,
                    paintBold
                ).also { onValueSelected(items[currentIndex]) }

                // Vertical Oval (Previous number and middle number)
                drawOval(
                    brush = style.separatorBrush,
                    topLeft = Offset(x = size.width / 2 - separatorWidthPx / 2, y = (0f)),
                    size = Size(width = separatorWidthPx, height = size.height)
                )
                // Safely handle the next index with wrap-around logic
                val nextIndex = if (currentIndex == items.size - 1) 0 else currentIndex + 1
                canvas.nativeCanvas.drawText(
                    items[nextIndex],
                    (size.width * 3 / 4),
                    size.height / 2 + numberTextSizeDeduct,
                    paintNormal
                )
            }
        }
    }
}

@Preview
@Composable
fun HorizontalTextPickerPreview() {



    Column(
        Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        HorizontalTextPicker2Column(
            modifier = Modifier,
             items = listOf("Kj","Kcal"),
            initialIndex = 0,

            ) { selectedAge ->

        }


    }


}