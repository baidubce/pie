// Copyright (C) 2019 Baidu Inc. All rights reserved.

package com.baidu.acu.pie.demo;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import com.baidu.acu.pie.AudioStreaming.AudioFragmentRequest;
import com.baidu.acu.pie.client.AsrClient;
import com.baidu.acu.pie.client.AsrClientFactory;
import com.baidu.acu.pie.model.AsrConfig;
import com.baidu.acu.pie.model.AsrProduct;
import com.baidu.acu.pie.model.RecognitionResult;
import com.google.protobuf.ByteString;

import io.grpc.stub.StreamObserver;

/**
 * JavaDemo
 *
 * @author Shu Lingjie(shulingjie@baidu.com)
 */
public class JavaDemo {
    public static void main(String[] args) {
        JavaDemo javaDemo = new JavaDemo();
        javaDemo.asyncRecognition();
    }

    private AsrClient createAsrClient() {
        // asrConfig构造后就不可修改
        AsrConfig asrConfig = new AsrConfig()
                .serverIp("180.76.107.131")
                .serverPort(8050)
                .appName("simple demo")
                .product(AsrProduct.CUSTOMER_SERVICE);

        return AsrClientFactory.buildClient(asrConfig);
    }

    public void recognizeFile() {
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

    public void asyncRecognition() {
        // 使用长音频来模拟不断输入的情况
        String longAudioFilePath = "testaudio/1.wav";
        AsrClient asrClient = createAsrClient();

        try (InputStream audioStream = Files.newInputStream(Paths.get(longAudioFilePath))) {
            byte[] data = new byte[asrClient.getFragmentSize()];
            int readSize;

            CountDownLatch finishLatch = new CountDownLatch(1);
            StreamObserver<AudioFragmentRequest> sender = asrClient.asyncRecognize(it -> {
                System.out.println(it);
            }, finishLatch);

            while ((readSize = audioStream.read(data)) != -1) {
                sender.onNext(AudioFragmentRequest.newBuilder()
                        .setAudioData(ByteString.copyFrom(data, 0, readSize))
                        .build());
            }

            // wait to ensure to receive the last response
            finishLatch.await(1000, TimeUnit.SECONDS);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        asrClient.shutdown();
        System.out.println("all task finished");
    }
}
