package com.wafflestudio.spring2025.post.repository

import com.wafflestudio.spring2025.post.model.Post
import com.wafflestudio.spring2025.post.model.PostWithUserAndBoard
import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param
import java.time.Instant

interface PostRepository : CrudRepository<Post, Long> {
    @Query(
        """
        SELECT
            p.id AS id,
            p.title AS title,
            p.content AS content,
            p.created_at AS created_at,
            p.updated_at AS updated_at,
            u.id AS user_id,
            u.username AS user_username,
            b.id AS board_id,
            b.name AS board_name
        FROM posts p
        JOIN users u ON p.user_id = u.id
        JOIN boards b ON p.board_id = b.id
        WHERE p.id = :postId
        """,
    )
    fun findByIdWithUserAndBoard(
        @Param("postId") postId: Long,
    ): PostWithUserAndBoard?

    @Query(
        """
        SELECT
            p.id AS id,
            p.title AS title,
            p.content AS content,
            p.created_at AS created_at,
            p.updated_at AS updated_at,
            u.id AS user_id,
            u.username AS user_username,
            b.id AS board_id,
            b.name AS board_name
        FROM posts p
        JOIN users u ON p.user_id = u.id
        JOIN boards b ON p.board_id = b.id
        WHERE p.board_id = :boardId AND
              (:nextCreatedAt IS NULL OR (p.created_at, p.id) < (:nextCreatedAt, :nextId))
        GROUP BY p.id, u.id, b.id
        ORDER BY p.created_at DESC, p.id DESC
        LIMIT :limit
        """,
    )
    fun findByBoardIdWithCursor(
        @Param("boardId") boardId: Long,
        @Param("nextCreatedAt") nextCreatedAt: Instant?,
        @Param("nextId") nextId: Long?,
        @Param("limit") limit: Int,
    ): List<PostWithUserAndBoard>

    @Query("SELECT * FROM posts WHERE id = :id FOR UPDATE")
    fun findByIdWithWriteLock(id: Long): Post?
}
