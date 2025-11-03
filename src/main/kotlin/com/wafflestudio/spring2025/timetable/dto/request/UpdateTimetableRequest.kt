package com.wafflestudio.spring2025.timetable.dto.request

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "시간표 이름 변경 요청")
data class UpdateTimetableRequest(
    @Schema(description = "시간표 이름", example = "2025-2 시간표", required = true)
    val name: String,
)
