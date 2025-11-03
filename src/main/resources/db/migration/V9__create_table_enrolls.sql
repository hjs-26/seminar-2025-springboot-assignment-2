CREATE TABLE IF NOT EXISTS enrolls
(
    id         BIGINT AUTO_INCREMENT
    PRIMARY KEY,
    timetable_id BIGINT NOT NULL,
    course_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (timetable_id) REFERENCES timetables(id) ON DELETE CASCADE,
    FOREIGN KEY (course_id) REFERENCES courses(id) ON DELETE CASCADE,

    UNIQUE KEY uq_timetable_course (timetable_id, course_id)
    );
