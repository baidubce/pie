// Copyright (C) 2019 Baidu Inc. All rights reserved.

package com.baidu.acu.pie.demo;

import com.baidu.acu.pie.AudioStreaming.AudioFragmentRequest;
import com.baidu.acu.pie.client.AsrClient;
import com.baidu.acu.pie.client.AsrClientFactory;
import com.baidu.acu.pie.client.Consumer;
import com.baidu.acu.pie.model.AsrConfig;
import com.baidu.acu.pie.model.AsrProduct;
import com.baidu.acu.pie.model.RecognitionResult;
import com.google.protobuf.ByteString;
import io.grpc.stub.StreamObserver;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * JavaDemo
 *
 * @author Cynric Shu (cynricshu@gmail.com)
 */
public class JavaDemo {
    public static void main(String[] args) {
        JavaDemo javaDemo = new JavaDemo();
        javaDemo.asyncRecognition();
    }

    private AsrClient createAsrClient() {
        // asrConfig构造后就不可修改
        AsrConfig asrConfig = new AsrConfig()
                .serverIp("127.0.0.1")
                .serverPort(80)
                .appName("simple demo")
                .sleepRatio(1)
                .product(AsrProduct.CUSTOMER_SERVICE_FINANCE);

        return AsrClientFactory.buildClient(asrConfig);
    }

    public void recognizeFile() {
        String audioFilePath = "testaudio/1.wav";
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

    /**
     * 使用长音频来模拟人对着麦克风不断说话的情况
     */
    public void asyncRecognition() {
        String longAudioFilePath = "testaudio/1.wav";
        AsrClient asrClient = createAsrClient();
        CountDownLatch finishLatch = new CountDownLatch(1);
        StreamObserver<AudioFragmentRequest> sender = asrClient.asyncRecognize(new Consumer<RecognitionResult>() {
            @Override
            public void accept(RecognitionResult it) {
                System.out.println(
//                        Instant.now().toString() + "\t" + Thread.currentThread().getId() + " receive fragment: " + it);
                        Thread.currentThread().getId() + " receive fragment: " + it);
            }
        }, finishLatch);

        try (InputStream audioStream = Files.newInputStream(Paths.get(longAudioFilePath))) {
            byte[] data = new byte[asrClient.getFragmentSize()];
            int readSize;

//            System.out.println(Instant.now().toString() + "\t" + Thread.currentThread().getId() + " start to send");
            System.out.println(Thread.currentThread().getId() + " start to send");
            while ((readSize = audioStream.read(data)) != -1) {
//                System.out.println(Instant.now().toString() + "\t"
                System.out.println(
                        +Thread.currentThread().getId() + " send fragment");
                sender.onNext(AudioFragmentRequest.newBuilder()
                        .setAudioData(ByteString.copyFrom(data, 0, readSize))
                        .build());
                // 主动休眠一段时间，来模拟人说话场景下的音频产生速率
                Thread.sleep(20);
            }
//            System.out.println(Instant.now().toString() + "\t" + Thread.currentThread().getId() + " send finish");
            System.out.println(Thread.currentThread().getId() + " send finish");

            // wait to ensure to receive the last response
            finishLatch.await(1000, TimeUnit.SECONDS);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        } finally {
            sender.onCompleted();
            asrClient.shutdown();
        }

        System.out.println("all task finished");
    }
}
