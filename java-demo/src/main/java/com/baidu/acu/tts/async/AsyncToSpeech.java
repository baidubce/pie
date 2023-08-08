package com.baidu.acu.tts.async;

import com.baidu.acu.pie.client.TtsClient;
import com.baidu.acu.pie.client.TtsClientFactory;
import com.baidu.acu.pie.model.TtsConfig;
import com.baidu.acu.pie.model.TtsRequest;
import com.baidu.acu.pie.model.TtsStreamContext;
import org.joda.time.DateTime;

/**
 * 类<code>AsyncToSpeech</code>用于：
 *
 * @author Xia Shuai(xiashuai01@baidu.com)
 * @version 1.0
 * @date 2023-08-08
 **/
public class AsyncToSpeech {
    public static void main(String[] args) {
        try {
            AsyncToSpeech ttsDemo = new AsyncToSpeech();
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
