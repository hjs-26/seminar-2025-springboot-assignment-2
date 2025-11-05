package com.wafflestudio.spring2025

import com.fasterxml.jackson.databind.ObjectMapper
import com.wafflestudio.spring2025.common.enum.Semester
import com.wafflestudio.spring2025.course.repository.CourseRepository
import com.wafflestudio.spring2025.helper.DataGenerator
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.testcontainers.junit.jupiter.Testcontainers

@SpringBootTest
@ActiveProfiles("test")
@Testcontainers
@AutoConfigureMockMvc
class CourseFetchIntegrationTest
    @Autowired
    constructor(
        private val mvc: MockMvc,
        private val mapper: ObjectMapper,
        private val courseRepository: CourseRepository,
        private val dataGenerator: DataGenerator,
    ) {
        @BeforeEach
        fun cleanup() {
            // 각 테스트 전에 course 데이터 정리하여 중복 키 에러 방지
            courseRepository.deleteAll()
        }

        // @Disabled("실제 서울대 사이트를 호출하므로 수동으로 실행")
        @Test
        fun `should fetch and save courses from SNU course registration site`() {
            // 서울대 수강신청 사이트에서 강의 정보를 크롤링하여 DB에 저장할 수 있다
            val (_, token) = dataGenerator.generateUser()
            val semester = "2025-1"

            val countBefore = courseRepository.count()

            val result =
                mvc
                    .perform(
                        post("/api/v1/courses/fetch")
                            .header("Authorization", "Bearer $token")
                            .param("semester", semester),
                    ).andExpect(status().isOk)
                    .andExpect(jsonPath("$.message").value("Successfully fetched courses"))
                    .andExpect(jsonPath("$.semester").value(semester))
                    .andExpect(jsonPath("$.year").value(2025))
                    .andExpect(jsonPath("$.semesterValue").value("SPRING"))
                    .andExpect(jsonPath("$.count").exists())
                    .andReturn()

            val response = mapper.readTree(result.response.contentAsString)
            val savedCount = response.get("count").asInt()
            assertTrue(savedCount > 0, "크롤링된 강의가 1개 이상이어야 합니다")

            val countAfter = courseRepository.count()
            assertEquals(countBefore + savedCount, countAfter, "DB에 저장된 강의 수가 일치해야 합니다")

            val courses = courseRepository.search(2025, Semester.SPRING, null, null, 1)
            assertFalse(courses.isEmpty(), "저장된 강의가 최소 1개 이상 있어야 합니다")

            val course = courses.first()
            assertNotNull(course.courseNumber, "교과목번호가 있어야 합니다")
            assertNotNull(course.lectureNumber, "강좌번호가 있어야 합니다")
            assertNotNull(course.courseTitle, "교과목명이 있어야 합니다")
            assertEquals(2025, course.year, "연도가 2025여야 합니다")
            assertEquals(Semester.SPRING, course.semester, "학기가 SPRING이어야 합니다")
        }

        @Test
        fun `should return 400 when semester format is invalid`() {
            // 잘못된 semester 형식으로 요청 시 400 에러를 반환한다
            val (_, token) = dataGenerator.generateUser()

            val invalidFormats = listOf("2025", "2025-5", "abc", "2025-1-1", "25-1")

            invalidFormats.forEach { invalidSemester ->
                mvc
                    .perform(
                        post("/api/v1/courses/fetch")
                            .header("Authorization", "Bearer $token")
                            .param("semester", invalidSemester),
                    ).andExpect(status().isBadRequest)
            }
        }

        // @Disabled("실제 서울대 사이트를 호출하므로 수동으로 실행")
        @Test
        fun `should parse excel data correctly and save to database`() {
            // 엑셀 데이터를 올바르게 파싱하여 DB에 저장한다
            val (_, token) = dataGenerator.generateUser()
            val semester = "2025-2"

            mvc
                .perform(
                    post("/api/v1/courses/fetch")
                        .header("Authorization", "Bearer $token")
                        .param("semester", semester),
                ).andExpect(status().isOk)
                .andExpect(jsonPath("$.count").exists())

            val courses = courseRepository.search(2025, Semester.SUMMER, null, null, 10)
            assertTrue(courses.isNotEmpty(), "크롤링된 강의가 있어야 합니다")

            val course = courses.first()

            assertNotNull(course.courseNumber, "교과목번호는 null이 아니어야 합니다")
            assertNotNull(course.lectureNumber, "강좌번호는 null이 아니어야 합니다")
            assertNotNull(course.courseTitle, "교과목명은 null이 아니어야 합니다")
            assertTrue(course.credit >= 0, "학점은 0 이상이어야 합니다")

            println("Course: ${course.courseTitle}")
            println("Classification: ${course.classification}")
            println("College: ${course.college}")
            println("Department: ${course.department}")
            println("Instructor: ${course.instructor}")
        }

        // @Disabled("실제 서울대 사이트를 호출하므로 수동으로 실행")
        @Test
        fun `should handle all four semester types correctly`() {
            // 4가지 학기 타입(SPRING, SUMMER, FALL, WINTER)을 모두 올바르게 처리한다
            val (_, token) = dataGenerator.generateUser()

            val semesterMapping =
                mapOf(
                    "2025-1" to Semester.SPRING,
                    "2025-2" to Semester.SUMMER,
                    "2025-3" to Semester.FALL,
                    "2025-4" to Semester.WINTER,
                )

            semesterMapping.forEach { (semesterParam, expectedEnum) ->
                mvc
                    .perform(
                        post("/api/v1/courses/fetch")
                            .header("Authorization", "Bearer $token")
                            .param("semester", semesterParam),
                    ).andExpect(status().isOk)
                    .andExpect(jsonPath("$.semester").value(semesterParam))
                    .andExpect(jsonPath("$.semesterValue").value(expectedEnum.name))

                println("Successfully processed $semesterParam -> $expectedEnum")
            }
        }

        // @Disabled("실제 서울대 사이트를 호출하므로 수동으로 실행")
        @Test
        fun `should enrich course data with API information`() {
            // API 호출을 통해 추가 정보로 강의 데이터를 보완한다
            val (_, token) = dataGenerator.generateUser()
            val semester = "2025-1"

            mvc
                .perform(
                    post("/api/v1/courses/fetch")
                        .header("Authorization", "Bearer $token")
                        .param("semester", semester),
                ).andExpect(status().isOk)

            val courses = courseRepository.search(2025, Semester.SPRING, null, null, 5)
            assertTrue(courses.isNotEmpty(), "크롤링된 강의가 있어야 합니다")

            courses.forEach { course ->
                println("Course: ${course.courseTitle}")
                println("  Instructor: ${course.instructor}")
                println("  Department: ${course.department}")
                println("  Class Time: ${course.classTimeJsonString}")
            }
        }

        // @Disabled("실제 서울대 사이트를 호출하므로 수동으로 실행")
        @Test
        fun `should handle missing columns in excel gracefully`() {
            // 엑셀에서 컬럼이 누락된 경우 적절히 처리한다
            val (_, token) = dataGenerator.generateUser()
            val semester = "2025-1"

            mvc
                .perform(
                    post("/api/v1/courses/fetch")
                        .header("Authorization", "Bearer $token")
                        .param("semester", semester),
                ).andExpect(status().isOk)

            val courses = courseRepository.search(2025, Semester.SPRING, null, null, 100)

            courses.forEach { course ->
                assertNotNull(course.courseNumber, "교과목번호는 필수입니다")
                assertNotNull(course.lectureNumber, "강좌번호는 필수입니다")
                assertNotNull(course.courseTitle, "교과목명은 필수입니다")
            }
        }
    }
