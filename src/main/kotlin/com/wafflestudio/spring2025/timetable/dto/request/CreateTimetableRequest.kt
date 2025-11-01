package com.wafflestudio.spring2025.timetable.dto.request

import com.wafflestudio.spring2025.common.enum.Semester
import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "시간표 생성 요청")
data class CreateTimetableRequest(
    @Schema(description = "시간표 이름", example = "2025-2 시간표", required = true)
    val name: String,
    @Schema(description = "연도", example = "2025", required = true)
    val year: Int,
    @Schema(description = "학기 (SPRING, SUMMER, FALL, WINTER)", example = "FALL", required = true)
    var semester: Semester,
)
