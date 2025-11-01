CREATE TABLE IF NOT EXISTS timetables
(
    id         BIGINT AUTO_INCREMENT
    PRIMARY KEY,
    user_id    BIGINT       NOT NULL,
    name       VARCHAR(255) NOT NULL,
    year       INT          NOT NULL,
    semester   ENUM('SPRING', 'SUMMER', 'FALL', 'WINTER') NOT NULL,
    created_at TIMESTAMP(6)  NOT NULL,
    updated_at TIMESTAMP(6)  NOT NULL,

    CONSTRAINT timetables__fk__user_id
    FOREIGN KEY (user_id) REFERENCES users (id),

    UNIQUE KEY uq__user_id__year__semester__name (user_id, year, semester, name)
    );

CREATE INDEX idx_timetables_1 ON timetables (user_id);