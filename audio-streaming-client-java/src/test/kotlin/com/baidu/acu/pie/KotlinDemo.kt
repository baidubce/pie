// Copyright (C) 2018 Baidu Inc. All rights reserved.

package com.baidu.acu.pie

import com.baidu.acu.pie.client.AsrClient
import com.baidu.acu.pie.client.AsrClientFactory
import com.baidu.acu.pie.model.AsrConfig
import com.baidu.acu.pie.model.AsrProduct
import org.junit.Ignore
import org.junit.Test
import java.io.File
import java.nio.file.Files
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
            .product(AsrProduct.CUSTOMER_SERVICE_FINANCE)
            .appName("simple demo")
            .timeoutMinutes(120)

        val asrClient = AsrClientFactory.buildClient(asrConfig)
        return asrClient
    }

    @Test
    fun `batch Test And Save Result to File`() {
        val asrClient = createAsrClient()
        val dirPath = Paths.get("testaudio")
        val fileList = Files.list(dirPath)

        val resultFile = dirPath.resolve("result.txt").toFile()
        resultFile.writeText(
            String.format(
                AsrConfig.TITLE_FORMAT,
                "file_name",
                "serial_num",
                "err_no",
                "err_message",
                "start_time",
                "end_time",
                "result"
            )
        )
        resultFile.appendText("\n")

        for (file in fileList) {
            val results = asrClient.syncRecognize(dirPath.resolve(file.fileName))
            for (result in results) {
                resultFile.appendText(
                    String.format(
                        AsrConfig.TITLE_FORMAT,
                        file.fileName,
                        result.serialNum,
                        result.errorCode,
                        result.errorMessage,
                        result.startTime,
                        result.endTime,
                        result.result
                    )
                )
                resultFile.appendText("\n")
            }
            resultFile.appendText("\n\n")
        }
        // don't forget to shutdown !!!
        asrClient.shutdown()
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
                    "file_name",
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
                    audioFilePath,
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
                    "file_name",
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
                    audioFilePath,
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