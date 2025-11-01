package com.wafflestudio.spring2025.timetable.dto.core

import com.wafflestudio.spring2025.common.enum.Semester
import com.wafflestudio.spring2025.timetable.model.Timetable
import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "시간표 정보")
data class TimetableDto(
    @Schema(description = "시간표 ID", example = "1")
    val id: Long,
    @Schema(description = "시간표 이름", example = "2025-2 시간표")
    val name: String,
    @Schema(description = "연도", example = "2025")
    val year: Int,
    @Schema(description = "학기", example = "FALL")
    val semester: Semester,
) {
    constructor(timetable: Timetable) : this(
        id = timetable.id!!,
        name = timetable.name,
        year = timetable.year,
        semester = timetable.semester,
    )
}
