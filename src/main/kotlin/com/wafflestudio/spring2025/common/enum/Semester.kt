package com.wafflestudio.spring2025.common.enum

import org.springframework.core.convert.converter.Converter
import org.springframework.data.convert.ReadingConverter
import org.springframework.data.convert.WritingConverter
import org.springframework.stereotype.Component

enum class Semester {
    SPRING,
    SUMMER,
    FALL,
    WINTER,
}

@ReadingConverter
@Component
class SemesterReadConverter : Converter<String, Semester> {
    override fun convert(source: String): Semester = Semester.valueOf(source.uppercase())
}

@WritingConverter
@Component
class SemesterWriteConverter : Converter<Semester, String> {
    override fun convert(source: Semester): String = source.name
}
