package com.wafflestudio.spring2025.timetable.controller

import com.wafflestudio.spring2025.timetable.dto.request.CreateTimetableRequest
import com.wafflestudio.spring2025.timetable.dto.request.UpdateTimetableRequest
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

    @Operation(summary = "시간표 전체 조회", description = "유저가 생성한 모든 시간표를 조회합니다")
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

    @Operation(summary = "시간표 이름 변경", description = "유저가 생성한 시간표의 이름을 변경합니다")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "시간표 이름 변경 성공"),
            ApiResponse(responseCode = "400", description = "잘못된 요청 (빈 이름)"),
            ApiResponse(responseCode = "403", description = "다른 유저의 시간표의 이름을 변경 시도"),
            ApiResponse(responseCode = "404", description = "시간표를 찾지 못함"),
            ApiResponse(responseCode = "409", description = "중복된 시간표"),
        ],
    )
    @PatchMapping("/timetables/{timetableId}")
    fun update(
        @Parameter(hidden = true) @LoggedInUser user: User,
        @Parameter @PathVariable timetableId: Long,
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

    @Operation(summary = "시간표 삭제", description = "유저가 생성한 시간표를 삭제합니다")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "204", description = "시간표 삭제 성공"),
            ApiResponse(responseCode = "403", description = "다른 유저의 시간표를 삭제 시도"),
            ApiResponse(responseCode = "404", description = "시간표를 찾지 못함"),
        ],
    )
    @DeleteMapping("/timetables/{timetableId}")
    fun delete(
        @Parameter(hidden = true) @LoggedInUser user: User,
        @Parameter @PathVariable timetableId: Long,
    ): ResponseEntity<Unit> {
        timetableService.delete(
            user = user,
            timetableId = timetableId,
        )
        return ResponseEntity.noContent().build()
    }

    @Operation(summary = "시간표 상세 조회", description = "특정 시간표의 기본 정보와 포함된 모든 강의의 상세 정보 및 학점 수 합을 조회합니다")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "시간표 상세 조회 성공"),
            ApiResponse(responseCode = "404", description = "시간표를 찾지 못함")
        ]
    )
    @GetMapping("/timetables/{timetableId}")
    fun getDetail(
        @Parameter @PathVariable timetableId: Long,
    ): ResponseEntity<TimetableDetailResponse> {
        val timetableDetailResponse =
            timetableService.getDetail(
                timetableId = timetableId,
            )
        return ResponseEntity.ok(timetableDetailResponse)
    }
}
