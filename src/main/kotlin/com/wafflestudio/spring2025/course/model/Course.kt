package com.wafflestudio.spring2025.course.model

import com.wafflestudio.spring2025.common.enum.Semester
import com.wafflestudio.spring2025.course.crawling.ClassPlaceAndTime
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table

@Table("courses")
data class Course(
    @Id
    var id: Long? = null,
    var year: Int,
    var semester: Semester,
    var classification: String?,
    var college: String?,
    var department: String?,
    var academicCourse: String?,
    var academicYear: String?,
    var courseNumber: String,
    var lectureNumber: String,
    var courseTitle: String,
    var credit: Long,
    var instructor: String?,
    var classTimeJson: List<ClassPlaceAndTime>?
)