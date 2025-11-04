package com.wafflestudio.spring2025.course

import com.wafflestudio.spring2025.DomainException
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatusCode

sealed class CourseException(
    errorCode: Int,
    httpStatusCode: HttpStatusCode,
    msg: String,
    cause: Throwable? = null,
) : DomainException(errorCode, httpStatusCode, msg, cause)

class CourseNotFoundException :
    CourseException(
        errorCode = 0,
        httpStatusCode = HttpStatus.NOT_FOUND,
        msg = "Course not found",
    )

class IllegalPeriodException :
    CourseException(
        errorCode = 0,
        httpStatusCode = HttpStatus.BAD_REQUEST,
        msg = "Search for Illegal period",
    )

class InvalidSemesterFormatException :
    CourseException(
        errorCode = 0,
        httpStatusCode = HttpStatus.BAD_REQUEST,
        msg = "semester must be YYYY-1, YYYY-2, YYYY-3, or YYYY-4 format",
    )
