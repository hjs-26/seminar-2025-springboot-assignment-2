package com.wafflestudio.spring2025.timetable.dto.request

import com.wafflestudio.spring2025.common.enum.Semester

data class CreateTimetableRequest (
    val name: String,
    val year: Int,
    var semester: Semester,
)