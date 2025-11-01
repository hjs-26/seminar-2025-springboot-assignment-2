package com.wafflestudio.spring2025.course.dto

import com.wafflestudio.spring2025.common.enum.Semester

data class CourseSearchRequest(
    val year: Int,
    val semester: Semester,
    val keyword: String?,
    val nextId: Int?,
    val limit: Int = 20,
)
