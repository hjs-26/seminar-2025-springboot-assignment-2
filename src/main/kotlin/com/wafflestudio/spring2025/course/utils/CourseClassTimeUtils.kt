package com.wafflestudio.spring2025.course.utils

import com.wafflestudio.spring2025.course.model.DayOfWeek
import com.wafflestudio.spring2025.course.model.ClassPlaceAndTime
import com.wafflestudio.spring2025.course.model.CourseClassTime
import org.slf4j.LoggerFactory

object CourseClassTimeUtils {
    private val log = LoggerFactory.getLogger(javaClass)
    private val classTimeRegEx =
        """^(?<day>[월화수목금토일])\((?<startHour>\d{2}):(?<startMinute>\d{2})~(?<endHour>\d{2}):(?<endMinute>\d{2})\)$""".toRegex()

    fun convertTextToClassTimeObject(
        classTimesTexts: List<String>,
        locationsTexts: List<String>,
    ): List<ClassPlaceAndTime> =
        runCatching {
            val CourseClassTimes =
                classTimesTexts
                    .filter { it.isNotBlank() }
                    .map(CourseClassTimeUtils::parseCourseClassTime)
            val locationTexts =
                locationsTexts.let { locationText ->
                    when (locationText.size) {
                        CourseClassTimes.size -> locationText
                        1 -> List(CourseClassTimes.size) { locationText.first() }
                        0 -> List(CourseClassTimes.size) { "" }
                        else -> throw RuntimeException("locations does not match with times $classTimesTexts $locationsTexts")
                    }
                }
            CourseClassTimes
                .zip(locationTexts)
                .groupBy({ it.first }, { it.second })
                .map { (CourseClassTime, locationTexts) ->
                    ClassPlaceAndTime(
                        day = DayOfWeek.getByKoreanText(CourseClassTime.dayOfWeek)!!,
                        place = locationTexts.joinToString("/"),
                        startMinute = CourseClassTime.startHour.toInt() * 60 + CourseClassTime.startMinute.toInt(),
                        endMinute = CourseClassTime.endHour.toInt() * 60 + CourseClassTime.endMinute.toInt(),
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
