package com.wafflestudio.spring2025.course.controller

import com.wafflestudio.spring2025.course.dto.CourseSearchRequest
import com.wafflestudio.spring2025.course.dto.CourseSearchResponse
import com.wafflestudio.spring2025.course.service.CourseService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/courses")
class CourseController(
    private val courseService: CourseService,
) {
    @GetMapping
    fun search(
        @RequestBody searchRequest: CourseSearchRequest,
    ): ResponseEntity<CourseSearchResponse> {
        val coursePagingResponse =
            courseService.searchByYearSemesterKeyword(
                year = searchRequest.year,
                semester = searchRequest.semester,
                keyword = searchRequest.keyword,
                nextId = searchRequest.nextId,
                limit = searchRequest.limit,
            )
        return ResponseEntity.ok(coursePagingResponse)
    }
}
