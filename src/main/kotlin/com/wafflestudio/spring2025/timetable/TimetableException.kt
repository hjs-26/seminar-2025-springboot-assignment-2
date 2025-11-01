package com.wafflestudio.spring2025.timetable

import com.wafflestudio.spring2025.DomainException
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatusCode

sealed class TimetableException(
    errorCode: Int,
    httpStatusCode: HttpStatusCode,
    msg: String,
    cause: Throwable? = null,
) : DomainException(errorCode, httpStatusCode, msg, cause)

class TimetableNotFoundException :
    TimetableException(
        errorCode = 0,
        httpStatusCode = HttpStatus.NOT_FOUND,
        msg = "Timetable not found",
    )

class CourseNotFoundException :
    TimetableException(
        errorCode = 1,
        httpStatusCode = HttpStatus.NOT_FOUND,
        msg = "Course not found",
    )

class TimeConflictException(conflictingCourseTitle: String) :
    TimetableException(
        errorCode = 2,
        httpStatusCode = HttpStatus.CONFLICT,
        msg = "Time conflict with existing course: $conflictingCourseTitle",
    )

class CourseAlreadyAddedException :
    TimetableException(
        errorCode = 3,
        httpStatusCode = HttpStatus.CONFLICT,
        msg = "Course already added to timetable",
    )

class UnauthorizedTimetableAccessException :
    TimetableException(
        errorCode = 4,
        httpStatusCode = HttpStatus.FORBIDDEN,
        msg = "You don't have permission to access this timetable",
    )

