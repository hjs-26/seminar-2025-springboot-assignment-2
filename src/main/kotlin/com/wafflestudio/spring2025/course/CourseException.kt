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

class CourseNameBlankException :
    CourseException(
        errorCode = 0,
        httpStatusCode = HttpStatus.BAD_REQUEST,
        msg = "Course name is blank",
    )

class CourseNameConflictException :
    CourseException(
        errorCode = 0,
        httpStatusCode = HttpStatus.CONFLICT,
        msg = "Course name already exists",
    )

class IllegalPeriodException :
    CourseException(
        errorCode = 0,
        httpStatusCode = HttpStatus.NOT_FOUND,
        msg = "Search for Illegal period",
    )