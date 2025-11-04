package com.wafflestudio.spring2025.course.service

import com.wafflestudio.spring2025.common.enum.Semester
import com.wafflestudio.spring2025.course.CourseNotFoundException
import com.wafflestudio.spring2025.course.IllegalPeriodException
import com.wafflestudio.spring2025.course.dto.CourseSearchResponse
import com.wafflestudio.spring2025.course.dto.core.CourseDto
import com.wafflestudio.spring2025.course.repository.CourseRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import java.time.LocalDate

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
        val currentYear = LocalDate.now().year
        if (year !in 2013..currentYear) {
            throw IllegalPeriodException()
        }
        val queryLimit = limit + 1

        val courses =
            courseRepository.search(
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

    fun getById(id: Long): CourseDto {
        val course =
            courseRepository.findByIdOrNull(id)
                ?: throw CourseNotFoundException()
        return CourseDto(course)
    }
}
