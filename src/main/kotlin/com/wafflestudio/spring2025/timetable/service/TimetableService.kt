package com.wafflestudio.spring2025.timetable.service

import com.wafflestudio.spring2025.common.enum.Semester
import com.wafflestudio.spring2025.course.ClassTimeCrawlingException
import com.wafflestudio.spring2025.course.CourseNotFoundException
import com.wafflestudio.spring2025.course.dto.core.CourseDto
import com.wafflestudio.spring2025.course.model.Course
import com.wafflestudio.spring2025.course.repository.CourseRepository
import com.wafflestudio.spring2025.course.crawling.ClassPlaceAndTime
import com.wafflestudio.spring2025.timetable.CourseDuplicateException
import com.wafflestudio.spring2025.timetable.CourseNotExistsInTimetableException
import com.wafflestudio.spring2025.timetable.CourseOverlapException
import com.wafflestudio.spring2025.timetable.CourseTimetableNotMatchException
import com.wafflestudio.spring2025.timetable.TimetableDuplicateException
import com.wafflestudio.spring2025.timetable.TimetableInvalidYearException
import com.wafflestudio.spring2025.timetable.TimetableModifyForbiddenException
import com.wafflestudio.spring2025.timetable.TimetableNameBlankException
import com.wafflestudio.spring2025.timetable.TimetableNotFoundException
import com.wafflestudio.spring2025.timetable.dto.core.TimetableDto
import com.wafflestudio.spring2025.timetable.dto.response.AddCourseResponse
import com.wafflestudio.spring2025.timetable.dto.response.TimetableDetailResponse
import com.wafflestudio.spring2025.timetable.model.Enroll
import com.wafflestudio.spring2025.timetable.model.Timetable
import com.wafflestudio.spring2025.timetable.repository.EnrollRepository
import com.wafflestudio.spring2025.timetable.repository.TimetableRepository
import com.wafflestudio.spring2025.user.model.User
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import java.time.LocalDate
import kotlin.jvm.optionals.getOrNull

@Service
class TimetableService(
    private val timetableRepository: TimetableRepository,
    private val courseRepository: CourseRepository,
    private val enrollRepository: EnrollRepository,
) {
    fun create(
        user: User,
        year: Int,
        semester: Semester,
        name: String,
    ): TimetableDto {
        // Exceptions
        if (name.isBlank()) {
            throw TimetableNameBlankException()
        }
        val currentYear = LocalDate.now().year
        if (year !in 2013..currentYear) {
            throw TimetableInvalidYearException(currentYear)
        }
        if (timetableRepository.existsByUserIdAndYearAndSemesterAndName(user.id!!, year, semester, name)) {
            throw TimetableDuplicateException()
        }

        // Create new timetable
        val timetable =
            timetableRepository.save(
                Timetable(
                    userId = user.id!!,
                    name = name,
                    year = year,
                    semester = semester,
                ),
            )

        return TimetableDto(timetable)
    }

    fun getAll(user: User): List<TimetableDto> {
        // Get all timetable of logged-in user
        val timetable = timetableRepository.findByUserId(user.id!!)
        return timetable.map { TimetableDto(it) }
    }

    fun update(
        user: User,
        timetableId: Long,
        name: String,
    ): TimetableDto {
        // Exceptions
        val timetable =
            timetableRepository.findById(timetableId).getOrNull()
                ?: throw TimetableNotFoundException()
        if (user.id != timetable.userId) {
            throw TimetableModifyForbiddenException()
        }
        if (name.isBlank()) {
            throw TimetableNameBlankException()
        }
        if (timetableRepository.existsByUserIdAndYearAndSemesterAndName(
                user.id!!,
                timetable.year,
                timetable.semester,
                name
            )
        ) {
            throw TimetableDuplicateException()
        }

        // Update timetable name
        val newTimetable =
            timetableRepository.save(
                Timetable(
                    id = timetableId,
                    userId = user.id!!,
                    name = name,
                    year = timetable.year,
                    semester = timetable.semester,
                ),
            )

        return TimetableDto(newTimetable)
    }

    fun delete(
        user: User,
        timetableId: Long,
    ) {
        // Exceptions
        val timetable =
            timetableRepository.findById(timetableId).getOrNull()
                ?: throw TimetableNotFoundException()
        if (user.id != timetable.userId) {
            throw TimetableModifyForbiddenException()
        }

        // Delete timetable
        timetableRepository.delete(timetable)
    }

    fun getDetail(
        timetableId: Long,
    ): TimetableDetailResponse {
        // Exceptions
        val timetable = timetableRepository.findById(timetableId).getOrNull()
            ?: throw TimetableNotFoundException()

        // Get timetable detail response
        val courses = courseRepository.findByTimetableId(timetableId)
        val credits = courses.sumOf { it.credit }

        return TimetableDetailResponse(
            timetable = TimetableDto(timetable),
            courses = courses.map { CourseDto(it) },
            credits = credits,
        )
    }

    fun addCourse(
        user: User,
        timetableId: Long,
        courseId: Long,
    ): AddCourseResponse {
        // Exceptions
        val timetable = timetableRepository.findById(timetableId).getOrNull()
            ?: throw TimetableNotFoundException()
        if (user.id != timetable.userId) {
            throw TimetableModifyForbiddenException()
        }
        val newCourse = courseRepository.findById(courseId).getOrNull()
            ?: throw CourseNotFoundException()
        if (newCourse.year != timetable.year || newCourse.semester != timetable.semester) {
            throw CourseTimetableNotMatchException()
        }
        if (enrollRepository.existsByTimetableIdAndCourseId(timetableId, courseId)) {
            throw CourseDuplicateException()
        }

        // Check if course time overlaps with existing courses
        val existingCourses = courseRepository.findByTimetableId(timetableId)
        validateTimeConflict(newCourse, existingCourses)

        // Add course to timetable
        enrollRepository.save(
            Enroll(
                timetableId = timetableId,
                courseId = courseId,
            )
        )

        return courseRepository.findByTimetableId(timetableId).map { CourseDto(it) }
    }

    fun deleteCourse(
        user: User,
        timetableId: Long,
        courseId: Long,
    ) {
        // Exceptions
        val timetable = timetableRepository.findById(timetableId).getOrNull()
            ?: throw TimetableNotFoundException()
        if (user.id != timetable.userId) {
            throw TimetableModifyForbiddenException()
        }
        if (!enrollRepository.existsByTimetableIdAndCourseId(timetableId, courseId)) {
            throw CourseNotExistsInTimetableException()
        }

        // Delete course from timetable
        enrollRepository.deleteByTimetableIdAndCourseId(timetableId, courseId)
    }

    private fun validateTimeConflict(newCourse: Course, existingCourses: List<Course>) {
        val newCourseTimes = newCourse.classTimeJson ?: return

        for (existingCourse in existingCourses) {
            val existingCourseTimes = existingCourse.classTimeJson ?: continue

            for (newTime in newCourseTimes) {
                for (existingTime in existingCourseTimes) {
                    if (isTimeOverlap(newTime, existingTime)) {
                        throw CourseOverlapException()
                    }
                }
            }
        }
    }

    private fun isTimeOverlap(time1: ClassPlaceAndTime, time2: ClassPlaceAndTime): Boolean {
        return time1.day == time2.day &&
            time1.endMinute > time2.startMinute &&
            time2.endMinute > time1.startMinute
    }
}
