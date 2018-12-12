// Copyright (C) 2018 Baidu Inc. All rights reserved.

import com.baidu.acu.pie.AsrClient
import com.baidu.acu.pie.AudioStreaming.AudioFragmentRequest
import com.baidu.acu.pie.Constants
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
        val asrClient = AsrClient(Constants.SERVER_IP_ADDR, Constants.SERVER_IP_PORT)

        val inputStream = File(Constants.AUDIO_FILE_PATH).inputStream()

        val fragmentSize = 2560
        var data = ByteArray(fragmentSize)

        val requests = mutableListOf<AudioFragmentRequest>()

        while (true) {
            val readCount = inputStream.read(data)
            if (readCount < 0) break

            val req = AudioFragmentRequest.newBuilder()
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