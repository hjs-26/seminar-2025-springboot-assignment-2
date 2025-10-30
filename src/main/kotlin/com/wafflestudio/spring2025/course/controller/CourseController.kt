package com.wafflestudio.spring2025.course.controller

import com.wafflestudio.spring2025.course.dto.CreateCourseResponse
import com.wafflestudio.spring2025.course.dto.CreateCourseRequest
import com.wafflestudio.spring2025.course.service.CourseFetchService
import com.wafflestudio.spring2025.course.service.CourseService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/courses")
class CourseController(
    private val courseService: CourseService,
    private val courseFetchService: CourseFetchService,
) {

    @GetMapping("/fetch")
    suspend fun fetchCourses(
        @RequestParam(defaultValue = "2025-2") semester: String
    ): ResponseEntity<Map<String, Any>> {
        val courses = courseFetchService.getCourses()
        return ResponseEntity.ok(mapOf("semester" to semester, "fetched" to courses.size))
    }

}
