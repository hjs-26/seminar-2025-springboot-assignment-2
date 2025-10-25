package com.wafflestudio.spring2025.timetable.controller

import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api")
class TimetableController(
) {
    @PostMapping
    fun create() {    }

}
