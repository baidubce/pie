// Copyright (C) 2018 Baidu Inc. All rights reserved.

package com.baidu.acu.pie;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import com.baidu.acu.pie.AudioStreaming.AudioFragmentRequest;
import com.google.protobuf.ByteString;

/**
 * Main
 *
 * @author Shu Lingjie(shulingjie@baidu.com)
 */
public class Main {
    public static void main(String[] args) throws InterruptedException, IOException {
        AsrClient asrClient = new AsrClient(Constants.SERVER_IP_ADDR, Constants.SERVER_IP_PORT);

        InputStream inputStream = Files.newInputStream(Paths.get(Constants.AUDIO_FILE_PATH));

        int fragmentSize = 2560;
        byte[] data = new byte[fragmentSize];
        int readCount;

        List<AudioFragmentRequest> requests = new ArrayList<>();

        while ((readCount = inputStream.read(data)) != -1) {
            requests.add(
                    AudioFragmentRequest.newBuilder()
                            .setAudioData(ByteString.copyFrom(data, 0, readCount))
                            .build()
            );
        }

        CountDownLatch finishLatch = asrClient.sendAllData(requests);

        if (!finishLatch.await(2, TimeUnit.MINUTES)) {
            System.out.println("routeChat can not finish within 1 minutes");
        }

        asrClient.shutdown();
    }
}
