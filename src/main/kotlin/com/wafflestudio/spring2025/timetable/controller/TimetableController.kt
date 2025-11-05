package com.wafflestudio.spring2025.timetable.controller

import com.wafflestudio.spring2025.timetable.dto.request.CreateTimetableRequest
import com.wafflestudio.spring2025.timetable.dto.request.UpdateTimetableRequest
import com.wafflestudio.spring2025.timetable.dto.response.AddCourseResponse
import com.wafflestudio.spring2025.timetable.dto.response.TimetableDetailResponse
import com.wafflestudio.spring2025.timetable.dto.response.TimetableListResponse
import com.wafflestudio.spring2025.timetable.dto.response.TimetableResponse
import com.wafflestudio.spring2025.timetable.service.TimetableService
import com.wafflestudio.spring2025.user.LoggedInUser
import com.wafflestudio.spring2025.user.model.User
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Timetable", description = "시간표 관리 API")
class TimetableController(
    private val timetableService: TimetableService,
) {
    @Operation(summary = "시간표 생성", description = "새로운 시간표를 생성합니다")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "201", description = "시간표 생성 성공"),
            ApiResponse(responseCode = "400", description = "잘못된 요청 (빈 이름 또는 잘못된 연도)"),
            ApiResponse(responseCode = "409", description = "중복된 시간표"),
        ],
    )
    @PostMapping("/timetables")
    fun create(
        @Parameter(hidden = true) @LoggedInUser user: User,
        @RequestBody createRequest: CreateTimetableRequest,
    ): ResponseEntity<TimetableResponse> {
        val timetableDto =
            timetableService.create(
                user = user,
                semester = createRequest.semester,
                name = createRequest.name,
                year = createRequest.year,
            )
        return ResponseEntity.status(HttpStatus.CREATED).body(timetableDto)
    }

    @Operation(
        summary = "시간표 전체 조회",
        description = """
            로그인한 사용자가 생성한 모든 시간표를 조회합니다.
            
            **응답 정보:**
            - 시간표 기본 정보만 포함 (ID, 이름, 연도, 학기)
            - 강의 목록은 포함되지 않음 (상세 조회 API 사용)
        """,
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "시간표 전체 조회 성공"),
        ],
    )
    @GetMapping("/timetables")
    fun getAll(
        @Parameter(hidden = true) @LoggedInUser user: User,
    ): ResponseEntity<TimetableListResponse> {
        val timetableDtoList =
            timetableService.getAll(
                user = user,
            )
        return ResponseEntity.ok(timetableDtoList)
    }

    @Operation(summary = "시간표 이름 변경", description = "사용자가 생성한 시간표의 이름을 변경합니다")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "시간표 이름 변경 성공"),
            ApiResponse(responseCode = "400", description = "잘못된 요청 (빈 이름)"),
            ApiResponse(responseCode = "403", description = "다른 사용자의 시간표 수정 시도"),
            ApiResponse(responseCode = "404", description = "시간표를 찾지 못함"),
            ApiResponse(responseCode = "409", description = "중복된 시간표 이름"),
        ],
    )
    @PatchMapping("/timetables/{timetableId}")
    fun update(
        @Parameter(hidden = true) @LoggedInUser user: User,
        @Parameter(
            description = "시간표 ID",
            example = "42",
        ) @PathVariable timetableId: Long,
        @RequestBody updateRequest: UpdateTimetableRequest,
    ): ResponseEntity<TimetableResponse> {
        val timetableDto =
            timetableService.update(
                user = user,
                timetableId = timetableId,
                name = updateRequest.name,
            )
        return ResponseEntity.ok(timetableDto)
    }

    @Operation(
        summary = "시간표 삭제",
        description = """
            사용자가 생성한 시간표를 삭제합니다.
            
            **연쇄 삭제:**
            - 시간표 삭제 시 해당 시간표에 추가된 모든 강의 정보도 함께 삭제됩니다.
            - 강의 자체는 삭제되지 않고, 시간표-강의 연결(Enroll)만 삭제됩니다.
        """,
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "204", description = "시간표 삭제 성공"),
            ApiResponse(responseCode = "403", description = "다른 사용자의 시간표 삭제 시도"),
            ApiResponse(responseCode = "404", description = "시간표를 찾지 못함"),
        ],
    )
    @DeleteMapping("/timetables/{timetableId}")
    fun delete(
        @Parameter(hidden = true) @LoggedInUser user: User,
        @Parameter(
            description = "시간표 ID",
            example = "42",
        ) @PathVariable timetableId: Long,
    ): ResponseEntity<Unit> {
        timetableService.delete(
            user = user,
            timetableId = timetableId,
        )
        return ResponseEntity.noContent().build()
    }

    @Operation(
        summary = "시간표 상세 조회",
        description = """
            특정 시간표의 기본 정보와 포함된 모든 강의의 상세 정보를 조회합니다.
            
            **응답 정보:**
            - 시간표 기본 정보 (ID, 이름, 연도, 학기)
            - 포함된 모든 강의의 상세 정보
            - 총 학점 수
        """,
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "시간표 상세 조회 성공"),
            ApiResponse(responseCode = "404", description = "시간표를 찾지 못함"),
        ],
    )
    @GetMapping("/timetables/{timetableId}")
    fun getDetail(
        @Parameter(hidden = true) @LoggedInUser user: User,
        @Parameter(
            description = "시간표 ID",
            example = "42",
        ) @PathVariable timetableId: Long,
    ): ResponseEntity<TimetableDetailResponse> {
        val timetableDetailResponse =
            timetableService.getDetail(
                timetableId = timetableId,
            )
        return ResponseEntity.ok(timetableDetailResponse)
    }

    @Operation(
        summary = "시간표에 강의 추가",
        description = """
            사용자가 생성한 시간표에 강의를 추가합니다.
            
            **검증 사항:**
            1. 시간표와 강의의 연도/학기 일치 여부
            2. 시간표에 이미 추가된 강의인지 확인 (중복 방지)
            3. 기존 강의와 시간 중복 여부
            4. 강의 시간 정보 존재 여부
        """,
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "시간표에 강의 추가 성공"),
            ApiResponse(responseCode = "400", description = "시간표와 강의의 년도/학기 불일치"),
            ApiResponse(responseCode = "403", description = "다른 사용자의 시간표에 강의 추가 시도"),
            ApiResponse(responseCode = "404", description = "시간표 또는 강의를 찾지 못함"),
            ApiResponse(responseCode = "409", description = "이미 추가된 강의이거나 기존 강의와 시간 중복"),
            ApiResponse(responseCode = "503", description = "강의 시간 정보가 아직 등록되지 않음"),
        ],
    )
    @PostMapping("/timetables/{timetableId}/courses/{courseId}")
    fun addCourse(
        @Parameter(hidden = true) @LoggedInUser user: User,
        @Parameter(
            description = "시간표 ID",
            example = "42",
        ) @PathVariable timetableId: Long,
        @Parameter(
            description = "강의 ID",
            example = "1234",
        ) @PathVariable courseId: Long,
    ): ResponseEntity<AddCourseResponse> {
        val addCourseResponse =
            timetableService.addCourse(
                user = user,
                timetableId = timetableId,
                courseId = courseId,
            )
        return ResponseEntity.ok(addCourseResponse)
    }

    @Operation(
        summary = "시간표에서 강의 삭제",
        description = """
            사용자가 생성한 시간표에서 추가된 강의를 삭제합니다.
            
            **주의사항:**
            - 강의 자체는 삭제되지 않고, 시간표-강의 연결만 제거됩니다.
        """,
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "204", description = "시간표에서 강의 삭제 성공"),
            ApiResponse(responseCode = "400", description = "시간표에 해당 강의가 없음"),
            ApiResponse(responseCode = "403", description = "다른 사용자의 시간표에서 강의 삭제 시도"),
            ApiResponse(responseCode = "404", description = "시간표를 찾지 못함"),
        ],
    )
    @DeleteMapping("/timetables/{timetableId}/courses/{courseId}")
    fun deleteCourse(
        @Parameter(hidden = true) @LoggedInUser user: User,
        @Parameter(
            description = "시간표 ID",
            example = "42",
        ) @PathVariable timetableId: Long,
        @Parameter(
            description = "강의 ID",
            example = "1234",
        ) @PathVariable courseId: Long,
    ): ResponseEntity<Unit> {
        timetableService.deleteCourse(
            user = user,
            timetableId = timetableId,
            courseId = courseId,
        )
        return ResponseEntity.noContent().build()
    }
}
