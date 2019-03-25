package com.baidu.acu.pie.grpc

import com.baidu.acu.pie.model.AsrConfig
import com.baidu.acu.pie.model.AsrProduct
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExpectedException
import org.springframework.test.util.ReflectionTestUtils
import java.time.LocalTime
import kotlin.test.assertEquals

/**
 * AsrClientGrpcImplTest
 *
 * @author Cynric Shu(cynricshu@gmail.com)
 */
class AsrClientGrpcImplTest {
    @Rule
    @JvmField
    val expectedException = ExpectedException.none()

    val asrConfig = AsrConfig().serverIp("127.0.0.1").serverPort(8080).product(AsrProduct.CUSTOMER_SERVICE_FINANCE)
        .appName("unit test")

    @Test
    fun `test parseLocalTime`() {
        val asrClient = AsrClientGrpcImpl(asrConfig)

        val `time with out hour` = "01:23.456"

        var localTime = ReflectionTestUtils.invokeMethod<LocalTime>(asrClient, "parseLocalTime", `time with out hour`)
        assertEquals(localTime?.minute, 1)
        assertEquals(localTime?.second, 23)
        assertEquals(localTime?.nano, 456000000)

        val `time with hour` = "01:23:45.678"
        localTime = ReflectionTestUtils.invokeMethod<LocalTime>(asrClient, "parseLocalTime", `time with hour`)
        assertEquals(localTime?.hour, 1)
        assertEquals(localTime?.minute, 23)
        assertEquals(localTime?.second, 45)
        assertEquals(localTime?.nano, 678000000)

        val `special time` = "01:00.40"
        localTime = ReflectionTestUtils.invokeMethod<LocalTime>(asrClient, "parseLocalTime", `special time`)
        assertEquals(localTime?.minute, 1)
        assertEquals(localTime?.second, 0)
        assertEquals(localTime?.nano, 400000000)
    }

    @Test
    fun `test parseLocalTime with exception`() {
        val asrClient = AsrClientGrpcImpl(asrConfig)

        val localTime = ReflectionTestUtils.invokeMethod<LocalTime>(asrClient, "parseLocalTime", "")
        assertEquals(localTime, LocalTime.MIN)
    }
}