package com.wafflestudio.spring2025.course.crawling.repository

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.wafflestudio.spring2025.common.enum.Semester
import com.wafflestudio.spring2025.course.crawling.data.CourseInfo
import com.wafflestudio.spring2025.course.crawling.utils.CourseUrlUtils
import org.springframework.core.io.buffer.PooledDataBuffer
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBody
import org.springframework.web.reactive.function.client.awaitExchange
import org.springframework.web.reactive.function.client.createExceptionAndAwait

@Component
class CourseFetchRepository(
    private val sugangSnuWebClient: WebClient,
    private val objectMapper: ObjectMapper,
) {
    companion object {
        // 강의 상세 정보 조회 엔드포인트
        const val SEARCH_POPUP_PATH = "/sugang/cc/cc101ajax.action"
        const val DEFAULT_SEARCH_POPUP_PARAMS = """t_profPersNo=&workType=+&sbjtSubhCd=000"""

        // 강의 엑셀 다운로드 엔드포인트
        const val LECTURE_EXCEL_DOWNLOAD_PATH = "/sugang/cc/cc100InterfaceExcel.action"
        val DEFAULT_LECTURE_EXCEL_DOWNLOAD_PARAMS =
            """
            seeMore=더보기&
            srchBdNo=&srchCamp=&srchOpenSbjtFldCd=&srchCptnCorsFg=&
            srchCurrPage=1&
            srchExcept=&srchGenrlRemoteLtYn=&srchIsEngSbjt=&
            srchIsPendingCourse=&srchLsnProgType=&srchMrksApprMthdChgPosbYn=&srchMrksGvMthd=&
            srchOpenUpDeptCd=&srchOpenMjCd=&srchOpenPntMax=&srchOpenPntMin=&srchOpenSbjtDayNm=&
            srchOpenSbjtNm=&srchOpenSbjtTm=&srchOpenSbjtTmNm=&srchOpenShyr=&srchOpenSubmattCorsFg=&
            srchOpenSubmattFgCd1=&srchOpenSubmattFgCd2=&srchOpenSubmattFgCd3=&srchOpenSubmattFgCd4=&
            srchOpenSubmattFgCd5=&srchOpenSubmattFgCd6=&srchOpenSubmattFgCd7=&srchOpenSubmattFgCd8=&
            srchOpenSubmattFgCd9=&srchOpenDeptCd=&srchOpenUpSbjtFldCd=&
            srchPageSize=9999&
            srchProfNm=&srchSbjtCd=&srchSbjtNm=&srchTlsnAplyCapaCntMax=&srchTlsnAplyCapaCntMin=&srchTlsnRcntMax=&srchTlsnRcntMin=&
            workType=EX
            """.trimIndent().replace("\n", "")
    }

    suspend fun getLectureInfo(
        year: Int,
        semester: Semester,
        courseNumber: String,
        lectureNumber: String,
    ): CourseInfo =
        sugangSnuWebClient
            .get()
            .uri { builder ->
                val semesterSearchString = CourseUrlUtils.convertSemesterToSearchString(semester)
                builder
                    .path(SEARCH_POPUP_PATH)
                    .query(DEFAULT_SEARCH_POPUP_PARAMS)
                    .queryParam("openSchyy", year)
                    .queryParam("openShtmFg", semesterSearchString.substring(0..9))
                    .queryParam("openDetaShtmFg", semesterSearchString.substring(10))
                    .queryParam("sbjtCd", courseNumber)
                    .queryParam("ltNo", lectureNumber)
                    .build()
            }.accept(MediaType.APPLICATION_JSON)
            .retrieve()
            .awaitBody<String>()
            .let { objectMapper.readValue<CourseInfo>(it) }

    suspend fun downloadCoursesExcel(
        year: Int,
        semester: Semester,
        language: String = "ko",
    ): PooledDataBuffer =
        sugangSnuWebClient
            .get()
            .uri { builder ->
                builder.run {
                    path(LECTURE_EXCEL_DOWNLOAD_PATH)
                    query(DEFAULT_LECTURE_EXCEL_DOWNLOAD_PARAMS)
                    queryParam("srchLanguage", language)
                    queryParam("srchOpenSchyy", year)
                    queryParam("srchOpenShtm", CourseUrlUtils.convertSemesterToSearchString(semester))
                    build()
                }
            }.accept(MediaType.TEXT_HTML)
            .awaitExchange {
                if (it.statusCode().is2xxSuccessful) {
                    it.awaitBody()
                } else {
                    throw it.createExceptionAndAwait()
                }
            }
}
