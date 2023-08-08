package com.baidu.acu.tts.sync;

import com.baidu.acu.pie.client.TtsClient;
import com.baidu.acu.pie.client.TtsClientFactory;
import com.baidu.acu.pie.model.TtsConfig;
import com.baidu.acu.pie.model.TtsRequest;

/**
 * 类<code>SyncToSpeech</code>用于：
 *
 * @author Xia Shuai(xiashuai01@baidu.com)
 * @version 1.0
 * @date 2023-08-08
 **/
public class SyncToSpeech {
    public static void main(String[] args) {
        try {
            SyncToSpeech ttsDemo = new SyncToSpeech();
            ttsDemo.syncToSpeech();

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
        return ttsRequest;
    }


    public void syncToSpeech() {
        String longAudioFilePath = "temp/audio.mp3";

        TtsClient ttsClient = TtsClientFactory.buildClient(buildTtsConfig());

        TtsRequest request = createRequest();
        // 3表示为MP3，4表示pcm
        request.setAue(3);

        String text = "你好";

        ttsClient.syncToSpeech(text, longAudioFilePath, request);
        System.out.println("all task finished");
    }

}
