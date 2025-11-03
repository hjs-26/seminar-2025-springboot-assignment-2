package com.wafflestudio.spring2025.course.crawling.utils

import com.wafflestudio.spring2025.course.crawling.ClassPlaceAndTime
import com.wafflestudio.spring2025.course.crawling.CourseClassTime
import com.wafflestudio.spring2025.course.crawling.DayOfWeek
import org.slf4j.LoggerFactory
import kotlin.text.get

object CourseClassTimeUtils {
    private val log = LoggerFactory.getLogger(javaClass)
    private val classTimeRegEx =
        """^(?<day>[월화수목금토일])\((?<startHour>\d{2}):(?<startMinute>\d{2})~(?<endHour>\d{2}):(?<endMinute>\d{2})\)$""".toRegex()

    fun convertTextToClassTimeObject(
        classTimesTexts: List<String>,
        locationsTexts: List<String>,
    ): List<ClassPlaceAndTime> =
        runCatching {
            val courseClassTimes =
                classTimesTexts
                    .filter { it.isNotBlank() }
                    .map(CourseClassTimeUtils::parseCourseClassTime)
            val locationTexts =
                locationsTexts.let { locationText ->
                    when (locationText.size) {
                        courseClassTimes.size -> locationText
                        1 -> List(courseClassTimes.size) { locationText.first() }
                        0 -> List(courseClassTimes.size) { "" }
                        else -> throw RuntimeException("locations does not match with times $classTimesTexts $locationsTexts")
                    }
                }
            courseClassTimes
                .zip(locationTexts)
                .groupBy({ it.first }, { it.second })
                .map { (courseClassTime, locationTexts) ->
                    ClassPlaceAndTime(
                        day = DayOfWeek.getByKoreanText(courseClassTime.dayOfWeek)!!,
                        place = locationTexts.joinToString("/"),
                        startMinute = courseClassTime.startHour.toInt() * 60 + courseClassTime.startMinute.toInt(),
                        endMinute = courseClassTime.endHour.toInt() * 60 + courseClassTime.endMinute.toInt(),
                    )
                }.sortedWith(compareBy({ it.day.value }, { it.startMinute }))
        }.getOrElse {
            log.error("classtime으로 변환 실패 (time: {}, location: {})", classTimesTexts, locationsTexts)
            emptyList()
        }

    private fun parseCourseClassTime(classTime: String): CourseClassTime =
        classTimeRegEx.find(classTime)!!.groups.let { matchResult ->
            CourseClassTime(
                dayOfWeek = matchResult["day"]!!.value,
                startHour = matchResult["startHour"]!!.value,
                startMinute = matchResult["startMinute"]!!.value,
                endHour = matchResult["endHour"]!!.value,
                endMinute = matchResult["endMinute"]!!.value,
            )
        }
}
