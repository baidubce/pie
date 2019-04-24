package com.baidu.acu.pie.util

import org.joda.time.LocalTime
import org.junit.Test
import kotlin.test.assertEquals

/**
 * DateTimeParserTest
 *
 * @author Shu Lingjie
 */
class DateTimeParserTest {
    @Test
    fun `test parseLocalTime`() {
        val `time with out hour` = "01:23.456"

        var localTime = DateTimeParser.parseLocalTime(`time with out hour`)
        assertEquals(localTime?.minuteOfHour, 1)
        assertEquals(localTime?.secondOfMinute, 23)
        assertEquals(localTime?.millisOfSecond, 456)

        val `time with hour` = "01:23:45.678"
        localTime = DateTimeParser.parseLocalTime(`time with hour`)
        assertEquals(localTime?.hourOfDay, 1)
        assertEquals(localTime?.minuteOfHour, 23)
        assertEquals(localTime?.secondOfMinute, 45)
        assertEquals(localTime?.millisOfSecond, 678)

        val `special time` = "01:00.40"
        localTime = DateTimeParser.parseLocalTime(`special time`)
        assertEquals(localTime?.minuteOfHour, 1)
        assertEquals(localTime?.secondOfMinute, 0)
        assertEquals(localTime?.millisOfSecond, 400)
    }

    @Test
    fun `test parseLocalTime with exception`() {
        val localTime = DateTimeParser.parseLocalTime("")
        assertEquals(localTime, LocalTime.MIDNIGHT)
    }

}