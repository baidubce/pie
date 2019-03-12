// Copyright (C) 2018 Baidu Inc. All rights reserved.

package com.baidu.acu.pie

import com.baidu.acu.pie.client.AsrClient
import com.baidu.acu.pie.client.AsrClientFactory
import com.baidu.acu.pie.model.AsrConfig
import com.baidu.acu.pie.model.AsrProduct
import org.junit.Ignore
import org.junit.Test
import java.io.File
import java.nio.file.Paths

/**
 * KotlinDemo
 *
 * @author Cynric Shu (cynricshu@gmail.com)
 */
@Ignore
class KotlinDemo {

    fun createAsrClient(): AsrClient {
        val asrConfig = AsrConfig()
            .serverIp("127.0.0.1")
            .serverPort(80)
            .appName("simple demo")
            .product(AsrProduct.CUSTOMER_SERVICE_FINANCE)

        val asrClient = AsrClientFactory.buildClient(asrConfig)
        return asrClient
    }

    @Test
    fun `test recognise with Path`() {
        val audioFilePath = "testaudio/bj8k.wav"

        val asrClient = createAsrClient()
        val results = asrClient.syncRecognize(Paths.get(audioFilePath))

        // don't forget to shutdown !!!
        asrClient.shutdown()

        for (result in results) {
            println(
                String.format(
                    AsrConfig.TITLE_FORMAT,
                    "serial_num",
                    "err_no",
                    "err_message",
                    "start_time",
                    "end_time",
                    "result"
                )
            )
            println(
                String.format(
                    AsrConfig.TITLE_FORMAT,
                    result.serialNum,
                    result.errorCode,
                    result.errorMessage,
                    result.startTime,
                    result.endTime,
                    result.result
                )
            )
        }
    }

    @Test
    fun `test recognise with File`() {
        val audioFilePath = "testaudio/bj8k.wav"

        val asrClient = createAsrClient()
        val results = asrClient.syncRecognize(File(audioFilePath))

        // don't forget to shutdown !!!
        asrClient.shutdown()

        for (result in results) {
            println(
                String.format(
                    AsrConfig.TITLE_FORMAT,
                    "serial_num",
                    "err_no",
                    "err_message",
                    "start_time",
                    "end_time",
                    "result"
                )
            )
            println(
                String.format(
                    AsrConfig.TITLE_FORMAT,
                    result.serialNum,
                    result.errorCode,
                    result.errorMessage,
                    result.startTime,
                    result.endTime,
                    result.result
                )
            )
        }
    }
}