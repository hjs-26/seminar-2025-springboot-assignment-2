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

class TimetableNameBlankException :
    TimetableException(
        errorCode = 0,
        httpStatusCode = HttpStatus.BAD_REQUEST,
        msg = "Timetable name must not be blank",
    )

class TimetableInvalidYearException(
    currentYear: Int,
) : TimetableException(
        errorCode = 0,
        httpStatusCode = HttpStatus.BAD_REQUEST,
        msg = "Year must be between 2013 and $currentYear",
    )

class TimetableDuplicateException :
    TimetableException(
        errorCode = 0,
        httpStatusCode = HttpStatus.CONFLICT,
        msg = "Timetable with the same name already exists for this semester",
    )

class TimetableNotFoundException :
    TimetableException(
        errorCode = 0,
        httpStatusCode = HttpStatus.NOT_FOUND,
        msg = "Timetable not found",
    )

class TimetableModifyForbiddenException :
    TimetableException(
        errorCode = 0,
        httpStatusCode = HttpStatus.FORBIDDEN,
        msg = "You don't have permission to modify this timetable",
    )
