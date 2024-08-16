package com.clappy.caloriesteptracker.ui.customUI.Pickers

import VerticalNumberPicker
import android.util.Log
import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
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
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

val TAG = "VerticalNumberPicker"


@Composable
private fun VerticalNumberPickerInner(
    modifier: Modifier = Modifier,
    range: IntRange,
    initialIndex: Int = 0,
    style: VerticalNumberPickerStyle,
    onValueSelected: (Int) -> Unit
) {
    var currentIndex by remember { mutableStateOf(initialIndex) }
    val coroutineScope = rememberCoroutineScope()
    val density = LocalDensity.current

    // Kaydırma hassasiyeti (örneğin 30dp)
    val scrollThreshold = with(density) { 4.dp.toPx() }
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
                                        it > range.last -> range.first  // Wrap around to the start
                                        it < range.first -> range.last  // Wrap around to the end
                                        else -> it.coerceIn(range.first, range.last)
                                    }
                                }
                        currentIndex = closestIndex
                        //  onValueSelected(range.elementAt(currentIndex))
                        accumulatedDrag = 0f
                    },


                    onVerticalDrag = { _, dragAmount ->
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
                val previousIndex = if (currentIndex == range.first) range.last else currentIndex - 1
                canvas.nativeCanvas.drawText(
                    range.elementAt(previousIndex % range.count()).toString(),
                    size.width / 2,
                    (size.height * 1 / 6) + numberTextSizeDeduct,
                    paintNormal
                )





                // Safely handle the middle index with wrap-around logic
                canvas.nativeCanvas.drawText(
                    range.elementAt(currentIndex % range.count()).toString(),
                    size.width / 2,
                    ((size.height * 0.5) + numberTextSizeDeduct).toFloat(),
                    paintBold
                ).also { onValueSelected(range.elementAt(currentIndex % range.count())) }


                // Safely handle the next index
                // Safely handle the next index with wrap-around logic
                val nextIndex = if (currentIndex == range.last) range.first else currentIndex + 1
                canvas.nativeCanvas.drawText(
                    range.elementAt(nextIndex % range.count()).toString(),
                    size.width / 2,
                    (size.height * 5 / 6) + numberTextSizeDeduct,
                    paintNormal
                )
            }
        }
    }
}

@Composable
private fun VerticalTextPickerInner(
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
    val scrollThreshold = with(density) { 10.dp.toPx() }
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

//                // Safely handle the previous index with wrap-around logic
//                val previousIndex = if (currentIndex == 0) items.size - 1 else currentIndex - 1
//                canvas.nativeCanvas.drawText(
//                    items[previousIndex],
//                    size.width / 2,
//                    (size.height * 1 / 6) + textSizeDeduct,
//                    paintNormal
//                )



                // Safely handle the middle index with wrap-around logic
                canvas.nativeCanvas.drawText(
                    items[currentIndex],
                    size.width / 2,
                    ((size.height * 0.5) + textSizeDeduct).toFloat(),
                    paintBold
                ).also { onValueSelected(items[currentIndex])
                    Log.d("VerticalTextPicker", "currentIndex: ${currentIndex}")
                }



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




@Composable
fun CombinedPicker(
    modifier: Modifier=Modifier,
    numberRange: IntRange,
    numberInitialIndex: Int = 16,
    textItems: List<String>,
    textInitialText: String = textItems.first(),
    style: VerticalNumberPickerStyle = VerticalNumberPickerStyle(),
    onNumberSelected: (Int) -> Unit,
    onTextSelected: (String) -> Unit
) {
    val density = LocalDensity.current
    val styleWithoutBorder = style.copy(
        borderThickness = 0.dp,
        borderRadius = 0.dp,
        borderColor = Color.Transparent,
        pickerWidth = 75.dp,

    )
    val seperatorWidthPx=   styleWithoutBorder.pickerWidth+ styleWithoutBorder.pickerWidth
    var rowWidth by remember { mutableStateOf(0) }


    val separatorHeightPx = with(density) { style.separatorHeight.toPx() }
val rowWidthPx= with(density){rowWidth.toFloat()}
    Row(
        modifier = Modifier
            .width(IntrinsicSize.Min)
            .height(IntrinsicSize.Min)
            .onGloballyPositioned { coordinates ->
                rowWidth = coordinates.size.width
            }
    ) {
        // Draw a separator between the pickers
        Canvas(modifier = Modifier
            .fillMaxHeight()
            .width(2.dp)) {
            drawOval(
                brush = style.separatorBrush,
                size = Size(width = seperatorWidthPx.toPx(), height = separatorHeightPx),
                topLeft = Offset(
                    x = 0f,
                    y = (size.height / 1/3) - (separatorHeightPx / 2)
                )
            )
        }
        // Draw a separator between the pickers
        Canvas(modifier = Modifier
            .fillMaxHeight()
            .width(2.dp)) {
            drawOval(
                brush = style.separatorBrush,
                size = Size(width = seperatorWidthPx.value.dp.toPx(), height = separatorHeightPx),
                topLeft = Offset(
                    x = -0f,
                    y = (size.height *2/3) - (separatorHeightPx / 2)
                )
            )
        }
        Log.i("CombinedPicker", "CombinedPicker: $seperatorWidthPx")
        VerticalNumberPickerInner(
            modifier = Modifier.weight(1f),
            range = numberRange,
            initialIndex = numberInitialIndex,
            style = styleWithoutBorder,
            onValueSelected = onNumberSelected
        )



        VerticalTextPickerInner(
            modifier = Modifier.weight(1f),
            items = textItems,
            initialText = textInitialText,
            style = styleWithoutBorder,
            onValueSelected = onTextSelected
        )
    }
}
@Preview
@Composable
fun ExampleUsage() {
    CombinedPicker(
        numberRange = 1..100,
        numberInitialIndex = 50,
        textItems = listOf("One", "Two", "Three"),
        textInitialText = "Two",

        onNumberSelected = { number -> /* Handle number selection */ },
        onTextSelected = { text -> /* Handle text selection */ }
    )
}