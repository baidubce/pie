// Copyright (C) 2018 Baidu Inc. All rights reserved.
package com.baidu.acu.pie

// Copyright (C) 2018 Baidu Inc. All rights reserved.
// Copyright (C) 2018 Baidu Inc. All rights reserved.

import com.baidu.acu.pie.client.AsrClientFactory
import com.baidu.acu.pie.model.AsrConfig
import com.baidu.acu.pie.model.AsrProduct
import org.junit.Test
import java.nio.file.Paths

/**
 * KotlinDemo
 *
 * @author Shu Lingjie(shulingjie@baidu.com)
 */
class KotlinDemo {
    @Test
    fun testSendFile() {
        val audioFilePath = "testaudio/bj8k.wav"

        val asrConfig = AsrConfig()
            .serverIp("180.76.107.131")
            .serverPort(8050)
            .appName("simple demo")
            .product(AsrProduct.CUSTOMER_SERVICE)

        val asrClient = AsrClientFactory.buildClient(asrConfig)
        val results = asrClient.syncRecognize(Paths.get(audioFilePath))

        // don't forget to shutdown !!!
        asrClient.shutdown()

        for (result in results) {
            println(
                String.format(
                    AsrConfig.TITLE_FORMAT,
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