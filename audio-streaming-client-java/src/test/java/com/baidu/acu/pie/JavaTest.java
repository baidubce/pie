// Copyright (C) 2019 Baidu Inc. All rights reserved.

package com.baidu.acu.pie;

import com.baidu.acu.pie.client.AsrClient;
import com.baidu.acu.pie.client.AsrClientFactory;
import com.baidu.acu.pie.client.Consumer;
import com.baidu.acu.pie.model.AsrConfig;
import com.baidu.acu.pie.model.AsrProduct;
import com.baidu.acu.pie.model.RecognitionResult;
import com.baidu.acu.pie.model.StreamContext;
import com.baidu.acu.pie.util.Base64;
import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import javax.xml.bind.DatatypeConverter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * JavaTest
 *
 * @author Cynric Shu (cynricshu@gmail.com)
 */
public class JavaTest {
    private AsrClient createAsrClient() {
        // asrConfig构造后就不可修改
        AsrConfig asrConfig = new AsrConfig()
                .serverIp("127.0.0.1")
                .serverPort(80)
                .appName("simple demo")
                .userName("user")
                .password("password")
                .product(AsrProduct.CUSTOMER_SERVICE);

        return AsrClientFactory.buildClient(asrConfig);
    }

    @Ignore
    @Test
    public void testAsyncRecognition() {
        // 使用长音频来模拟不断输入的情况
        String longAudioFilePath = "testaudio/1.wav";
        AsrClient asrClient = createAsrClient();

        try (InputStream audioStream = Files.newInputStream(Paths.get(longAudioFilePath))) {
            byte[] data = new byte[asrClient.getFragmentSize()];
            int readSize;

            StreamContext context = asrClient.asyncRecognize(new Consumer<RecognitionResult>() {
                @Override
                public void accept(RecognitionResult it) {
                    System.out.println(it);
                }
            });

            while ((readSize = audioStream.read(data)) != -1) {
                context.send(data);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        asrClient.shutdown();

        System.out.println("all task finished");
    }

    @Ignore
    @Test
    public void testSendFileMultiThread() {
        final String audioFilePath = "testaudio/bj8k.wav";
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
        Assert.assertEquals(Base64.encode(s.getBytes()), DatatypeConverter.printBase64Binary(s.getBytes()));
    }
}
