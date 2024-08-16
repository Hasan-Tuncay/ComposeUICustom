package com.clappy.caloriesteptracker.ui.customUI.Pickers


import android.util.Log
import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
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
import kotlinx.coroutines.launch
import java.time.LocalTime
import kotlin.math.roundToInt


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
                    String.format("%02d", range.elementAt(previousIndex % range.count())),
                    size.width / 2,
                    (size.height * 1 / 6) + numberTextSizeDeduct,
                    paintNormal
                )





                // Safely handle the middle index with wrap-around logic
                canvas.nativeCanvas.drawText(
                    String.format("%02d", range.elementAt(currentIndex % range.count())), // Ensure two digits
                    size.width / 2,
                    ((size.height * 0.5) + numberTextSizeDeduct).toFloat(),
                    paintBold
                ).also { onValueSelected(range.elementAt(currentIndex % range.count())) }


                // Safely handle the next index
                // Safely handle the next index with wrap-around logic
                val nextIndex = if (currentIndex == range.last) range.first else currentIndex + 1
                canvas.nativeCanvas.drawText(
                    String.format("%02d", range.elementAt(nextIndex % range.count())),
                    size.width / 2,
                    (size.height * 5 / 6) + numberTextSizeDeduct,
                    paintNormal
                )
            }
        }
    }
}





@Composable
fun VerticalTimePicker(
    modifier: Modifier = Modifier,
    style: VerticalNumberPickerStyle = VerticalNumberPickerStyle(pickerWidth = 30.dp),
    onTimeSelected: (LocalTime) -> Unit
) {
    val density = LocalDensity.current
    val styleWithoutBorder = style.copy(
        borderThickness = 0.dp,
        borderRadius = 0.dp,
        borderColor = Color.Transparent,
        pickerWidth = 60.dp,

        )
    val seperatorWidthPx=   styleWithoutBorder.pickerWidth+ styleWithoutBorder.pickerWidth
    var rowWidth by remember { mutableStateOf(0) }

    val currentHour = LocalTime.now().hour
    val currentMinute = LocalTime.now().minute
    val separatorHeightPx = with(density) { style.separatorHeight.toPx() }
    val rowWidthPx= with(density){rowWidth.toFloat()}

    var selectedHour by remember { mutableStateOf(currentHour) }
    var selectedMinute by remember { mutableStateOf(currentMinute) }

    val hourRange = 0..23
    val minuteRange = 0..59
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

        VerticalNumberPickerInner(
            modifier = Modifier.weight(1f),
            range = hourRange,
            initialIndex = selectedHour,
            style = styleWithoutBorder,
            onValueSelected = {
                selectedHour = it
                onTimeSelected(LocalTime.of(selectedHour, selectedMinute))
            }
        )

        VerticalNumberPickerInner(
            modifier = Modifier.weight(1f),
            range = minuteRange,
            initialIndex = selectedMinute,
            style = styleWithoutBorder,
            onValueSelected = {
                selectedMinute = it
                onTimeSelected(LocalTime.of(selectedHour, selectedMinute))
            }
        )


    }
}
@Preview
@Composable
fun VerticalTimePickerPreview() {
   VerticalTimePicker {

       Log.i("VerticalTimePicker", "Localtime: $it")
   }
}