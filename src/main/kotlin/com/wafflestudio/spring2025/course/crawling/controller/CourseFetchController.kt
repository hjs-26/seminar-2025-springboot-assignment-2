package com.wafflestudio.spring2025.course.crawling.controller

import com.wafflestudio.spring2025.common.enum.Semester
import com.wafflestudio.spring2025.course.crawling.service.CourseFetchService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.constraints.Pattern
import kotlinx.coroutines.runBlocking
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/courses")
@Tag(name = "Course Fetch", description = "강의 크롤링 API")
@Validated
class CourseFetchController(
    private val courseFetchService: CourseFetchService,
) {
    @Operation(
        summary = "강의 정보 크롤링",
        description = """
            서울대 수강신청 사이트에서 강의 정보를 가져와 DB에 저장합니다.

            semester 형식: YYYY-1 (SPRING), YYYY-2 (SUMMER), YYYY-3 (FALL), YYYY-4 (WINTER)
            예: 2025-1, 2025-2, 2025-3, 2025-4

            작업 시간: 약 5-10분 소요 (강의 수에 따라 다름)

            주의: 동일한 year/semester로 여러 번 실행 시 중복 데이터가 생길 수 있습니다.
        """,
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "크롤링 성공"),
            ApiResponse(responseCode = "400", description = "잘못된 semester 형식"),
            ApiResponse(responseCode = "500", description = "크롤링 실패 (네트워크 오류 등)"),
        ],
    )
    @PostMapping("/fetch")
    fun fetchCourses(
        @Parameter(description = "학기 (YYYY-1~4 형식)", example = "2025-1")
        @RequestParam(defaultValue = "2025-1")
        @Pattern(regexp = "^\\d{4}-[1-4]$", message = "semester must be YYYY-1, YYYY-2, YYYY-3, or YYYY-4 format")
        semester: String,
    ): ResponseEntity<Map<String, Any>> =
        runBlocking {
            val parsedSemester =
                parseSemester(semester) ?: return@runBlocking ResponseEntity.badRequest().body(
                    mapOf(
                        "error" to "Invalid semester format",
                        "message" to "semester must be YYYY-1, YYYY-2, YYYY-3, or YYYY-4 format",
                    ),
                )

            val (year, semesterEnum) = parsedSemester
            val count = courseFetchService.fetchAndSaveCourses(year, semesterEnum)
            ResponseEntity.ok(
                mapOf(
                    "message" to "Successfully fetched courses",
                    "semester" to semester,
                    "year" to year,
                    "semesterValue" to semesterEnum,
                    "count" to count,
                ),
            )
        }

    private fun parseSemester(semester: String): Pair<Int, Semester>? {
        return try {
            val (year, sem) = semester.split("-")
            val semesterEnum =
                when (sem) {
                    "1" -> Semester.SPRING
                    "2" -> Semester.SUMMER
                    "3" -> Semester.FALL
                    "4" -> Semester.WINTER
                    else -> return null
                }
            year.toInt() to semesterEnum
        } catch (e: Exception) {
            null
        }
    }
}
