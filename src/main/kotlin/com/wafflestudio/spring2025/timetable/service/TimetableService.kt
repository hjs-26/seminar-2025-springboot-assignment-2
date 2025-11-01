package com.wafflestudio.spring2025.timetable.service

import com.wafflestudio.spring2025.common.enum.Semester
import com.wafflestudio.spring2025.timetable.TimetableDuplicateException
import com.wafflestudio.spring2025.timetable.TimetableInvalidYearException
import com.wafflestudio.spring2025.timetable.TimetableModifyForbiddenException
import com.wafflestudio.spring2025.timetable.TimetableNameBlankException
import com.wafflestudio.spring2025.timetable.TimetableNotFoundException
import com.wafflestudio.spring2025.timetable.dto.core.TimetableDto
import com.wafflestudio.spring2025.timetable.model.Timetable
import com.wafflestudio.spring2025.timetable.repository.TimetableRepository
import com.wafflestudio.spring2025.user.model.User
import org.springframework.stereotype.Service
import java.time.LocalDate
import kotlin.jvm.optionals.getOrNull

@Service
class TimetableService(
    private val timetableRepository: TimetableRepository,
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
        if (timetableRepository.existsByUserIdAndYearAndSemesterAndName(user.id!!, timetable.year, timetable.semester, name)) {
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
}
