package com.wafflestudio.spring2025.course.controller

import com.wafflestudio.spring2025.common.enum.Semester
import com.wafflestudio.spring2025.course.dto.CourseSearchResponse
import com.wafflestudio.spring2025.course.dto.core.CourseDto
import com.wafflestudio.spring2025.course.service.CourseService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/courses")
@Tag(name = "Course", description = "강의 관리 API")
class CourseController(
    private val courseService: CourseService,
) {
    @Operation(summary = "강의 검색", description = "연도, 학기, 키워드(강의명 또는 교수명)로 강의를 검색합니다")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "강의 검색 성공"),
            ApiResponse(responseCode = "400", description = "잘못된 요청 (유효하지 않은 연도 또는 학기)"),
        ],
    )
    @GetMapping
    fun search(
        @Parameter(description = "연도") @RequestParam year: Int,
        @Parameter(description = "학기", example = "FALL") @RequestParam semester: Semester,
        @Parameter(description = "검색 키워드") @RequestParam(required = false) keyword: String?,
        @Parameter(description = "다음 페이지 커서") @RequestParam(required = false) nextId: Long?,
        @Parameter(description = "페이지 크기") @RequestParam(defaultValue = "20") limit: Int,
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

    @Operation(summary = "강의 단건 조회", description = "강의 ID로 특정 강의의 상세 정보를 조회합니다")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "강의 조회 성공"),
            ApiResponse(responseCode = "400", description = "유효하지 않은 ID 형식"),
            ApiResponse(responseCode = "404", description = "강의를 찾을 수 없음"),
        ],
    )
    @GetMapping("/{id}")
    fun get(
        @Parameter(description = "강의 ID") @PathVariable id: Long,
    ): ResponseEntity<CourseDto> {
        val courseDto = courseService.getById(id)
        return ResponseEntity.ok(courseDto)
    }
}
