package com.wafflestudio.spring2025.timetable.dto.core

import com.wafflestudio.spring2025.common.enum.Semester
import com.wafflestudio.spring2025.timetable.model.Timetable

data class TimetableDto(
    val id: Long,
    val name: String,
    val year: Int,
    val semester: Semester,
) {
    constructor(timetable: Timetable) : this(
        id = timetable.id!!,
        name = timetable.name,
        year = timetable.year,
        semester = timetable.semester,
    )
}