package com.wafflestudio.spring2025.board.controller

import com.wafflestudio.spring2025.board.dto.CreateBoardRequest
import com.wafflestudio.spring2025.board.dto.CreateBoardResponse
import com.wafflestudio.spring2025.board.dto.ListBoardResponse
import com.wafflestudio.spring2025.board.service.BoardService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/boards")
@Tag(name = "Board", description = "게시판 관리 API")
class BoardController(
    private val boardService: BoardService,
) {
    @Operation(summary = "게시판 생성", description = "새로운 게시판을 생성합니다")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "201", description = "게시판 생성 성공"),
            ApiResponse(responseCode = "400", description = "잘못된 요청 (빈 이름)"),
            ApiResponse(responseCode = "409", description = "중복된 게시판 이름"),
        ],
    )
    @PostMapping
    fun create(
        @RequestBody createRequest: CreateBoardRequest,
    ): ResponseEntity<CreateBoardResponse> {
        val board = boardService.create(createRequest.name)
        return ResponseEntity.status(HttpStatus.CREATED).body(board)
    }

    @Operation(summary = "게시판 목록 조회", description = "모든 게시판 목록을 조회합니다")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "게시판 목록 조회 성공"),
        ],
    )
    @GetMapping
    fun list(): ResponseEntity<ListBoardResponse> {
        val boards = boardService.list()
        return ResponseEntity.ok(boards)
    }
}
