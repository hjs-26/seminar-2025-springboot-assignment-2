package com.wafflestudio.spring2025.course.crawling.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.wafflestudio.spring2025.common.enum.Semester
import com.wafflestudio.spring2025.course.crawling.repository.CourseFetchRepository
import com.wafflestudio.spring2025.course.crawling.utils.CourseClassTimeUtils
import com.wafflestudio.spring2025.course.model.Course
import com.wafflestudio.spring2025.course.repository.CourseRepository
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.apache.poi.ss.usermodel.Cell
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

interface CourseFetchService {
    suspend fun fetchAndSaveCourses(
        year: Int,
        semester: Semester,
    ): Int
}

@Service
class CourseFetchServiceImpl(
    private val courseRepository: CourseRepository,
    private val courseFetchRepository: CourseFetchRepository,
    private val objectMapper: ObjectMapper,
) : CourseFetchService {
    private val log = LoggerFactory.getLogger(javaClass)

    override suspend fun fetchAndSaveCourses(
        year: Int,
        semester: Semester,
    ): Int {
            val courses = getCourses(year, semester)

            courseRepository.saveAll(courses)
            return courses.size
    }

    private suspend fun getCourses(
        year: Int,
        semester: Semester,
    ): List<Course> {
            val koreanLectureXlsx = courseFetchRepository.downloadCoursesExcel(year, semester, "ko")
            val englishLectureXlsx = courseFetchRepository.downloadCoursesExcel(year, semester, "en")

            val koreanSheet = HSSFWorkbook(koreanLectureXlsx.asInputStream()).getSheetAt(0)
            val englishSheet = HSSFWorkbook(englishLectureXlsx.asInputStream()).getSheetAt(0)
            val fullSheet =
                koreanSheet.zip(englishSheet).map { (koreanRow, englishRow) ->
                    koreanRow + englishRow
                }

            val columnNameIndex = fullSheet[2].associate { it.stringCellValue to it.columnIndex }

            return fullSheet
                .drop(3)
                .map { row ->
                    convertRowToCourse(row, columnNameIndex, year, semester)
                }.also {
                    koreanLectureXlsx.release()
                    englishLectureXlsx.release()
                }.mapIndexed { index, course ->
                    // 4. 추가 정보 API 호출하여 보완 (진행상황 로깅)
                    if (index % 100 == 0) {
                    }
                    enrichCourseWithApiData(course, year, semester)
                }
    }

    private fun convertRowToCourse(
        row: List<Cell>,
        columnNameIndex: Map<String, Int>,
        year: Int,
        semester: Semester,
    ): Course {
        fun List<Cell>.getCellByColumnName(key: String): String =
            this
                .getOrNull(
                    columnNameIndex.getOrElse(key) {
                        this.size
                    },
                )?.stringCellValue ?: ""

        val classification = row.getCellByColumnName("교과구분")
        val college = row.getCellByColumnName("개설대학")
        val department = row.getCellByColumnName("개설학과")
        val academicCourse = row.getCellByColumnName("이수과정")
        val academicYear = row.getCellByColumnName("학년")
        val courseNumber = row.getCellByColumnName("교과목번호")
        val lectureNumber = row.getCellByColumnName("강좌번호")
        val courseTitle = row.getCellByColumnName("교과목명")
        val courseSubtitle = row.getCellByColumnName("부제명")
        val credit = row.getCellByColumnName("학점").toLongOrNull() ?: 0L
        val classTimeText = row.getCellByColumnName("수업교시")
        val location = row.getCellByColumnName("강의실(동-호)(#연건, *평창)")
        val instructor = row.getCellByColumnName("주담당교수")

        val classTimes =
            CourseClassTimeUtils.convertTextToClassTimeObject(
                classTimeText.split("/"),
                location.split("/"),
            )

        val courseFullTitle = if (courseSubtitle.isEmpty()) courseTitle else "$courseTitle ($courseSubtitle)"

        return Course(
            year = year,
            semester = semester,
            classification = classification,
            college = college,
            department = department.replace("null", "").ifEmpty { college },
            academicCourse = academicCourse,
            academicYear = if (academicCourse != "학사") academicCourse else academicYear,
            courseNumber = courseNumber,
            lectureNumber = lectureNumber,
            courseTitle = courseFullTitle,
            credit = credit,
            instructor = instructor,
            classTimeJson = classTimes,
        )
    }

    private suspend fun enrichCourseWithApiData(
        course: Course,
        year: Int,
        semester: Semester,
    ): Course =
        try {
            val extraInfo =
                courseFetchRepository.getLectureInfo(
                    year,
                    semester,
                    course.courseNumber,
                    course.lectureNumber,
                )

            // 추가 정보로 Course 보완
            val extraCourseTitle =
                if (extraInfo.subInfo.courseSubName.isNullOrEmpty()) {
                    extraInfo.subInfo.courseName
                } else {
                    "${extraInfo.subInfo.courseName} (${extraInfo.subInfo.courseSubName})"
                }

            val extraDepartment =
                if (extraInfo.subInfo.departmentKorNm != null && extraInfo.subInfo.majorKorNm != null) {
                    "${extraInfo.subInfo.departmentKorNm}(${extraInfo.subInfo.majorKorNm})"
                } else {
                    null
                }

            val extraClassTimes =
                CourseClassTimeUtils.convertTextToClassTimeObject(
                    extraInfo.ltTime,
                    extraInfo.ltRoom.map { it.replace("(무선랜제공)", "") },
                )

            val updatedClassTimeJson =
                if (extraClassTimes.isNotEmpty()) {
                    objectMapper.writeValueAsString(extraClassTimes)
                } else {
                    course.classTimeJsonString
                }

            course.copy(
                courseTitle = extraCourseTitle ?: course.courseTitle,
                instructor = extraInfo.subInfo.professorName?.substringBeforeLast(" (") ?: course.instructor,
                department = extraDepartment ?: course.department,
                academicYear =
                    extraInfo.subInfo.academicCourse?.takeIf { it != "학사" }
                        ?: extraInfo.subInfo.academicYear?.let { "${it}학년" } ?: course.academicYear,
                classTimeJsonString = updatedClassTimeJson,
            )
        } catch (e: Exception) {
            log.warn("Failed to enrich course ${course.courseNumber}-${course.lectureNumber}: ${e.message}")
            course
        }
}
