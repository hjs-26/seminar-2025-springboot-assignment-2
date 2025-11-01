package com.wafflestudio.spring2025.course.dto

data class CoursePaging(
    val nextId: Long?,
    val hasNext: Boolean,
    val limit: Int = 20,
)
