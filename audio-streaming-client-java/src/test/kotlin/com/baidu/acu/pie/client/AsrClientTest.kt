// Copyright (C) 2019 Baidu Inc. All rights reserved.
package com.baidu.acu.pie.client

import com.baidu.acu.pie.grpc.AsrClientGrpcImpl
import com.baidu.acu.pie.model.AsrConfig
import com.baidu.acu.pie.model.AsrProduct
import org.joda.time.LocalTime
import org.junit.Test
import org.springframework.test.util.ReflectionTestUtils
//import java.time.LocalTime
import kotlin.test.assertEquals

/**
 * AsrClientTest
 *
 * @author Cynric Shu (cynricshu@gmail.com)
 */
class AsrClientTest {
    val asrConfig = AsrConfig().serverIp("127.0.0.1").serverPort(8080).product(AsrProduct.CUSTOMER_SERVICE_FINANCE)
        .appName("unit test")

    @Test
    fun testParseLocalTimeFromAsrResult() {
        val asrClient = AsrClientGrpcImpl(asrConfig)

        val `time with out hour` = "01:23.456"

        var localTime = ReflectionTestUtils.invokeMethod<LocalTime>(asrClient, "parseLocalTime", `time with out hour`)
        assertEquals(localTime?.minuteOfHour, 1)
        assertEquals(localTime?.secondOfMinute, 23)
        assertEquals(localTime?.millisOfSecond, 456)

        val `time with hour` = "01:23:45.678"
        localTime = ReflectionTestUtils.invokeMethod<LocalTime>(asrClient, "parseLocalTime", `time with hour`)
        assertEquals(localTime?.hourOfDay, 1)
        assertEquals(localTime?.minuteOfHour, 23)
        assertEquals(localTime?.secondOfMinute, 45)
        assertEquals(localTime?.millisOfSecond, 678)

        val `special time` = "01:00.40"
        localTime = ReflectionTestUtils.invokeMethod<LocalTime>(asrClient, "parseLocalTime", `special time`)
        assertEquals(localTime?.minuteOfHour, 1)
        assertEquals(localTime?.secondOfMinute, 0)
        assertEquals(localTime?.millisOfSecond, 400)
    }
}