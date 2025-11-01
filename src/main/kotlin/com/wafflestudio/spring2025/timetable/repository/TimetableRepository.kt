package com.wafflestudio.spring2025.timetable.repository

import com.wafflestudio.spring2025.timetable.model.Timetable
import org.springframework.data.repository.ListCrudRepository

interface TimetableRepository : ListCrudRepository<Timetable, Long> {
    fun findByUserId(userId: Long): List<Timetable>
    fun findByIdAndUserId(id: Long, userId: Long): Timetable?
}
