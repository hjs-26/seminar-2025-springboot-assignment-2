package com.wafflestudio.spring2025

import com.fasterxml.jackson.databind.ObjectMapper
import com.wafflestudio.spring2025.common.enum.Semester
import com.wafflestudio.spring2025.helper.DataGenerator
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.testcontainers.junit.jupiter.Testcontainers

@SpringBootTest
@ActiveProfiles("test")
@Testcontainers
@AutoConfigureMockMvc
class TimetableIntegrationTest
    @Autowired
    constructor(
        private val mvc: MockMvc,
        private val mapper: ObjectMapper,
        private val dataGenerator: DataGenerator,
    ) {
        @Test
        fun `should create a timetable`() {
            // 시간표를 생성할 수 있다
            val (user, token) = dataGenerator.generateUser()
            val request = mapOf(
                "name" to "2025-2 시간표",
                "year" to 2025,
                "semester" to "FALL"
            )

            mvc.perform(
                post("/api/v1/timetables")
                    .header("Authorization", "Bearer $token")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(mapper.writeValueAsString(request))
            )
                .andExpect(status().isCreated)
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("2025-2 시간표"))
                .andExpect(jsonPath("$.year").value(2025))
                .andExpect(jsonPath("$.semester").value("FALL"))
        }

        @Test
        fun `should retrieve all own timetables`() {
            // 자신의 모든 시간표 목록을 조회할 수 있다
            val (user, token) = dataGenerator.generateUser()
            dataGenerator.generateTimetable(name = "2025-1 시간표", year = 2025, semester = Semester.SPRING, user = user)
            dataGenerator.generateTimetable(name = "2025-2 시간표", year = 2025, semester = Semester.FALL, user = user)

            mvc.perform(
                get("/api/v1/timetables")
                    .header("Authorization", "Bearer $token")
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").exists())
                .andExpect(jsonPath("$[1].name").exists())
        }

        @Test
        fun `should retrieve timetable details`() {
            // 시간표 상세 정보를 조회할 수 있다
            val (user, token) = dataGenerator.generateUser()
            val timetable = dataGenerator.generateTimetable(name = "2025-2 시간표", user = user)

            mvc.perform(
                get("/api/v1/timetables/${timetable.id}")
                    .header("Authorization", "Bearer $token")
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.id").value(timetable.id!!))
                .andExpect(jsonPath("$.name").value("2025-2 시간표"))
                .andExpect(jsonPath("$.courses").isArray)
                .andExpect(jsonPath("$.totalCredits").exists())
        }

        @Test
        fun `should update timetable name`() {
            // 시간표 이름을 수정할 수 있다
            val (user, token) = dataGenerator.generateUser()
            val timetable = dataGenerator.generateTimetable(name = "기존 시간표", user = user)
            val request = mapOf("name" to "새로운 시간표")

            mvc.perform(
                patch("/api/v1/timetables/${timetable.id}")
                    .header("Authorization", "Bearer $token")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(mapper.writeValueAsString(request))
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.id").value(timetable.id!!))
                .andExpect(jsonPath("$.name").value("새로운 시간표"))
        }

        @Test
        fun `should not update another user's timetable`() {
            // 다른 사람의 시간표는 수정할 수 없다
            val (user1, token1) = dataGenerator.generateUser()
            val (user2, token2) = dataGenerator.generateUser()
            val timetable = dataGenerator.generateTimetable(name = "user1 시간표", user = user1)
            val request = mapOf("name" to "해킹 시도")

            mvc.perform(
                patch("/api/v1/timetables/${timetable.id}")
                    .header("Authorization", "Bearer $token2")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(mapper.writeValueAsString(request))
            )
                .andExpect(status().isForbidden)
        }

        @Test
        fun `should delete a timetable`() {
            // 시간표를 삭제할 수 있다
            val (user, token) = dataGenerator.generateUser()
            val timetable = dataGenerator.generateTimetable(name = "삭제할 시간표", user = user)

            mvc.perform(
                delete("/api/v1/timetables/${timetable.id}")
                    .header("Authorization", "Bearer $token")
            )
                .andExpect(status().isNoContent)
        }

        @Test
        fun `should not delete another user's timetable`() {
            // 다른 사람의 시간표는 삭제할 수 없다
            val (user1, token1) = dataGenerator.generateUser()
            val (user2, token2) = dataGenerator.generateUser()
            val timetable = dataGenerator.generateTimetable(name = "user1 시간표", user = user1)

            mvc.perform(
                delete("/api/v1/timetables/${timetable.id}")
                    .header("Authorization", "Bearer $token2")
            )
                .andExpect(status().isForbidden)
        }

        @Test
        @Disabled("강의 검색 기능 구현 후 테스트")
        fun `should search for courses`() {
            // 강의를 검색할 수 있다
        }

        @Test
        @Disabled("강의 추가 기능 구현 후 테스트")
        fun `should add a course to timetable`() {
            // 시간표에 강의를 추가할 수 있다
        }

        @Test
        @Disabled("강의 추가 기능 구현 후 테스트")
        fun `should return error when adding overlapping course to timetable`() {
            // 시간표에 강의 추가 시, 시간이 겹치면 에러를 반환한다
        }

        @Test
        @Disabled("강의 추가 기능 구현 후 테스트")
        fun `should not add a course to another user's timetable`() {
            // 다른 사람의 시간표에는 강의를 추가할 수 없다
        }

        @Test
        @Disabled("강의 삭제 기능 구현 후 테스트")
        fun `should remove a course from timetable`() {
            // 시간표에서 강의를 삭제할 수 있다
        }

        @Test
        @Disabled("강의 삭제 기능 구현 후 테스트")
        fun `should not remove a course from another user's timetable`() {
            // 다른 사람의 시간표에서는 강의를 삭제할 수 없다
        }

        @Test
        @Disabled("곧 안내드리겠습니다")
        fun `should fetch and save course information from SNU course registration site`() {
            // 서울대 수강신청 사이트에서 강의 정보를 가져와 저장할 수 있다
        }

        @Test
        @Disabled("강의 추가 기능 구현 후 테스트")
        fun `should return correct course list and total credits when retrieving timetable details`() {
            // 시간표 상세 조회 시, 강의 정보 목록과 총 학점이 올바르게 반환된다
        }

        @Test
        @Disabled("강의 검색은 course 패키지에서 담당")
        fun `should paginate correctly when searching for courses`() {
            // 강의 검색 시, 페이지네이션이 올바르게 동작한다
        }
    }
