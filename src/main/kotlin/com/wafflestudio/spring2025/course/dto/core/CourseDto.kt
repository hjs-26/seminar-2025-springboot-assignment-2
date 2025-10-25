package com.wafflestudio.spring2025.course.dto.core

import com.wafflestudio.spring2025.course.model.Course

data class CourseDto(
    val id: Long,
    val name: String,
) {
    constructor (course: Course) : this(
        id = course.id!!,
        name = course.name,
    )
}
