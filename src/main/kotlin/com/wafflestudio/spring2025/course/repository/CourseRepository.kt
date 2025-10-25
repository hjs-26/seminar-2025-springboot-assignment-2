package com.wafflestudio.spring2025.course.repository

import com.wafflestudio.spring2025.course.model.Course
import org.springframework.data.repository.ListCrudRepository

interface CourseRepository : ListCrudRepository<Course, Long> {
    fun existsByName(name: String): Boolean
}
