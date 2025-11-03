package com.wafflestudio.spring2025.course.dto

import com.wafflestudio.spring2025.course.dto.core.CourseDto

data class CourseSearchResponse(
    val data: List<CourseDto>,
    val nextId: Long?,
    val hasNext: Boolean,
)
