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
  val TAG = "VerticalNumberPicker"


@Composable
fun VerticalNumberPicker(
    modifier: Modifier = Modifier,
    range: IntRange,
    initialIndex: Int = 16,
    style: VerticalNumberPickerStyle,
    onValueSelected: (Int) -> Unit
) {
    var currentIndex by remember { mutableStateOf(initialIndex) }
    val coroutineScope = rememberCoroutineScope()
    val density = LocalDensity.current

    // Kaydırma hassasiyeti (örneğin 30dp)
    val scrollThreshold = with(density) { 14.dp.toPx() }
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

            val yOffset = size.height

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
                val previousIndex = if (currentIndex == range.first) range.last else currentIndex - 1
                canvas.nativeCanvas.drawText(
                    range.elementAt(previousIndex % range.count()).toString(),
                    size.width / 2,
                    (size.height * 1 / 6) + numberTextSizeDeduct,
                    paintNormal
                )



                // Oval Separator (Önceki rakam ile ortadaki rakam arası)
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
                    range.elementAt(currentIndex % range.count()).toString(),
                    size.width / 2,
                    ((size.height * 0.5) + numberTextSizeDeduct).toFloat(),
                    paintBold
                ).also { onValueSelected(range.elementAt(currentIndex % range.count())) }

                // Oval Separator (Ortadaki rakam ile sonraki rakam arası)
                drawOval(
                    brush = style.separatorBrush,
                    size = Size(width = separatorWidthPx, height = separatorHeightPx),
                    topLeft = Offset(
                        x = (size.width / 2) - (separatorWidthPx / 2),
                        y = size.height * 2 / 3
                    )
                )

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


//•	Artan/Azalan Sıralama: Sayıların artan veya azalan sırayla gösterilmesi.
//•	Metin Hizalama: Sayıların metin hizalamasını belirleme.
//•	Sarma (Wrap Around): Sayılar uç değerlerde tekrar başa dönme özelliği.
//•	Kaydırma, Dokunma, Uzun Basma: Kullanıcı etkileşimleri için dokunma, kaydırma ve uzun basma işlevleri.
//•	Özelleştirilebilir Yazı Tipi ve Stili: Metin rengi, yazı tipi, yazı kalınlığı gibi stil ayarları.



@Preview
@Composable
fun VerticalNumberPickerScreen2() {
    val pickerStyle = VerticalNumberPickerStyle(
        numberTextSize = 20.sp,
        numberTextColor = Color.Gray,
        boldNumberTextSize = 20.sp,
        boldNumberTextColor = Color.Black,
        separatorWidth = 100.dp,
        separatorHeight = 4.dp,
        separatorBrush = Brush.horizontalGradient(
            colors = listOf(Color.Blue, Color.Cyan, Color.Green)
        ),

        pickerWidth = 100.dp,
        pickerHeight = 150.dp,
        borderRadius = 12.dp,
        borderColor = Color.Gray,
        borderThickness = 2.dp
    )


    Column(
        Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        VerticalNumberPicker(
            modifier = Modifier,
            range = 1..99, // 18 ile 99 arası yaş
            initialIndex = 12,
            style = pickerStyle
        ) { selectedAge ->
            Log.i(TAG, "VerticalNumberPickerScreen2: $selectedAge")
        }


    }


}




