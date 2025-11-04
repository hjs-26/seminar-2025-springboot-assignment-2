package com.wafflestudio.spring2025.board.dto

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "게시판 생성 요청")
data class CreateBoardRequest(
    @Schema(description = "게시판 이름", example = "자유게시판", required = true)
    val name: String,
)
