package com.wafflestudio.spring2025.course.model

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.wafflestudio.spring2025.common.enum.Semester
import com.wafflestudio.spring2025.course.crawling.ClassPlaceAndTime
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Transient
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import kotlin.collections.emptyList

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
    @Column("class_time_json")
    var classTimeJsonString: String? = null,
) {
    companion object {
        private val objectMapper = ObjectMapper().findAndRegisterModules()
    }

    var classTimeJson: List<ClassPlaceAndTime>?
        @Transient
        get() =
            // 정보가 아예 없으면 null이 됨
            // 수업 시간 정보가 필요 없다는 정보가 있으면 빈 리스트가 됨
            classTimeJsonString?.let {
                if (it.isBlank()) {
                    emptyList()
                } else {
                    objectMapper.readValue(it)
                }
            }

        @Transient
        set(value) {
            classTimeJsonString = value?.let { objectMapper.writeValueAsString(it) }
        }

    // 생성자에서 classTimeJson을 받을 수 있도록 하는 헬퍼 생성자
    constructor(
        id: Long? = null,
        year: Int,
        semester: Semester,
        classification: String?,
        college: String?,
        department: String?,
        academicCourse: String?,
        academicYear: String?,
        courseNumber: String,
        lectureNumber: String,
        courseTitle: String,
        credit: Long,
        instructor: String?,
        classTimeJson: List<ClassPlaceAndTime>?,
    ) : this(
        id = id,
        year = year,
        semester = semester,
        classification = classification,
        college = college,
        department = department,
        academicCourse = academicCourse,
        academicYear = academicYear,
        courseNumber = courseNumber,
        lectureNumber = lectureNumber,
        courseTitle = courseTitle,
        credit = credit,
        instructor = instructor,
        classTimeJsonString = classTimeJson?.let { objectMapper.writeValueAsString(it) },
    )
}
