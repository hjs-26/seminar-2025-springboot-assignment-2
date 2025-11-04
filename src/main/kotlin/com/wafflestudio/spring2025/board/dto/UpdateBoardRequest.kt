package com.wafflestudio.spring2025.board.dto

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "게시판 수정 요청")
data class UpdateBoardRequest(
    @Schema(description = "게시판 이름", example = "새로운 게시판", required = true)
    val name: String,
)
