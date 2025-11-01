CREATE TABLE IF NOT EXISTS courses
(
    id         BIGINT AUTO_INCREMENT
        PRIMARY KEY,
    year       INT          NOT NULL,
    semester   ENUM('SPRING', 'SUMMER', 'FALL', 'WINTER') NOT NULL,

    -- 강의 상세 정보
    classification VARCHAR(50),                 -- 교과구분 (model: classification)
    college VARCHAR(100),                       -- 개설대학 (model: 없음)
    department VARCHAR(100),                    -- 개설학과 (model: department)
    academic_course VARCHAR(50),                -- 이수과정 (model: 없음)
    academic_year VARCHAR(20),                  -- 학년 (model: academicYear)
    course_number VARCHAR(20) NOT NULL,         -- 교과목번호 (model: courseNumber)
    lecture_number VARCHAR(10) NOT NULL,        -- 강좌번호 (model: lectureNumber)
    course_title VARCHAR(255) NOT NULL,         -- 교과목명 (model: courseTitle)
    credit BIGINT NOT NULL,                     -- 학점 수 (model: credit)
    instructor VARCHAR(100),                    -- 담당교수 (model: instructor)

    -- 수업 시간 및 강의실 정보 (JSON 형식으로 저장)
    -- (model: classPlaceAndTimes)
    -- 예: [{"day": "MON", "start_time": "10:00", "end_time": "11:50", "room": "301-101"}, ...]
    class_time_json JSON,

    -- 특정 학년도의 강의는 교과목번호와 강좌번호로 유일하게 식별됩니다.
    UNIQUE KEY uq__year__semester__course_number__lecture_number (year, semester, course_number, lecture_number)
);

-- 강의 검색을 위한 인덱스
CREATE INDEX idx_courses_1 ON courses (year, semester);
CREATE INDEX idx_courses_2 ON courses (course_title);
CREATE INDEX idx_courses_3 ON courses (instructor)