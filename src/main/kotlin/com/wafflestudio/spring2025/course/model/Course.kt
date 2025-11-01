package com.wafflestudio.spring2025.course.model

import com.fasterxml.jackson.annotation.JsonProperty
import com.wafflestudio.spring2025.course.crawling.ClassPlaceAndTime
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.CompoundIndex
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.Field

@Document("courses")
@CompoundIndex(def = "{ 'year': 1, 'semester': 1 }")
@CompoundIndex(def = "{ 'course_number': 1, 'lecture_number': 1 }")
data class Course(
    @Id
    @JsonProperty("_id")
    var id: String? = null,
    var classification: String,
    var college: String,
    var department: String?,
    @Field("course")
    var academicCourse: String?,
    @Field("academic_year")
    var academicYear: String?,
    @Field("course_number")
    var courseNumber: String,
    @Field("lecture_number")
    var lectureNumber: String,
    @Field("course_title")
    var courseTitle: String,
    var credit: Long,
    @Field("class_time_json")
    var classPlaceAndTimes: List<ClassPlaceAndTime>,
    var instructor: String?,
)