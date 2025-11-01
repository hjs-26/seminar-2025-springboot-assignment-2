package com.wafflestudio.spring2025.timetable.controller

import com.wafflestudio.spring2025.timetable.dto.request.CreateTimetableRequest
import com.wafflestudio.spring2025.timetable.dto.response.CreateTimetableResponse
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
            ApiResponse(responseCode = "409", description = "중복된 시간표")
        ]
    )
    @PostMapping("/timetables")
    fun create(
        @Parameter(hidden = true) @LoggedInUser user: User,
        @RequestBody createRequest: CreateTimetableRequest,
    ): ResponseEntity<CreateTimetableResponse> {
        val timetableDto =
            timetableService.create(
                user = user,
                semester = createRequest.semester,
                name = createRequest.name,
                year = createRequest.year,
            )
        return ResponseEntity.status(HttpStatus.CREATED).body(timetableDto)
    }

}
