package com.baidu.acu.pie.demo;

import com.baidu.acu.pie.TtsServiceGrpc;
import com.baidu.acu.pie.TtsStreaming;
import com.baidu.acu.pie.client.AsrClient;
import com.baidu.acu.pie.client.Consumer;
import com.baidu.acu.pie.client.TtsClient;
import com.baidu.acu.pie.client.TtsClientFactory;
import com.baidu.acu.pie.exception.GlobalException;
import com.baidu.acu.pie.model.RecognitionResult;
import com.baidu.acu.pie.model.RequestMetaData;
import com.baidu.acu.pie.model.StreamContext;
import com.baidu.acu.pie.model.TtsConfig;
import com.baidu.acu.pie.model.TtsRequest;
import com.baidu.acu.pie.model.TtsStreamContext;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * 类<code>TtsDemo</code>用于：
 *
 * @author Xia Shuai(xiashuai01@baidu.com)
 * @version 1.0
 **/
@Slf4j
public class TtsDemo {

    public static void main(String[] args) {
        try {
            TtsDemo ttsDemo = new TtsDemo();
            ttsDemo.asyncToSpeech();
//            ttsDemo.syncToSpeech();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public TtsConfig buildTtsConfig() {
        return TtsConfig.builder()
                .serverIp("127.0.0.1")
                .serverPort(8052)
                .build();
    }

    public TtsRequest createRequest() {
        TtsRequest ttsRequest = new TtsRequest();
//        ttsRequest.setAue(3);
        return ttsRequest;
    }


    public void syncToSpeech() {
        String longAudioFilePath = "temp/audio.mp3";

        TtsClient ttsClient = TtsClientFactory.buildClient(buildTtsConfig());

        TtsRequest request = createRequest();

        String text = "你好";

        ttsClient.syncToSpeech(text, longAudioFilePath, request);
        System.out.println("all task finished");
    }

    /**
     * 流式返回音频流
     */
    public void asyncToSpeech() {

        TtsClient ttsClient = TtsClientFactory.buildClient(buildTtsConfig());

        TtsRequest request = createRequest();

        String text = "你好";


        TtsStreamContext streamContext = ttsClient.asyncToSpeech(text, e -> {
            System.out.println(
                    DateTime.now().toString() + "\t" + Thread.currentThread().getId() +
                            " receive fragment: " + e.length);
        }, request);
        streamContext.enableCallback(e -> {
            if (e != null) {
                System.out.println(e);
            }
        });


        // 这里从文件中得到一个InputStream，实际场景下，也可以从麦克风或者其他音频源来得到InputStream
        try {

            // wait to ensure to receive the last response
            streamContext.getFinishLatch().await();
        } catch (Throwable e) {
            e.printStackTrace();
        } finally {
            ttsClient.shutdown();
        }

        System.out.println("all task finished");
    }

}
