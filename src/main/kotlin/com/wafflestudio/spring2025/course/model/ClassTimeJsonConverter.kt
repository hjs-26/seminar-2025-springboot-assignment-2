package com.wafflestudio.spring2025.course.model

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.wafflestudio.spring2025.course.crawling.ClassPlaceAndTime
import org.springframework.core.convert.converter.Converter
import org.springframework.data.convert.ReadingConverter
import org.springframework.data.convert.WritingConverter
import org.springframework.stereotype.Component

@ReadingConverter
@Component
class ClassTimeJsonReadConverter(
    private val objectMapper: ObjectMapper,
) : Converter<String, List<ClassPlaceAndTime>> {
    override fun convert(source: String): List<ClassPlaceAndTime> = objectMapper.readValue(source)
}

@WritingConverter
@Component
class ClassTimeJsonWriteConverter(
    private val objectMapper: ObjectMapper,
) : Converter<List<ClassPlaceAndTime>, String> {
    override fun convert(source: List<ClassPlaceAndTime>): String = objectMapper.writeValueAsString(source)
}
