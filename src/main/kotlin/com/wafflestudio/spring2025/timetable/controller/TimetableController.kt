package com.wafflestudio.spring2025.timetable.controller

import com.wafflestudio.spring2025.timetable.dto.request.CreateTimetableRequest
import com.wafflestudio.spring2025.timetable.dto.response.CreateTimetableResponse
import com.wafflestudio.spring2025.timetable.service.TimetableService
import com.wafflestudio.spring2025.user.LoggedInUser
import com.wafflestudio.spring2025.user.model.User
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1")
class TimetableController(
    private val timetableService: TimetableService,
) {
    @PostMapping("/timetables")
    fun create(
        @LoggedInUser user: User,
        @RequestBody createRequest: CreateTimetableRequest,
    ): ResponseEntity<CreateTimetableResponse> {
        val timetableDto =
            timetableService.create(
                user = user,
                semester = createRequest.semester,
                name = createRequest.name,
                year = createRequest.year,
            )
        return ResponseEntity.status(HttpStatus.CREATED).body(timetableDto)
    }

}
