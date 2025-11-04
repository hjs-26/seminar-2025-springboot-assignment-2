package com.wafflestudio.spring2025.timetable.repository

import com.wafflestudio.spring2025.timetable.model.Enroll
import org.springframework.data.repository.ListCrudRepository

interface EnrollRepository : ListCrudRepository<Enroll, Long> {
    fun existsByTimetableIdAndCourseId(
        timetableId: Long,
        courseId: Long,
    ): Boolean

    fun deleteByTimetableIdAndCourseId(
        timetableId: Long,
        courseId: Long,
    ): Int
}
