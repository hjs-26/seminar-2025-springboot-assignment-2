package com.wafflestudio.spring2025.timetable.model

import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.time.Instant

@Table("enrolls")
data class Enroll(
    @Id var id: Long? = null,
    var timetableId: Long,
    var courseId: Long,
    @CreatedDate
    var createdAt: Instant? = null,
)
