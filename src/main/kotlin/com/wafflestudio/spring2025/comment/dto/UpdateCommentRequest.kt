package com.wafflestudio.spring2025.comment.dto

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "댓글 수정 요청")
data class UpdateCommentRequest(
    @Schema(description = "댓글 내용", example = "수정된 댓글입니다.", required = true)
    val content: String,
)
