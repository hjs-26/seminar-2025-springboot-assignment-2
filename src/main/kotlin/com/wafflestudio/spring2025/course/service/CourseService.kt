package com.wafflestudio.spring2025.course.service

import com.wafflestudio.spring2025.board.BoardNameBlankException
import com.wafflestudio.spring2025.board.BoardNameConflictException
import com.wafflestudio.spring2025.course.dto.core.CourseDto
import com.wafflestudio.spring2025.course.model.Course
import com.wafflestudio.spring2025.course.repository.CourseRepository
import org.springframework.stereotype.Service

@Service
class CourseService(
    private val courseRepository: CourseRepository,
)

