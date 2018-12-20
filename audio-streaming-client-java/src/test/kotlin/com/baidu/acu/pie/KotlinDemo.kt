// Copyright (C) 2018 Baidu Inc. All rights reserved.
package com.baidu.acu.pie

// Copyright (C) 2018 Baidu Inc. All rights reserved.
// Copyright (C) 2018 Baidu Inc. All rights reserved.

import com.baidu.acu.pie.model.AsrConfig
import org.junit.Test

/**
 * KotlinDemo
 *
 * @author Shu Lingjie(shulingjie@baidu.com)
 */
class KotlinDemo {
    @Test
    fun testSendFile() {
        val audioFilePath = "testaudio/bj8k.wav"
        val asrClient = AsrClientImpl(
            AsrConfig()
                .serverIp("180.76.107.131")
                .serverPort(8051)
        )

//        val inputStream = File(audioFilePath).inputStream()
//
//        val fragmentSize = 2560
//        var data = ByteArray(fragmentSize)
//
//        val requests = mutableListOf<AudioStreaming.AudioFragmentRequest>()
//
//        while (true) {
//            val readCount = inputStream.read(data)
//            if (readCount < 0) break
//
//            val req = AudioStreaming.AudioFragmentRequest.newBuilder()
//                .setAudioData(ByteString.copyFrom(data, 0, readCount))
//                .build()
//
//            requests.add(req)
//        }
//
//        var finishLatch = asrClient.sendMessages(requests)
//
//        if (!finishLatch.await(2, TimeUnit.MINUTES)) {
//            println("routeChat can not finish within 1 minutes")
//        }
//
//        asrClient.shutdown()
    }
}