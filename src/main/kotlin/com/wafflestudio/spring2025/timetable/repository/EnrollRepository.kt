package com.wafflestudio.spring2025.timetable.repository

import com.wafflestudio.spring2025.timetable.model.Enroll
import org.springframework.data.repository.ListCrudRepository

interface EnrollRepository : ListCrudRepository<Enroll, Long> {
    fun findByTimetableId(timetableId: Long): List<Enroll>
    fun deleteByTimetableId(timetableId: Long)
    fun deleteByTimetableIdAndCourseId(timetableId: Long, courseId: Long)
    fun existsByTimetableIdAndCourseId(timetableId: Long, courseId: Long): Boolean
}
