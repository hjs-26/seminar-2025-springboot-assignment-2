package com.wafflestudio.spring2025.course.dto.core

import com.wafflestudio.spring2025.course.model.Course

data class CourseDto(
    val id: String,
    val name: String,
) {
    constructor (course: Course) : this(
        id = course.courseNumber,
        name = course.courseTitle,
    )
}
