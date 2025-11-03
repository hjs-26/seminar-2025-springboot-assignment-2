package com.wafflestudio.spring2025.course.controller

import com.wafflestudio.spring2025.common.enum.Semester
import com.wafflestudio.spring2025.course.dto.CourseSearchRequest
import com.wafflestudio.spring2025.course.dto.CourseSearchResponse
import com.wafflestudio.spring2025.course.service.CourseService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/course")
class CourseController(
    private val courseService: CourseService,
) {
    @GetMapping
    fun search(
        @RequestParam year: Int,
        @RequestParam semester: Semester,
        @RequestParam(required = false) keyword: String?,
        @RequestParam(required = false) nextId: Long?,
        @RequestParam(defaultValue = "20") limit: Int,
    ): ResponseEntity<CourseSearchResponse> {
        val coursePagingResponse =
            courseService.searchByYearSemesterKeyword(
                year = year,
                semester = semester,
                keyword = keyword,
                nextId = nextId,
                limit = limit,
            )
        return ResponseEntity.ok(coursePagingResponse)
    }


}
