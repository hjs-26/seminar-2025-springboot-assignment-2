package com.wafflestudio.spring2025.comment.controller

import com.wafflestudio.spring2025.comment.dto.CreateCommentRequest
import com.wafflestudio.spring2025.comment.dto.CreateCommentResponse
import com.wafflestudio.spring2025.comment.dto.UpdateCommentRequest
import com.wafflestudio.spring2025.comment.dto.UpdateCommentResponse
import com.wafflestudio.spring2025.comment.dto.core.CommentDto
import com.wafflestudio.spring2025.comment.service.CommentService
import com.wafflestudio.spring2025.user.LoggedInUser
import com.wafflestudio.spring2025.user.model.User
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/posts/{postId}/comments")
@Tag(name = "Comment", description = "댓글 관리 API")
class CommentController(
    private val commentService: CommentService,
) {
    @Operation(
        summary = "댓글 목록 조회",
        description = """
            특정 게시글의 모든 댓글을 조회합니다.
            
            **정렬:** 생성일시 내림차순 (최신순)
        """,
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "댓글 목록 조회 성공"),
            ApiResponse(responseCode = "404", description = "게시글을 찾을 수 없음"),
        ],
    )
    @GetMapping
    fun list(
        @Parameter(
            description = "게시글 ID",
            example = "123",
        ) @PathVariable postId: Long,
    ): ResponseEntity<List<CommentDto>> {
        val comments = commentService.list(postId)
        return ResponseEntity.ok(comments)
    }

    @Operation(summary = "댓글 작성", description = "특정 게시글에 댓글을 작성합니다")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "201", description = "댓글 작성 성공"),
            ApiResponse(responseCode = "400", description = "잘못된 요청 (빈 내용)"),
            ApiResponse(responseCode = "404", description = "게시글을 찾을 수 없음"),
        ],
    )
    @PostMapping
    fun create(
        @Parameter(
            description = "게시글 ID",
            example = "123",
        ) @PathVariable postId: Long,
        @RequestBody createRequest: CreateCommentRequest,
        @Parameter(hidden = true) @LoggedInUser user: User,
    ): ResponseEntity<CreateCommentResponse> {
        val comment =
            commentService.create(
                postId = postId,
                content = createRequest.content,
                user = user,
            )
        return ResponseEntity.status(HttpStatus.CREATED).body(comment)
    }

    @Operation(summary = "댓글 수정", description = "작성한 댓글의 내용을 수정합니다")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "댓글 수정 성공"),
            ApiResponse(responseCode = "400", description = "잘못된 요청 (빈 내용)"),
            ApiResponse(responseCode = "403", description = "권한 없음 (다른 사용자의 댓글)"),
            ApiResponse(responseCode = "404", description = "댓글을 찾을 수 없음"),
        ],
    )
    @PutMapping("/{id}")
    fun update(
        @Parameter(
            description = "게시글 ID",
            example = "123",
        ) @PathVariable postId: Long,
        @Parameter(
            description = "댓글 ID",
            example = "456",
        ) @PathVariable id: Long,
        @Parameter(hidden = true) @LoggedInUser user: User,
        @RequestBody updateRequest: UpdateCommentRequest,
    ): ResponseEntity<UpdateCommentResponse> {
        val comment =
            commentService.update(
                commentId = id,
                postId = postId,
                content = updateRequest.content,
                user = user,
            )
        return ResponseEntity.ok(comment)
    }

    @Operation(summary = "댓글 삭제", description = "작성한 댓글을 삭제합니다")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "204", description = "댓글 삭제 성공"),
            ApiResponse(responseCode = "403", description = "권한 없음 (다른 사용자의 댓글)"),
            ApiResponse(responseCode = "404", description = "댓글을 찾을 수 없음"),
        ],
    )
    @DeleteMapping("/{id}")
    fun delete(
        @Parameter(
            description = "게시글 ID",
            example = "123",
        ) @PathVariable postId: Long,
        @Parameter(
            description = "댓글 ID",
            example = "456",
        ) @PathVariable id: Long,
        @Parameter(hidden = true) @LoggedInUser user: User,
    ): ResponseEntity<Unit> {
        commentService.delete(
            commentId = id,
            postId = postId,
            user = user,
        )
        return ResponseEntity.noContent().build()
    }
}
