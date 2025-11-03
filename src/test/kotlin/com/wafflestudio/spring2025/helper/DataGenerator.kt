package com.wafflestudio.spring2025.helper

import com.wafflestudio.spring2025.board.model.Board
import com.wafflestudio.spring2025.board.repository.BoardRepository
import com.wafflestudio.spring2025.comment.model.Comment
import com.wafflestudio.spring2025.comment.repository.CommentRepository
import com.wafflestudio.spring2025.common.enum.Semester
import com.wafflestudio.spring2025.course.crawling.ClassPlaceAndTime
import com.wafflestudio.spring2025.course.crawling.DayOfWeek
import com.wafflestudio.spring2025.course.model.Course
import com.wafflestudio.spring2025.course.repository.CourseRepository
import com.wafflestudio.spring2025.post.model.Post
import com.wafflestudio.spring2025.post.repository.PostRepository
import com.wafflestudio.spring2025.timetable.model.Timetable
import com.wafflestudio.spring2025.timetable.repository.TimetableRepository
import com.wafflestudio.spring2025.user.JwtTokenProvider
import com.wafflestudio.spring2025.user.model.User
import com.wafflestudio.spring2025.user.repository.UserRepository
import org.mindrot.jbcrypt.BCrypt
import org.springframework.stereotype.Component
import kotlin.random.Random

@Component
class DataGenerator(
    private val userRepository: UserRepository,
    private val boardRepository: BoardRepository,
    private val postRepository: PostRepository,
    private val commentRepository: CommentRepository,
    private val timetableRepository: TimetableRepository,
    private val courseRepository: CourseRepository,
    private val jwtTokenProvider: JwtTokenProvider,
) {
    fun generateUser(
        username: String? = null,
        password: String? = null,
    ): Pair<User, String> {
        val user =
            userRepository.save(
                User(
                    username = username ?: "user-${Random.Default.nextInt(1000000)}",
                    password = BCrypt.hashpw(password ?: "password-${Random.Default.nextInt(1000000)}", BCrypt.gensalt()),
                ),
            )
        return user to jwtTokenProvider.createToken(user.username)
    }

    fun generateBoard(name: String? = null): Board {
        val board =
            boardRepository.save(
                Board(
                    name = name ?: "board-${Random.Default.nextInt(1000000)}",
                ),
            )
        return board
    }

    fun generatePost(
        title: String? = null,
        content: String? = null,
        user: User? = null,
        board: Board? = null,
    ): Post {
        val post =
            postRepository.save(
                Post(
                    title = title ?: "title-${Random.Default.nextInt(1000000)}",
                    content = content ?: "content-${Random.Default.nextInt(1000000)}",
                    userId = (user ?: generateUser().first).id!!,
                    boardId = (board ?: generateBoard()).id!!,
                ),
            )
        return post
    }

    fun generateComment(
        content: String? = null,
        user: User? = null,
        post: Post? = null,
    ): Comment {
        val comment =
            commentRepository.save(
                Comment(
                    content = content ?: "content-${Random.Default.nextInt(1000000)}",
                    userId = (user ?: generateUser().first).id!!,
                    postId = (post ?: generatePost()).id!!,
                ),
            )
        return comment
    }

    fun generateTimetable(
        name: String? = null,
        year: Int? = null,
        semester: Semester? = null,
        user: User? = null,
    ): Timetable {
        val timetable =
            timetableRepository.save(
                Timetable(
                    userId = (user ?: generateUser().first).id!!,
                    name = name ?: "timetable-${Random.Default.nextInt(1000000)}",
                    year = year ?: 2025,
                    semester = semester ?: Semester.FALL,
                ),
            )
        return timetable
    }

    fun generateCourse(
        year: Int? = null,
        semester: Semester? = null,
        classification: String? = null,
        college: String? = null,
        department: String? = null,
        academicCourse: String? = null,
        academicYear: String? = null,
        courseNumber: String? = null,
        lectureNumber: String? = null,
        courseTitle: String? = null,
        credit: Long? = null,
        instructor: String? = null,
        classTimeJson: List<ClassPlaceAndTime>? = null,
    ): Course {
        val course =
            courseRepository.save(
                Course(
                    year = year ?: 2025,
                    semester = semester ?: Semester.FALL,
                    classification = classification ?: "전공필수",
                    college = college ?: "공과대학",
                    department = department ?: "컴퓨터공학부",
                    academicCourse = academicCourse ?: "학사",
                    academicYear = academicYear ?: "1학년",
                    courseNumber = courseNumber ?: "M1522.${Random.Default.nextInt(100000, 999999)}",
                    lectureNumber = lectureNumber ?: "00${Random.Default.nextInt(1, 9)}",
                    courseTitle = courseTitle ?: "강의-${Random.Default.nextInt(1000000)}",
                    credit = credit ?: 3L,
                    instructor = instructor ?: "교수-${Random.Default.nextInt(1000)}",
                    classTimeJson =
                        classTimeJson
                            ?: listOf(
                                ClassPlaceAndTime(
                                    day = DayOfWeek.MONDAY,
                                    place = "301-101",
                                    startMinute = 540,
                                    endMinute = 630,
                                ),
                            ),
                ),
            )
        return course
    }
}
