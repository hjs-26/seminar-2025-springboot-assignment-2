package com.wafflestudio.spring2025.course.service

import com.wafflestudio.spring2025.common.enum.Semester
import com.wafflestudio.spring2025.course.IllegalPeriodException
import com.wafflestudio.spring2025.course.dto.CourseSearchResponse
import com.wafflestudio.spring2025.course.dto.core.CourseDto
import com.wafflestudio.spring2025.course.repository.CourseRepository
import org.springframework.stereotype.Service

@Service
class CourseService(
    private val courseRepository: CourseRepository,
) {
    fun searchByYearSemesterKeyword(
        year: Int,
        semester: Semester,
        keyword: String?,
        nextId: Long?,
        limit: Int,
    ): CourseSearchResponse {

        if (year != 2025 || semester != Semester.SUMMER) {
            throw IllegalPeriodException()
        }
        val queryLimit = limit + 1

        val courses = courseRepository.search(
            year = year,
            semester = semester,
            keyword = keyword,
            nextId = nextId,
            limit = queryLimit,
        )

        val hasNext = courses.size > limit
        val coursesToReturn = if (hasNext) courses.take(limit) else courses
        val nextId = coursesToReturn.lastOrNull()?.id

        return CourseSearchResponse(
            coursesToReturn.map { CourseDto(it) },
            nextId = nextId,
            hasNext = hasNext,
        )
    }

}