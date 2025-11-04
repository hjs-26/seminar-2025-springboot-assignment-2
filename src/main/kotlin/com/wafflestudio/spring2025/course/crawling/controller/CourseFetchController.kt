package com.wafflestudio.spring2025.course.crawling.controller

import com.wafflestudio.spring2025.common.enum.Semester
import com.wafflestudio.spring2025.course.crawling.service.CourseFetchService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import kotlinx.coroutines.runBlocking
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/courses")
@Tag(name = "Course Fetch", description = "강의 크롤링 API")
class CourseFetchController(
    private val courseFetchService: CourseFetchService,
) {
    @Operation(
        summary = "강의 정보 크롤링",
        description = """
            서울대 수강신청 사이트에서 강의 정보를 가져와 DB에 저장합니다.
            
            작업 시간: 약 5-10분 소요 (강의 수에 따라 다름)
            
            주의: 동일한 year/semester로 여러 번 실행 시 중복 데이터가 생길 수 있습니다.
        """,
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "크롤링 성공"),
            ApiResponse(responseCode = "500", description = "크롤링 실패 (네트워크 오류 등)"),
        ],
    )
    @PostMapping("/fetch")
    fun fetchCourses(
        @Parameter(description = "연도 (예: 2025)", example = "2025")
        @RequestParam year: Int,
        @Parameter(description = "학기 (SPRING, SUMMER, FALL, WINTER)", example = "SUMMER")
        @RequestParam semester: Semester,
    ): ResponseEntity<Map<String, Any>> =
        runBlocking {
            val count = courseFetchService.fetchAndSaveCourses(year, semester)
            ResponseEntity.ok(
                mapOf(
                    "message" to "Successfully fetched courses",
                    "year" to year,
                    "semester" to semester,
                    "count" to count,
                ),
            )
        }
}
