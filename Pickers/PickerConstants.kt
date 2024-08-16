package com.clappy.caloriesteptracker.ui.customUI.Pickers

import com.clappy.caloriesteptracker.R
import java.time.LocalTime

object PickerConstants {
    // TimePicker Defaults
    val DEFAULT_WAKEUP_TIME = LocalTime.of(6, 0)
    val DEFAULT_BEDTIME = LocalTime.of(23, 0)

    // AgePicker Defaults
    val AGE_RANGE = 12..119
    val DEFAULT_AGE = 16

    // GenderPicker Options
    val GENDER_OPTIONS = listOf(R.string.gender_male, R.string.gender_female)

    // ActivityLevelPicker Options
    val ACTIVITY_LEVEL_LOW = R.string.low
    val ACTIVITY_LEVEL_MODERATE = R.string.moderate
    val ACTIVITY_LEVEL_HIGH = R.string.high
    val ACTIVITY_LEVELS = listOf(ACTIVITY_LEVEL_LOW, ACTIVITY_LEVEL_MODERATE, ACTIVITY_LEVEL_HIGH)

    // WeightPicker Defaults
    val WEIGHT_RANGE = 1..500
    val DEFAULT_WEIGHT = 80
    val WEIGHT_UNITS = listOf(R.string.kg, R.string.lbs)
}