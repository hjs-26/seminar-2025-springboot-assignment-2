package com.wafflestudio.spring2025.post.controller

import com.wafflestudio.spring2025.post.dto.CreatePostRequest
import com.wafflestudio.spring2025.post.dto.CreatePostResponse
import com.wafflestudio.spring2025.post.dto.PostPagingResponse
import com.wafflestudio.spring2025.post.dto.UpdatePostRequest
import com.wafflestudio.spring2025.post.dto.UpdatePostResponse
import com.wafflestudio.spring2025.post.dto.core.PostDto
import com.wafflestudio.spring2025.post.service.PostService
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
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.Instant

@RestController
@Tag(name = "Post", description = "게시글 관리 API")
class PostController(
    private val postService: PostService,
) {
    @Operation(summary = "게시글 생성", description = "특정 게시판에 새로운 게시글을 작성합니다")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "201", description = "게시글 생성 성공"),
            ApiResponse(responseCode = "400", description = "잘못된 요청 (빈 제목 또는 내용)"),
            ApiResponse(responseCode = "404", description = "게시판을 찾을 수 없음"),
        ],
    )
    @PostMapping("/api/v1/boards/{boardId}/posts")
    fun create(
        @Parameter(hidden = true) @LoggedInUser user: User,
        @Parameter(description = "게시판 ID") @PathVariable boardId: Long,
        @RequestBody createRequest: CreatePostRequest,
    ): ResponseEntity<CreatePostResponse> {
        val postDto =
            postService.create(
                title = createRequest.title,
                content = createRequest.content,
                user = user,
                boardId = boardId,
            )
        return ResponseEntity.status(HttpStatus.CREATED).body(postDto)
    }

    @Operation(summary = "게시글 목록 조회", description = "특정 게시판의 게시글 목록을 페이지네이션하여 조회합니다")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "게시글 목록 조회 성공"),
            ApiResponse(responseCode = "404", description = "게시판을 찾을 수 없음"),
        ],
    )
    @GetMapping("/api/v1/boards/{boardId}/posts")
    fun page(
        @Parameter(description = "게시판 ID") @PathVariable boardId: Long,
        @Parameter(description = "다음 페이지 커서 (생성 시간)") @RequestParam(value = "nextCreatedAt", required = false) nextCreatedAt: Long?,
        @Parameter(description = "다음 페이지 커서 (ID)") @RequestParam(value = "nextId", required = false) nextId: Long?,
        @Parameter(description = "페이지당 항목 수") @RequestParam(value = "limit", defaultValue = "20") limit: Int,
    ): ResponseEntity<PostPagingResponse> {
        val postPagingResponse =
            postService.pageByBoardId(
                boardId,
                nextCreatedAt?.let { Instant.ofEpochMilli(it) },
                nextId,
                limit,
            )
        return ResponseEntity.ok(postPagingResponse)
    }

    @Operation(summary = "게시글 단건 조회", description = "게시글 ID로 특정 게시글의 상세 정보를 조회합니다")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "게시글 조회 성공"),
            ApiResponse(responseCode = "404", description = "게시글을 찾을 수 없음"),
        ],
    )
    @GetMapping("/api/v1/posts/{id}")
    fun get(
        @Parameter(description = "게시글 ID") @PathVariable id: Long,
    ): ResponseEntity<PostDto> {
        val postDto = postService.get(id)
        return ResponseEntity.ok(postDto)
    }

    @Operation(summary = "게시글 수정", description = "작성한 게시글의 제목 또는 내용을 수정합니다")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "게시글 수정 성공"),
            ApiResponse(responseCode = "400", description = "잘못된 요청 (빈 제목 또는 내용)"),
            ApiResponse(responseCode = "403", description = "권한 없음 (다른 사용자의 게시글)"),
            ApiResponse(responseCode = "404", description = "게시글을 찾을 수 없음"),
        ],
    )
    @PatchMapping("/api/v1/posts/{id}")
    fun update(
        @Parameter(description = "게시글 ID") @PathVariable id: Long,
        @Parameter(hidden = true) @LoggedInUser user: User,
        @RequestBody updateRequest: UpdatePostRequest,
    ): ResponseEntity<UpdatePostResponse> {
        val postDto =
            postService.update(
                postId = id,
                title = updateRequest.title,
                content = updateRequest.content,
                user = user,
            )
        return ResponseEntity.ok(postDto)
    }

    @Operation(summary = "게시글 삭제", description = "작성한 게시글을 삭제합니다")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "204", description = "게시글 삭제 성공"),
            ApiResponse(responseCode = "403", description = "권한 없음 (다른 사용자의 게시글)"),
            ApiResponse(responseCode = "404", description = "게시글을 찾을 수 없음"),
        ],
    )
    @DeleteMapping("/api/v1/posts/{id}")
    fun delete(
        @Parameter(description = "게시글 ID") @PathVariable id: Long,
        @Parameter(hidden = true) @LoggedInUser user: User,
    ): ResponseEntity<Unit> {
        postService.delete(id, user)
        return ResponseEntity.noContent().build()
    }
}
