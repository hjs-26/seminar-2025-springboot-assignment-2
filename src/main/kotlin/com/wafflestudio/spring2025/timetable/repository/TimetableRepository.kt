package com.wafflestudio.spring2025.timetable.repository

import com.wafflestudio.spring2025.common.enum.Semester
import com.wafflestudio.spring2025.timetable.model.Timetable
import org.springframework.data.repository.ListCrudRepository

interface TimetableRepository : ListCrudRepository<Timetable, Long> {
    fun findByUserId(userId: Long): List<Timetable>

    fun existsByUserIdAndYearAndSemesterAndName(
        userId: Long,
        year: Int,
        semester: Semester,
        name: String,
    ): Boolean
}
