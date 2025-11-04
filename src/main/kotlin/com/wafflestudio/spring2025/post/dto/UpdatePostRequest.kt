package com.wafflestudio.spring2025.post.dto

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "게시글 수정 요청")
data class UpdatePostRequest(
    @Schema(description = "게시글 제목 (수정하지 않으려면 null)", example = "수정된 제목")
    val title: String?,
    @Schema(description = "게시글 내용 (수정하지 않으려면 null)", example = "수정된 내용입니다.")
    val content: String?,
)
