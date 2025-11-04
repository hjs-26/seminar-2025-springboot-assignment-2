package com.wafflestudio.spring2025.course.repository

import com.wafflestudio.spring2025.common.enum.Semester
import com.wafflestudio.spring2025.course.model.Course
import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param

interface CourseRepository : CrudRepository<Course, Long> {
    @Query(
        """
        SELECT *
        FROM courses c
        WHERE c.year = :year
          AND c.semester = :semester
          AND (
              :keyword IS NULL 
              OR c.course_title LIKE CONCAT('%', :keyword, '%') 
              OR c.instructor LIKE CONCAT('%', :keyword, '%')
          )
          AND (:nextId IS NULL OR c.id < :nextId)
        ORDER BY c.id DESC
        LIMIT :limit
    
    """,
    )
    fun search(
        @Param("year") year: Int,
        @Param("semester") semester: Semester,
        @Param("keyword") keyword: String?,
        @Param("nextId") nextId: Long?,
        @Param("limit") limit: Int,
    ): List<Course>

    @Query(
        """
        SELECT *
        FROM courses c
        INNER JOIN enrolls e ON e.course_id = c.id
        WHERE e.timetable_id = :timetableId
    """,
    )
    fun findByTimetableId(
        @Param("timetableId") timetableId: Long,
    ): List<Course>
}
