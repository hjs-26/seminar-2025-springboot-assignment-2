package com.wafflestudio.spring2025.helper

import com.wafflestudio.spring2025.board.model.Board
import com.wafflestudio.spring2025.board.repository.BoardRepository
import com.wafflestudio.spring2025.comment.model.Comment
import com.wafflestudio.spring2025.comment.repository.CommentRepository
import com.wafflestudio.spring2025.common.enum.Semester
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
}
