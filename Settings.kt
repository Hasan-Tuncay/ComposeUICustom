package com.clappy.caloriesteptracker.ui.customUI.Pickers

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


data class VerticalNumberPickerStyle(
    val numberTextSize: TextUnit = 18.sp,
    val numberTextColor: Color = Color(0xFF429EBB),
    val boldNumberTextSize: TextUnit = 18.sp,
    val boldNumberTextColor: Color = Color(0xFF007398),
    val separatorWidth: Dp = 100.dp,
    val separatorHeight: Dp = 2.dp,
    val separatorBrush: Brush = Brush.verticalGradient(
        colors = listOf(
            Color(0xFFF6FAFE),
            Color(0xFFC6EDFF),
            Color(0xFFAAD4F5),
            Color(0xFFAAD4F5),
            Color(0xFFAAD4F5),

            Color(0xFFC6EDFF),
            Color(0xFFF6FAFE),
        )
    ),
    val pickerWidth: Dp = 150.dp,
    val pickerHeight: Dp = 150.dp,
    val borderColor: Color = Color.Transparent,
    val borderThickness: Dp = 0.dp,
    val borderRadius: Dp = 0.dp
)

data class HorizontalNumberPickerStyle(
    val numberTextSize: TextUnit = 18.sp,
    val numberTextColor: Color = Color(0xFF429EBB),
    val boldNumberTextSize: TextUnit = 18.sp,
    val boldNumberTextColor: Color = Color(0xFF007398),
    val separatorWidth: Dp = 2.dp,
    val separatorHeight: Dp = 30.dp,
    val separatorBrush: Brush = Brush.verticalGradient(
        colors = listOf(
            Color(0xFFF6FAFE),
            Color(0xFFC6EDFF),
            Color(0xFFAAD4F5),
            Color(0xFFAAD4F5),
            Color(0xFFAAD4F5),
            Color(0xFFC6EDFF),
            Color(0xFFC6EDFF),
            Color(0xFFF6FAFE),
        )
    ),
    val pickerWidth: Dp = 150.dp,
    val pickerHeight: Dp = 50.dp,
    val borderColor: Color = Color.Transparent,
    val borderThickness: Dp = 0.dp,
    val borderRadius: Dp = 0.dp,
    val containerColor: Color=Color.Transparent
)