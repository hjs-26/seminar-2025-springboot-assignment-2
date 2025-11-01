package com.wafflestudio.spring2025.course.model

data class ClassPlaceAndTime(
    val day: DayOfWeek,
    val place: String,
    val startMinute: Int,
    val endMinute: Int,
)