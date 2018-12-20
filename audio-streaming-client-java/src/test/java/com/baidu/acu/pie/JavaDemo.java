// Copyright (C) 2018 Baidu Inc. All rights reserved.

package com.baidu.acu.pie;

import java.nio.file.Paths;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import org.junit.Test;

import com.baidu.acu.pie.client.AsrClient;
import com.baidu.acu.pie.client.AsrClientFactory;
import com.baidu.acu.pie.model.AsrConfig;
import com.baidu.acu.pie.model.AsrProduct;
import com.baidu.acu.pie.model.RecognitionResult;

import lombok.extern.slf4j.Slf4j;

/**
 * JavaDemo
 *
 * @author Shu Lingjie(shulingjie@baidu.com)
 */
public class JavaDemo {
    private AsrClient createAsrClient() {
        // asrConfig构造后就不可修改
        AsrConfig asrConfig = new AsrConfig()
                .serverIp("127.0.0.1")
                .serverPort(80)
                .appName("simple demo")
                .product(AsrProduct.CUSTOMER_SERVICE);

        return AsrClientFactory.buildClient(asrConfig);
    }

    @Test
    public void testSendFile() {
        String audioFilePath = "testaudio/bj8k.wav";
        AsrClient asrClient = createAsrClient();
        List<RecognitionResult> results = asrClient.syncRecognize(Paths.get(audioFilePath));

        // don't forget to shutdown !!!
        asrClient.shutdown();

        for (RecognitionResult result : results) {
            System.out.println(String.format(AsrConfig.TITLE_FORMAT,
                    "serial_num",
                    "err_no",
                    "err_message",
                    "start_time",
                    "end_time",
                    "result"));
            System.out.println(String.format(AsrConfig.TITLE_FORMAT,
                    result.getSerialNum(),
                    result.getErrorCode(),
                    result.getErrorMessage(),
                    result.getStartTime(),
                    result.getEndTime(),
                    result.getResult()
            ));
        }
    }

    @Test
    public void testSendFileMultiThread() {
        String audioFilePath = "testaudio/bj8k.wav";
        AsrClient asrClient = createAsrClient();

        int concurrentNum = 5;
        final CountDownLatch finishLatch = new CountDownLatch(concurrentNum);

        for (int i = 0; i < concurrentNum; i++) {
            new Thread(() -> {
                List<RecognitionResult> results = asrClient.syncRecognize(Paths.get(audioFilePath));
                System.out.printf("thread %d finished at time: %s, result: %s\n",
                        Thread.currentThread().getId(),
                        Instant.now().toString(),
                        results.get(0).getResult()
                );
                finishLatch.countDown();
            }).start();
        }

        try {
            finishLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        asrClient.shutdown();
        System.out.println("all task finished");
    }
}
