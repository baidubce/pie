// Copyright (C) 2019 Baidu Inc. All rights reserved.

package com.baidu.acu.pie;

import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.CountDownLatch;

//import javax.xml.bind.DatatypeConverter;

import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import com.baidu.acu.pie.client.AsrClient;
import com.baidu.acu.pie.client.AsrClientFactory;
import com.baidu.acu.pie.model.AsrConfig;
import com.baidu.acu.pie.model.AsrProduct;
import com.baidu.acu.pie.model.RecognitionResult;
import com.baidu.acu.pie.util.Base64;

/**
 * JavaTest
 *
 * @author Cynric Shu (cynricshu@gmail.com)
 */
public class JavaTest {
    private AsrClient createAsrClient() {
        // asrConfig构造后就不可修改
        AsrConfig asrConfig = AsrConfig.builder()
                .serverIp("127.0.0.1")
                .serverPort(80)
                .appName("simple demo")
                .userName("user")
                .password("password")
                .product(AsrProduct.CUSTOMER_SERVICE)
                .build();

        return AsrClientFactory.buildClient(asrConfig);
    }

    @Ignore
    @Test
    public void testSendFileMultiThread() {
        final String audioFilePath = "testaudio/1.wav";
        final AsrClient asrClient = createAsrClient();

        int concurrentNum = 5;
        final CountDownLatch finishLatch = new CountDownLatch(concurrentNum);

        for (int i = 0; i < concurrentNum; i++) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    List<RecognitionResult> results = asrClient.syncRecognize(Paths.get(audioFilePath).toFile());
                    System.out.printf("thread %d finished at time: %s, result: %s\n",
                            Thread.currentThread().getId(),
                            new DateTime().toString(),
                            results
                    );
                    finishLatch.countDown();
                }
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

    @Test
    public void testBase64() {
        String s = "abc+123+这是一个中文+😁";
//        Assert.assertEquals(Base64.encode(s.getBytes()), DatatypeConverter.printBase64Binary(s.getBytes()));
    }
}
