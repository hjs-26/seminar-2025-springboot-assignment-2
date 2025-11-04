package com.wafflestudio.spring2025.post.dto

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "게시글 생성 요청")
data class CreatePostRequest(
    @Schema(description = "게시글 제목", example = "첫 번째 게시글", required = true)
    val title: String,
    @Schema(description = "게시글 내용", example = "안녕하세요. 첫 게시글입니다.", required = true)
    val content: String,
)
