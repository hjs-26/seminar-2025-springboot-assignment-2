package com.wafflestudio.spring2025.course.service

import com.wafflestudio.spring2025.course.model.Course
import com.wafflestudio.spring2025.course.repository.CourseRepository
import com.wafflestudio.spring2025.course.utils.CourseClassTimeUtils
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.apache.poi.ss.usermodel.Cell
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

interface CourseFetchService {
    suspend fun getCourses(
    ): List<Course>
}

@Service
class CourseFetchServiceImpl(
    private val CourseRepository: CourseRepository,
) : CourseFetchService {
    private val log = LoggerFactory.getLogger(javaClass)
    private val quotaRegex = """(?<quota>\d+)(\s*\((?<quotaForCurrentStudent>\d+)\))?""".toRegex()

    override suspend fun getCourses(
    ): List<Course> {
        val koreanLectureXlsx = CourseRepository.getCourses("ko")
        val englishLectureXlsx = CourseRepository.getCourses("en")
        val koreanSheet = HSSFWorkbook(koreanLectureXlsx.asInputStream()).getSheetAt(0)
        val englishSheet = HSSFWorkbook(englishLectureXlsx.asInputStream()).getSheetAt(0)
        val fullSheet = koreanSheet.zip(englishSheet).map { (koreanRow, englishRow) -> koreanRow + englishRow }
        val columnNameIndex = fullSheet[2].associate { it.stringCellValue to it.columnIndex }
        return fullSheet
            .drop(3)
            .map { row ->
                convertCourseRowToLecture(row, columnNameIndex)
            }.also {
                koreanLectureXlsx.release()
            }.map { lecture ->
                lecture
            }
    }

    private fun convertCourseRowToLecture(
        row: List<Cell>,
        columnNameIndex: Map<String, Int>,
    ): Course {
        fun List<Cell>.getCellByColumnName(key: String): String =
            this[
                columnNameIndex.getOrElse(key) {
                    log.error("$key 와 매칭되는 excel 컬럼이 존재하지 않습니다.")
                    this.size
                },
            ].stringCellValue

        val classification = row.getCellByColumnName("교과구분")
        val college = row.getCellByColumnName("개설대학")
        val department = row.getCellByColumnName("개설학과")
        val academicCourse = row.getCellByColumnName("이수과정")
        val academicYear = row.getCellByColumnName("학년")
        val courseNumber = row.getCellByColumnName("교과목번호")
        val lectureNumber = row.getCellByColumnName("강좌번호")
        val courseTitle = row.getCellByColumnName("교과목명")
        val courseSubtitle = row.getCellByColumnName("부제명")
        val credit = row.getCellByColumnName("학점").toLong()
        val classTimeText = row.getCellByColumnName("수업교시")
        val location = row.getCellByColumnName("강의실(동-호)(#연건, *평창)")
        val instructor = row.getCellByColumnName("주담당교수")

        val classTimes =
            CourseClassTimeUtils.convertTextToClassTimeObject(classTimeText.split("/"), location.split("/"))

        val courseFullTitle = if (courseSubtitle.isEmpty()) courseTitle else "$courseTitle ($courseSubtitle)"

        return Course(
            classification = classification,
            department = department.replace("null", "").ifEmpty { college },
            academicYear = academicCourse.takeIf { academicCourse != "학사" } ?: academicYear,
            courseNumber = courseNumber,
            lectureNumber = lectureNumber,
            courseTitle = courseFullTitle,
            credit = credit,
            instructor = instructor,
            category = "",
            classPlaceAndTimes = classTimes,
        )
    }
}