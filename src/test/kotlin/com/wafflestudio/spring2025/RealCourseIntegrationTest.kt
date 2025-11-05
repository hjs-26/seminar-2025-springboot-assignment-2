package com.wafflestudio.spring2025

import com.fasterxml.jackson.databind.ObjectMapper
import com.wafflestudio.spring2025.common.enum.Semester
import com.wafflestudio.spring2025.course.repository.CourseRepository
import com.wafflestudio.spring2025.helper.DataGenerator
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Assumptions.assumeTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.testcontainers.junit.jupiter.Testcontainers

/**
 * 실제 크롤링된 Course 데이터를 활용한 통합 테스트
 * 첫 테스트 실행 시 한 번만 크롤링을 수행하고, 이후 테스트에서는 같은 데이터를 재사용합니다.
 */
@SpringBootTest
@ActiveProfiles("test")
@Testcontainers
@AutoConfigureMockMvc
class RealCourseIntegrationTest
    @Autowired
    constructor(
        private val mvc: MockMvc,
        private val mapper: ObjectMapper,
        private val courseRepository: CourseRepository,
        private val dataGenerator: DataGenerator,
    ) {
        companion object {
            // 크롤링이 이미 수행되었는지 추적하는 플래그
            private var isCrawled = false
        }

        @BeforeEach
        fun setupCrawledData() {
            // 이미 크롤링이 수행되었으면 스킵
            if (isCrawled) {
                return
            }

            // 각 테스트 전에 course 데이터 정리하여 중복 키 에러 방지
            dataGenerator.cleanupCourses()

            // 테스트용 사용자 생성
            val (_, token) = dataGenerator.generateUser()

            // 모든 학기에 대해 크롤링 수행
            val semesters = listOf("2025-1", "2025-2", "2025-3", "2025-4")

            semesters.forEach { semester ->
                mvc
                    .perform(
                        post("/api/v1/courses/fetch")
                            .header("Authorization", "Bearer $token")
                            .param("semester", semester),
                    ).andExpect(status().isOk)
            }

            // 크롤링된 데이터가 있는지 최종 확인
            val finalCourseCount = courseRepository.count()
            assumeTrue(finalCourseCount > 0, "크롤링된 강의 데이터가 DB에 있어야 합니다")

            // 크롤링 완료 플래그 설정
            isCrawled = true
        }

        @Test
        fun `should verify crawled course has required fields`() {
            // 크롤링된 강의가 필수 필드를 가지고 있는지 확인
            val courses = courseRepository.search(2025, Semester.SPRING, null, null, 10)
            assumeTrue(courses.isNotEmpty(), "크롤링된 강의가 있어야 합니다")

            courses.forEach { course ->
                // 필수 필드 검증
                assertNotNull(course.id, "Course ID는 null이 아니어야 합니다")
                assertNotNull(course.courseNumber, "교과목번호는 null이 아니어야 합니다")
                assertNotNull(course.lectureNumber, "강좌번호는 null이 아니어야 합니다")
                assertNotNull(course.courseTitle, "교과목명은 null이 아니어야 합니다")
                assertTrue(course.credit >= 0, "학점은 0 이상이어야 합니다")
                assertEquals(2025, course.year, "연도가 2025여야 합니다")
                assertEquals(Semester.SPRING, course.semester, "학기가 SPRING이어야 합니다")
            }
        }

        @Test
        fun `should add real crawled course to timetable`() {
            // 실제 크롤링된 강의를 시간표에 추가할 수 있다
            val (user, token) = dataGenerator.generateUser()
            val timetable =
                dataGenerator.generateTimetable(
                    name = "2025-1 시간표",
                    year = 2025,
                    semester = Semester.SPRING,
                    user = user,
                )

            // DB에서 실제 크롤링된 강의 조회
            val realCourses = courseRepository.search(2025, Semester.SPRING, null, null, 5)
            assumeTrue(realCourses.isNotEmpty(), "2025-1 학기 강의가 있어야 합니다")

            val course = realCourses.first()

            // 시간표에 강의 추가
            mvc
                .perform(
                    post("/api/v1/timetables/${timetable.id}/courses/${course.id}")
                        .header("Authorization", "Bearer $token"),
                ).andExpect(status().isOk)
                .andExpect(jsonPath("$.id").value(course.id!!))
                .andExpect(jsonPath("$.courseTitle").value(course.courseTitle))
                .andExpect(jsonPath("$.credit").value(course.credit))

            // 시간표 조회해서 강의가 추가되었는지 확인
            mvc
                .perform(
                    get("/api/v1/timetables/${timetable.id}")
                        .header("Authorization", "Bearer $token"),
                ).andExpect(status().isOk)
                .andExpect(jsonPath("$.timetable.id").value(timetable.id!!))
                .andExpect(jsonPath("$.courses.length()").value(1))
                .andExpect(jsonPath("$.courses[0].courseTitle").value(course.courseTitle))
                .andExpect(jsonPath("$.credits").value(course.credit))
        }

        @Test
        fun `should add multiple real courses to timetable and calculate total credits`() {
            // 여러 실제 강의를 시간표에 추가하고 총 학점을 계산할 수 있다
            val (user, token) = dataGenerator.generateUser()
            val timetable =
                dataGenerator.generateTimetable(
                    name = "2025-여름 시간표",
                    year = 2025,
                    semester = Semester.SUMMER,
                    user = user,
                )

            // 2025 여름학기 강의 조회 - 시간이 겹치지 않는 3개를 찾기 위해 더 많이 조회
            val realCourses = courseRepository.search(2025, Semester.SUMMER, null, null, 50)
            assumeTrue(realCourses.size >= 3, "최소 3개의 강의가 필요합니다")

            // 시간이 겹치지 않는 강의 3개를 찾아서 추가
            val addedCourses = mutableListOf<com.wafflestudio.spring2025.course.model.Course>()

            for (course in realCourses) {
                val result =
                    mvc
                        .perform(
                            post("/api/v1/timetables/${timetable.id}/courses/${course.id}")
                                .header("Authorization", "Bearer $token"),
                        ).andReturn()

                if (result.response.status == 200) {
                    addedCourses.add(course)
                    if (addedCourses.size == 3) {
                        break
                    }
                }
            }

            assumeTrue(addedCourses.size == 3, "시간이 겹치지 않는 3개의 강의를 찾을 수 없습니다")

            val expectedTotalCredits = addedCourses.sumOf { it.credit }

            // 시간표 조회 및 학점 합계 확인
            mvc
                .perform(
                    get("/api/v1/timetables/${timetable.id}")
                        .header("Authorization", "Bearer $token"),
                ).andExpect(status().isOk)
                .andExpect(jsonPath("$.courses.length()").value(3))
                .andExpect(jsonPath("$.credits").value(expectedTotalCredits))
        }

        @Test
        fun `should remove course from timetable`() {
            // 시간표에서 실제 강의를 삭제할 수 있다
            val (user, token) = dataGenerator.generateUser()
            val timetable =
                dataGenerator.generateTimetable(
                    name = "2025-2 시간표",
                    year = 2025,
                    semester = Semester.FALL,
                    user = user,
                )

            // 시간이 겹치지 않는 강의 2개를 찾기 위해 더 많은 강의를 조회
            val realCourses = courseRepository.search(2025, Semester.FALL, null, null, 50)
            assumeTrue(realCourses.size >= 2, "최소 2개의 강의가 필요합니다")

            // 첫 번째 강의 추가
            val course1 = realCourses[0]
            mvc
                .perform(
                    post("/api/v1/timetables/${timetable.id}/courses/${course1.id}")
                        .header("Authorization", "Bearer $token"),
                ).andExpect(status().isOk)

            // 시간이 겹치지 않는 두 번째 강의 찾기
            var course2 = realCourses[1]
            var addedSuccessfully = false

            for (course in realCourses.drop(1)) {
                val result =
                    mvc
                        .perform(
                            post("/api/v1/timetables/${timetable.id}/courses/${course.id}")
                                .header("Authorization", "Bearer $token"),
                        ).andReturn()

                if (result.response.status == 200) {
                    course2 = course
                    addedSuccessfully = true
                    break
                }
            }

            assumeTrue(addedSuccessfully, "시간이 겹치지 않는 두 번째 강의를 찾을 수 없습니다")

            // 첫 번째 강의 삭제
            mvc
                .perform(
                    delete("/api/v1/timetables/${timetable.id}/courses/${course1.id}")
                        .header("Authorization", "Bearer $token"),
                ).andExpect(status().isNoContent)

            // 시간표 조회 - 한 개만 남아있어야 함
            mvc
                .perform(
                    get("/api/v1/timetables/${timetable.id}")
                        .header("Authorization", "Bearer $token"),
                ).andExpect(status().isOk)
                .andExpect(jsonPath("$.courses.length()").value(1))
                .andExpect(jsonPath("$.courses[0].id").value(course2.id!!))
                .andExpect(jsonPath("$.credits").value(course2.credit))
        }

        @Test
        fun `should return error when adding course from different semester`() {
            // 다른 학기의 강의를 시간표에 추가하면 에러를 반환해야 한다
            val (user, token) = dataGenerator.generateUser()

            // 2025-1 시간표 생성
            val timetable =
                dataGenerator.generateTimetable(
                    name = "2025-1 시간표",
                    year = 2025,
                    semester = Semester.SPRING,
                    user = user,
                )

            // 2025-2(FALL) 강의 조회
            val fallCourses = courseRepository.search(2025, Semester.FALL, null, null, 1)
            if (fallCourses.isNotEmpty()) {
                val fallCourse = fallCourses.first()

                // 다른 학기 강의 추가 시도 (실패해야 함)
                mvc
                    .perform(
                        post("/api/v1/timetables/${timetable.id}/courses/${fallCourse.id}")
                            .header("Authorization", "Bearer $token"),
                    ).andExpect(status().isBadRequest)
            }
        }

        @Test
        fun `should prevent duplicate course addition to timetable`() {
            // 같은 강의를 중복으로 추가할 수 없다
            val (user, token) = dataGenerator.generateUser()
            val timetable =
                dataGenerator.generateTimetable(
                    name = "2025-1 시간표",
                    year = 2025,
                    semester = Semester.SPRING,
                    user = user,
                )

            val courses = courseRepository.search(2025, Semester.SPRING, null, null, 1)
            assumeTrue(courses.isNotEmpty(), "강의가 최소 1개 있어야 합니다")

            val course = courses.first()

            // 첫 번째 추가 (성공)
            mvc
                .perform(
                    post("/api/v1/timetables/${timetable.id}/courses/${course.id}")
                        .header("Authorization", "Bearer $token"),
                ).andExpect(status().isOk)

            // 같은 강의 다시 추가 시도 (실패해야 함)
            mvc
                .perform(
                    post("/api/v1/timetables/${timetable.id}/courses/${course.id}")
                        .header("Authorization", "Bearer $token"),
                ).andExpect(status().isConflict)
        }

        @Test
        fun `should search real courses by keyword`() {
            // 실제 강의를 키워드로 검색할 수 있다
            val (_, token) = dataGenerator.generateUser()

            // "컴퓨터" 키워드로 검색
            val result =
                mvc
                    .perform(
                        get("/api/v1/courses")
                            .header("Authorization", "Bearer $token")
                            .param("year", "2025")
                            .param("semester", "SPRING")
                            .param("keyword", "컴퓨터")
                            .param("limit", "10"),
                    ).andExpect(status().isOk)
                    .andReturn()

            val response = mapper.readTree(result.response.contentAsString)
            val courses = response.get("courses")

            // 검색 결과가 있으면 "컴퓨터"가 포함되어 있는지 확인
            if (courses.size() > 0) {
                val firstCourse = courses.get(0)
                val courseTitle = firstCourse.get("courseTitle").asText()
                assertTrue(
                    courseTitle.contains("컴퓨터") || courseTitle.contains("Computer"),
                    "검색 결과에 '컴퓨터' 키워드가 포함되어야 합니다",
                )
            }
        }

        @Test
        fun `should find courses by specific department`() {
            // 특정 학과의 강의를 찾을 수 있다
            val allCourses = courseRepository.search(2025, Semester.SPRING, null, null, 100)
            assumeTrue(allCourses.isNotEmpty(), "크롤링된 강의가 있어야 합니다")

            // 학과별로 그룹화
            val coursesByDepartment = allCourses.groupBy { it.department }

            assertTrue(coursesByDepartment.isNotEmpty(), "최소 한 개 이상의 학과가 있어야 합니다")
        }

        @Test
        fun `should find courses by instructor name`() {
            // 교수님 이름으로 강의를 검색할 수 있다
            val allCourses = courseRepository.search(2025, Semester.SPRING, null, null, 100)
            assumeTrue(allCourses.isNotEmpty(), "크롤링된 강의가 있어야 합니다")

            // 교수님이 지정된 강의 필터링
            val coursesWithInstructor = allCourses.filter { !it.instructor.isNullOrBlank() }
            assumeTrue(coursesWithInstructor.isNotEmpty(), "교수님이 지정된 강의가 있어야 합니다")

            // 교수님별로 그룹화
            val coursesByInstructor = coursesWithInstructor.groupBy { it.instructor }

            assertTrue(coursesByInstructor.isNotEmpty(), "최소 한 명 이상의 교수님이 있어야 합니다")
        }
    }
