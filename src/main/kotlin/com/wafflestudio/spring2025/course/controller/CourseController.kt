package com.wafflestudio.spring2025.course.controller

import com.wafflestudio.spring2025.course.dto.CreateCourseResponse
import com.wafflestudio.spring2025.course.dto.CreateCourseRequest
import com.wafflestudio.spring2025.course.service.CourseService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/boards")
class CourseController(
    private val courseService: CourseService,
) {
    @PostMapping
    fun create(
        @RequestBody createRequest: CreateCourseRequest,
    ): ResponseEntity<CreateCourseResponse> {
        val course = courseService.create(createRequest.name)
        return ResponseEntity.ok(course)
    }
}
