package com.wafflestudio.spring2025.timetable.repository

import com.wafflestudio.spring2025.timetable.model.TimetableCourse
import org.springframework.data.repository.ListCrudRepository

interface TimetableCourseRepository : ListCrudRepository<TimetableCourse, Long> {
    fun findByTimetableId(timetableId: Long): List<TimetableCourse>
    fun deleteByTimetableId(timetableId: Long)
    fun deleteByTimetableIdAndCourseId(timetableId: Long, courseId: Long)
    fun existsByTimetableIdAndCourseId(timetableId: Long, courseId: Long): Boolean
}
