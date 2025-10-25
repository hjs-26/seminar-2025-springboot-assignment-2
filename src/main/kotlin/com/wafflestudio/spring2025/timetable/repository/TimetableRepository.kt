package com.wafflestudio.spring2025.timetable.repository

import com.wafflestudio.spring2025.board.model.Board
import org.springframework.data.repository.ListCrudRepository

interface TimetableRepository : ListCrudRepository<Board, Long> {
    fun existsByName(name: String): Boolean
}
