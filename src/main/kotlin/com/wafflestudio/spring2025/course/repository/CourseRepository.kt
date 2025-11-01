package com.wafflestudio.spring2025.course.repository

import com.wafflestudio.spring2025.course.api.CourseApi
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.springframework.core.io.buffer.PooledDataBuffer
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.awaitBody
import org.springframework.web.reactive.function.client.awaitExchange
import org.springframework.web.reactive.function.client.createExceptionAndAwait

@Component
class CourseRepository(
    private val CourseApi: CourseApi,
    private val objectMapper: ObjectMapper,
) {
    companion object {
        const val SUGANG_SNU_COURSEBOOK_PATH = "/sugang/cc/cc100ajax.action"
        const val DEFAULT_COURSEBOOK_PARAMS = "openUpDeptCd=&openDeptCd="
        const val SUGANG_SNU_SEARCH_PATH = "/sugang/cc/cc100InterfaceSrch.action"
        const val DEFAULT_SEARCH_PAGE_PARAMS = "workType=S&sortKey=&sortOrder="
        const val SUGANG_SNU_SEARCH_POPUP_PATH = "/sugang/cc/cc101ajax.action"
        const val DEFAULT_SEARCH_POPUP_PARAMS = """t_profPersNo=&workType=+&sbjtSubhCd=000"""
        const val SUGANG_SNU_LECTURE_EXCEL_DOWNLOAD_PATH = "/sugang/cc/cc100InterfaceExcel.action"
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

    suspend fun getCourses(
        language: String = "ko",
    ): PooledDataBuffer =
        CourseApi
            .get()
            .uri { builder ->
                builder.run {
                    path(SUGANG_SNU_LECTURE_EXCEL_DOWNLOAD_PATH)
                    query(DEFAULT_LECTURE_EXCEL_DOWNLOAD_PARAMS)
                    queryParam("srchLanguage", language)
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