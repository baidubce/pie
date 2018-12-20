// Copyright (C) 2018 Baidu Inc. All rights reserved.
// Copyright (C) 2018 Baidu Inc. All rights reserved.

import com.baidu.acu.pie.AsrClient
import com.baidu.acu.pie.AsrConfig
import com.baidu.acu.pie.grpc.AudioStreaming
import com.google.protobuf.ByteString
import org.junit.Test
import java.io.File
import java.util.concurrent.TimeUnit

/**
 * AsrClientTest
 *
 * @author Shu Lingjie(shulingjie@baidu.com)
 */
class AsrClientTest {

    @Test
    fun testSend() {
        val audioFilePath = "testaudio/bj8k.wav"
        val asrClient = AsrClient(
            AsrConfig.builder()
                .serverIp("180.76.107.131")
                .serverPort(8051)
                .build()
        )

        val inputStream = File(audioFilePath).inputStream()

        val fragmentSize = 2560
        var data = ByteArray(fragmentSize)

        val requests = mutableListOf<AudioStreaming.AudioFragmentRequest>()

        while (true) {
            val readCount = inputStream.read(data)
            if (readCount < 0) break

            val req = AudioStreaming.AudioFragmentRequest.newBuilder()
                .setAudioData(ByteString.copyFrom(data, 0, readCount))
                .build()

            requests.add(req)
        }

        var finishLatch = asrClient.sendAllData(requests)

        if (!finishLatch.await(2, TimeUnit.MINUTES)) {
            println("routeChat can not finish within 1 minutes")
        }

        asrClient.shutdown()
    }
}