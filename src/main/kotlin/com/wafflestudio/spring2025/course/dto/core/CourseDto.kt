package com.wafflestudio.spring2025.course.dto.core

import com.wafflestudio.spring2025.common.enum.Semester
import com.wafflestudio.spring2025.course.crawling.ClassPlaceAndTime
import com.wafflestudio.spring2025.course.model.Course

data class CourseDto(
    var id: Long,
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
    var classTimeJson: List<ClassPlaceAndTime>?,
) {
    constructor(course: Course): this(
        id = course.id!!,
        year = course.year,
        semester = course.semester,
        classification = course.classification,
        college = course.college,
        department = course.department,
        academicCourse = course.academicCourse,
        academicYear = course.academicYear,
        courseNumber = course.courseNumber,
        lectureNumber = course.lectureNumber,
        courseTitle = course.courseTitle,
        credit = course.credit,
        instructor = course.instructor,
        classTimeJson = course.classTimeJson,
    )
}