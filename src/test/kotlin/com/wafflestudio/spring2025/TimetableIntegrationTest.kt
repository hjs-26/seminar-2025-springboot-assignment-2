package com.wafflestudio.spring2025

import com.fasterxml.jackson.databind.ObjectMapper
import com.wafflestudio.spring2025.common.enum.Semester
import com.wafflestudio.spring2025.course.crawling.ClassPlaceAndTime
import com.wafflestudio.spring2025.course.crawling.DayOfWeek
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
        // ========== 시간표 생성 테스트 ==========
        @Test
        fun `should create a timetable`() {
            // 시간표를 생성할 수 있다
            val (user, token) = dataGenerator.generateUser()
            val request =
                mapOf(
                    "name" to "2025-2 시간표",
                    "year" to 2025,
                    "semester" to "FALL",
                )

            mvc
                .perform(
                    post("/api/v1/timetables")
                        .header("Authorization", "Bearer $token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(request)),
                ).andExpect(status().isCreated)
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("2025-2 시간표"))
                .andExpect(jsonPath("$.year").value(2025))
                .andExpect(jsonPath("$.semester").value("FALL"))
        }

        @Test
        fun `should return error when creating timetable with blank name`() {
            // 빈 이름으로 시간표를 생성하면 에러를 반환한다
            val (user, token) = dataGenerator.generateUser()
            val request =
                mapOf(
                    "name" to "   ",
                    "year" to 2025,
                    "semester" to "FALL",
                )

            mvc
                .perform(
                    post("/api/v1/timetables")
                        .header("Authorization", "Bearer $token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(request)),
                ).andExpect(status().isBadRequest)
        }

        @Test
        fun `should return error when creating timetable with invalid year`() {
            // 잘못된 연도로 시간표를 생성하면 에러를 반환한다
            val (user, token) = dataGenerator.generateUser()
            val request =
                mapOf(
                    "name" to "2030 시간표",
                    "year" to 2030,
                    "semester" to "FALL",
                )

            mvc
                .perform(
                    post("/api/v1/timetables")
                        .header("Authorization", "Bearer $token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(request)),
                ).andExpect(status().isBadRequest)
        }

        @Test
        fun `should return error when creating duplicate timetable`() {
            // 중복된 시간표를 생성하면 에러를 반환한다
            val (user, token) = dataGenerator.generateUser()
            dataGenerator.generateTimetable(name = "2025-2 시간표", year = 2025, semester = Semester.FALL, user = user)

            val request =
                mapOf(
                    "name" to "2025-2 시간표",
                    "year" to 2025,
                    "semester" to "FALL",
                )

            mvc
                .perform(
                    post("/api/v1/timetables")
                        .header("Authorization", "Bearer $token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(request)),
                ).andExpect(status().isConflict)
        }

        // ========== 시간표 전체 조회 테스트 ==========
        @Test
        fun `should retrieve all own timetables`() {
            // 자신의 모든 시간표 목록을 조회할 수 있다
            val (user, token) = dataGenerator.generateUser()
            dataGenerator.generateTimetable(name = "2025-1 시간표", year = 2025, semester = Semester.SPRING, user = user)
            dataGenerator.generateTimetable(name = "2025-2 시간표", year = 2025, semester = Semester.FALL, user = user)

            mvc
                .perform(
                    get("/api/v1/timetables")
                        .header("Authorization", "Bearer $token"),
                ).andExpect(status().isOk)
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").exists())
                .andExpect(jsonPath("$[1].name").exists())
        }

        @Test
        fun `should return empty list when user has no timetables`() {
            // 시간표가 없는 유저는 빈 리스트를 조회한다
            val (user, token) = dataGenerator.generateUser()

            mvc
                .perform(
                    get("/api/v1/timetables")
                        .header("Authorization", "Bearer $token"),
                ).andExpect(status().isOk)
                .andExpect(jsonPath("$.length()").value(0))
        }

        // ========== 시간표 상세 조회 테스트 ==========
        @Test
        fun `should retrieve timetable details`() {
            // 시간표 상세 정보를 조회할 수 있다
            val (user, token) = dataGenerator.generateUser()
            val timetable = dataGenerator.generateTimetable(name = "2025-2 시간표", year = 2025, semester = Semester.FALL, user = user)

            mvc
                .perform(
                    get("/api/v1/timetables/${timetable.id}"),
                ).andExpect(status().isOk)
                .andExpect(jsonPath("$.timetable.id").value(timetable.id!!))
                .andExpect(jsonPath("$.timetable.name").value("2025-2 시간표"))
                .andExpect(jsonPath("$.courses").isArray)
                .andExpect(jsonPath("$.credits").exists())
        }

        @Test
        fun `should return error when retrieving non-existent timetable`() {
            // 존재하지 않는 시간표를 조회하면 에러를 반환한다
            val (user, token) = dataGenerator.generateUser()

            mvc
                .perform(
                    get("/api/v1/timetables/999999"),
                ).andExpect(status().isNotFound)
        }

        @Test
        fun `should return correct course list and total credits when retrieving timetable details`() {
            // 시간표 상세 조회 시, 강의 정보 목록과 총 학점이 올바르게 반환된다
            val (user, token) = dataGenerator.generateUser()
            val timetable = dataGenerator.generateTimetable(name = "2025-2 시간표", year = 2025, semester = Semester.FALL, user = user)

            val course1 = dataGenerator.generateCourse(year = 2025, semester = Semester.FALL, courseTitle = "자료구조", credit = 3)
            val course2 = dataGenerator.generateCourse(year = 2025, semester = Semester.FALL, courseTitle = "알고리즘", credit = 3)
            val course3 = dataGenerator.generateCourse(year = 2025, semester = Semester.FALL, courseTitle = "데이터베이스", credit = 4)

            dataGenerator.generateEnroll(timetable, course1)
            dataGenerator.generateEnroll(timetable, course2)
            dataGenerator.generateEnroll(timetable, course3)

            mvc
                .perform(
                    get("/api/v1/timetables/${timetable.id}"),
                ).andExpect(status().isOk)
                .andExpect(jsonPath("$.courses.length()").value(3))
                .andExpect(jsonPath("$.credits").value(10)) // 3 + 3 + 4 = 10
        }

        // ========== 시간표 수정 테스트 ==========
        @Test
        fun `should update timetable name`() {
            // 시간표 이름을 수정할 수 있다
            val (user, token) = dataGenerator.generateUser()
            val timetable = dataGenerator.generateTimetable(name = "기존 시간표", user = user)
            val request = mapOf("name" to "새로운 시간표")

            mvc
                .perform(
                    patch("/api/v1/timetables/${timetable.id}")
                        .header("Authorization", "Bearer $token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(request)),
                ).andExpect(status().isOk)
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

            mvc
                .perform(
                    patch("/api/v1/timetables/${timetable.id}")
                        .header("Authorization", "Bearer $token2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(request)),
                ).andExpect(status().isForbidden)
        }

        @Test
        fun `should return error when updating timetable with blank name`() {
            // 빈 이름으로 시간표를 수정하면 에러를 반환한다
            val (user, token) = dataGenerator.generateUser()
            val timetable = dataGenerator.generateTimetable(name = "기존 시간표", user = user)
            val request = mapOf("name" to "  ")

            mvc
                .perform(
                    patch("/api/v1/timetables/${timetable.id}")
                        .header("Authorization", "Bearer $token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(request)),
                ).andExpect(status().isBadRequest)
        }

        @Test
        fun `should return error when updating non-existent timetable`() {
            // 존재하지 않는 시간표를 수정하면 에러를 반환한다
            val (user, token) = dataGenerator.generateUser()
            val request = mapOf("name" to "새 이름")

            mvc
                .perform(
                    patch("/api/v1/timetables/999999")
                        .header("Authorization", "Bearer $token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(request)),
                ).andExpect(status().isNotFound)
        }

        @Test
        fun `should return error when updating timetable with duplicate name`() {
            // 중복된 이름으로 시간표를 수정하면 에러를 반환한다
            val (user, token) = dataGenerator.generateUser()
            dataGenerator.generateTimetable(name = "2025-1 시간표", year = 2025, semester = Semester.SPRING, user = user)
            val timetable2 = dataGenerator.generateTimetable(name = "2025-2 시간표", year = 2025, semester = Semester.SPRING, user = user)
            val request = mapOf("name" to "2025-1 시간표")

            mvc
                .perform(
                    patch("/api/v1/timetables/${timetable2.id}")
                        .header("Authorization", "Bearer $token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(request)),
                ).andExpect(status().isConflict)
        }

        // ========== 시간표 삭제 테스트 ==========
        @Test
        fun `should delete a timetable`() {
            // 시간표를 삭제할 수 있다
            val (user, token) = dataGenerator.generateUser()
            val timetable = dataGenerator.generateTimetable(name = "삭제할 시간표", user = user)

            mvc
                .perform(
                    delete("/api/v1/timetables/${timetable.id}")
                        .header("Authorization", "Bearer $token"),
                ).andExpect(status().isNoContent)
        }

        @Test
        fun `should not delete another user's timetable`() {
            // 다른 사람의 시간표는 삭제할 수 없다
            val (user1, token1) = dataGenerator.generateUser()
            val (user2, token2) = dataGenerator.generateUser()
            val timetable = dataGenerator.generateTimetable(name = "user1 시간표", user = user1)

            mvc
                .perform(
                    delete("/api/v1/timetables/${timetable.id}")
                        .header("Authorization", "Bearer $token2"),
                ).andExpect(status().isForbidden)
        }

        @Test
        fun `should return error when deleting non-existent timetable`() {
            // 존재하지 않는 시간표를 삭제하면 에러를 반환한다
            val (user, token) = dataGenerator.generateUser()

            mvc
                .perform(
                    delete("/api/v1/timetables/999999")
                        .header("Authorization", "Bearer $token"),
                ).andExpect(status().isNotFound)
        }

        // ========== 강의 추가 테스트 ==========
        @Test
        fun `should add a course to timetable`() {
            // 시간표에 강의를 추가할 수 있다
            val (user, token) = dataGenerator.generateUser()
            val timetable = dataGenerator.generateTimetable(name = "2025-2 시간표", year = 2025, semester = Semester.FALL, user = user)
            val course =
                dataGenerator.generateCourse(
                    year = 2025,
                    semester = Semester.FALL,
                    courseTitle = "자료구조",
                    credit = 3,
                    classTimeJson =
                        listOf(
                            ClassPlaceAndTime(day = DayOfWeek.MONDAY, place = "302-308", startMinute = 540, endMinute = 630),
                        ),
                )

            mvc
                .perform(
                    post("/api/v1/timetables/${timetable.id}/courses/${course.id}")
                        .header("Authorization", "Bearer $token"),
                ).andExpect(status().isOk)
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].courseTitle").value("자료구조"))
        }

        @Test
        fun `should return error when adding overlapping course to timetable`() {
            // 시간표에 강의 추가 시, 시간이 겹치면 에러를 반환한다
            val (user, token) = dataGenerator.generateUser()
            val timetable = dataGenerator.generateTimetable(name = "2025-2 시간표", year = 2025, semester = Semester.FALL, user = user)

            val course1 =
                dataGenerator.generateCourse(
                    year = 2025,
                    semester = Semester.FALL,
                    courseTitle = "자료구조",
                    classTimeJson =
                        listOf(
                            ClassPlaceAndTime(day = DayOfWeek.MONDAY, place = "302-308", startMinute = 540, endMinute = 630),
                        ),
                )
            val course2 =
                dataGenerator.generateCourse(
                    year = 2025,
                    semester = Semester.FALL,
                    courseTitle = "알고리즘",
                    classTimeJson =
                        listOf(
                            ClassPlaceAndTime(day = DayOfWeek.MONDAY, place = "302-309", startMinute = 600, endMinute = 690),
                        ),
                )

            // 첫 번째 강의 추가 성공
            mvc
                .perform(
                    post("/api/v1/timetables/${timetable.id}/courses/${course1.id}")
                        .header("Authorization", "Bearer $token"),
                ).andExpect(status().isOk)

            // 두 번째 강의 추가 실패 (시간 겹침)
            mvc
                .perform(
                    post("/api/v1/timetables/${timetable.id}/courses/${course2.id}")
                        .header("Authorization", "Bearer $token"),
                ).andExpect(status().isConflict)
        }

        @Test
        fun `should not add a course to another user's timetable`() {
            // 다른 사람의 시간표에는 강의를 추가할 수 없다
            val (user1, token1) = dataGenerator.generateUser()
            val (user2, token2) = dataGenerator.generateUser()
            val timetable = dataGenerator.generateTimetable(name = "user1 시간표", year = 2025, semester = Semester.FALL, user = user1)
            val course = dataGenerator.generateCourse(year = 2025, semester = Semester.FALL, courseTitle = "자료구조")

            mvc
                .perform(
                    post("/api/v1/timetables/${timetable.id}/courses/${course.id}")
                        .header("Authorization", "Bearer $token2"),
                ).andExpect(status().isForbidden)
        }

        @Test
        fun `should return error when adding course with mismatched year or semester`() {
            // 시간표와 년도/학기가 다른 강의를 추가하면 에러를 반환한다
            val (user, token) = dataGenerator.generateUser()
            val timetable = dataGenerator.generateTimetable(name = "2025-2 시간표", year = 2025, semester = Semester.FALL, user = user)
            val course = dataGenerator.generateCourse(year = 2024, semester = Semester.SPRING, courseTitle = "자료구조")

            mvc
                .perform(
                    post("/api/v1/timetables/${timetable.id}/courses/${course.id}")
                        .header("Authorization", "Bearer $token"),
                ).andExpect(status().isBadRequest)
        }

        @Test
        fun `should return error when adding non-existent course to timetable`() {
            // 존재하지 않는 강의를 추가하면 에러를 반환한다
            val (user, token) = dataGenerator.generateUser()
            val timetable = dataGenerator.generateTimetable(name = "2025-2 시간표", year = 2025, semester = Semester.FALL, user = user)

            mvc
                .perform(
                    post("/api/v1/timetables/${timetable.id}/courses/999999")
                        .header("Authorization", "Bearer $token"),
                ).andExpect(status().isNotFound)
        }

        @Test
        fun `should return error when adding course to non-existent timetable`() {
            // 존재하지 않는 시간표에 강의를 추가하면 에러를 반환한다
            val (user, token) = dataGenerator.generateUser()
            val course = dataGenerator.generateCourse(year = 2025, semester = Semester.FALL, courseTitle = "자료구조")

            mvc
                .perform(
                    post("/api/v1/timetables/999999/courses/${course.id}")
                        .header("Authorization", "Bearer $token"),
                ).andExpect(status().isNotFound)
        }

        @Test
        fun `should return error when adding duplicate course to timetable`() {
            // 이미 추가된 강의를 다시 추가하면 에러를 반환한다
            val (user, token) = dataGenerator.generateUser()
            val timetable = dataGenerator.generateTimetable(name = "2025-2 시간표", year = 2025, semester = Semester.FALL, user = user)
            val course =
                dataGenerator.generateCourse(
                    year = 2025,
                    semester = Semester.FALL,
                    courseTitle = "자료구조",
                    classTimeJson =
                        listOf(
                            ClassPlaceAndTime(day = DayOfWeek.MONDAY, place = "302-308", startMinute = 540, endMinute = 630),
                        ),
                )

            // 첫 번째 추가 성공
            mvc
                .perform(
                    post("/api/v1/timetables/${timetable.id}/courses/${course.id}")
                        .header("Authorization", "Bearer $token"),
                ).andExpect(status().isOk)

            // 두 번째 추가 실패 (중복)
            mvc
                .perform(
                    post("/api/v1/timetables/${timetable.id}/courses/${course.id}")
                        .header("Authorization", "Bearer $token"),
                ).andExpect(status().isConflict)
        }

        // ========== 강의 삭제 테스트 ==========
        @Test
        fun `should remove a course from timetable`() {
            // 시간표에서 강의를 삭제할 수 있다
            val (user, token) = dataGenerator.generateUser()
            val timetable = dataGenerator.generateTimetable(name = "2025-2 시간표", year = 2025, semester = Semester.FALL, user = user)
            val course = dataGenerator.generateCourse(year = 2025, semester = Semester.FALL, courseTitle = "자료구조")
            dataGenerator.generateEnroll(timetable, course)

            mvc
                .perform(
                    delete("/api/v1/timetables/${timetable.id}/courses/${course.id}")
                        .header("Authorization", "Bearer $token"),
                ).andExpect(status().isNoContent)
        }

        @Test
        fun `should not remove a course from another user's timetable`() {
            // 다른 사람의 시간표에서는 강의를 삭제할 수 없다
            val (user1, token1) = dataGenerator.generateUser()
            val (user2, token2) = dataGenerator.generateUser()
            val timetable = dataGenerator.generateTimetable(name = "user1 시간표", year = 2025, semester = Semester.FALL, user = user1)
            val course = dataGenerator.generateCourse(year = 2025, semester = Semester.FALL, courseTitle = "자료구조")
            dataGenerator.generateEnroll(timetable, course)

            mvc
                .perform(
                    delete("/api/v1/timetables/${timetable.id}/courses/${course.id}")
                        .header("Authorization", "Bearer $token2"),
                ).andExpect(status().isForbidden)
        }

        @Test
        fun `should return error when removing non-existent course from timetable`() {
            // 시간표에 없는 강의를 삭제하면 에러를 반환한다
            val (user, token) = dataGenerator.generateUser()
            val timetable = dataGenerator.generateTimetable(name = "2025-2 시간표", year = 2025, semester = Semester.FALL, user = user)
            val course = dataGenerator.generateCourse(year = 2025, semester = Semester.FALL, courseTitle = "자료구조")

            mvc
                .perform(
                    delete("/api/v1/timetables/${timetable.id}/courses/${course.id}")
                        .header("Authorization", "Bearer $token"),
                ).andExpect(status().isBadRequest)
        }

        @Test
        fun `should return error when removing course from non-existent timetable`() {
            // 존재하지 않는 시간표에서 강의를 삭제하면 에러를 반환한다
            val (user, token) = dataGenerator.generateUser()
            val course = dataGenerator.generateCourse(year = 2025, semester = Semester.FALL, courseTitle = "자료구조")

            mvc
                .perform(
                    delete("/api/v1/timetables/999999/courses/${course.id}")
                        .header("Authorization", "Bearer $token"),
                ).andExpect(status().isNotFound)
        }

        // ========== 강의 검색 (Course 패키지 담당 - 최연서 & 손현준) ==========
        @Test
        @Disabled("강의 검색 기능 구현 후 테스트")
        fun `should search for courses`() {
            // 강의를 검색할 수 있다
        }

        @Test
        @Disabled("강의 검색은 course 패키지에서 담당")
        fun `should paginate correctly when searching for courses`() {
            // 강의 검색 시, 페이지네이션이 올바르게 동작한다
        }

        @Test
        @Disabled("곧 안내드리겠습니다")
        fun `should fetch and save course information from SNU course registration site`() {
            // 서울대 수강신청 사이트에서 강의 정보를 가져와 저장할 수 있다
        }
    }
