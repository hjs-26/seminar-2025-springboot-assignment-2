package com.wafflestudio.spring2025.timetable.dto.response

import com.wafflestudio.spring2025.course.dto.core.CourseDto
import com.wafflestudio.spring2025.timetable.dto.core.TimetableDto

data class TimetableDetailResponse (
    var timetable: TimetableDto,
    var courses: List<CourseDto>,
    var credits: Long,
)