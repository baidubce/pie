package com.baidu.acu.tts.async;

import com.baidu.acu.asr.model.TtsArgs;
import com.baidu.acu.pie.client.TtsClient;
import com.baidu.acu.pie.client.TtsClientFactory;
import com.baidu.acu.pie.model.TtsConfig;
import com.baidu.acu.pie.model.TtsRequest;
import com.baidu.acu.pie.model.TtsStreamContext;
import com.baidu.acu.util.TtsPlay;

import javax.sound.sampled.LineUnavailableException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

/**
 * 类<code>AsyncToSpeechWithRead</code>用于：
 *
 * @author Xia Shuai(xiashuai01@baidu.com)
 * @version 1.0
 * @date 2023-08-17
 **/
public class AsyncToSpeech {
    public static void main(String[] args) {
        // 测试保存数据
        // java -jar java-demo-1.2.2-SNAPSHOT-shaded.jar -address ws://127.0.0.1:8081/api/v1/tts/stream -per 5106 -text 你好 -type save -file-path=audio1.pcm
        // 测试扬声器播放
        // java -jar java-demo-1.2.2-SNAPSHOT-shaded.jar -address ws://127.0.0.1:8081/api/v1/tts/stream -per 5106 -text 你好 -type read
        TtsArgs mainArgs = TtsArgs.parse(args);
        AsyncToSpeech asyncToSpeech = new AsyncToSpeech();
        try {
            if ("read".equals(mainArgs.getType())) {
                asyncToSpeech.read(mainArgs);
            } else if ("save".equals(mainArgs.getType())) {
                asyncToSpeech.write(mainArgs);
            } else {
                System.out.println("tye is error");
            }
        } catch (LineUnavailableException e) {
            System.out.println("无扬声器设备");
            e.printStackTrace();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


    /**
     * 流式读取音频流
     * @param args 请求参数
     */
    public void read(TtsArgs args) throws LineUnavailableException, InterruptedException {

        TtsClient ttsClient = TtsClientFactory.buildClient(buildTtsConfig(args));

        TtsRequest request = createRequest(args);

        final TtsPlay ttsPlay = new TtsPlay(16000, 16, 1, 2, TtsPlay.encodingString);


        TtsStreamContext streamContext = ttsClient.asyncToSpeech(args.getText(), ttsPlay::playStream, request);
        streamContext.enableCallback(e -> {
            if (e != null) {
                e.printStackTrace();
            }
        });

        // wait to ensure to receive the last response
        streamContext.getFinishLatch().await();
        ttsClient.shutdown();
        ttsPlay.close();

        System.out.println("all task finished");
    }


    /**
     * 流式写音频流文件
     * @param args 请求参数
     */
    public void write(TtsArgs args) throws InterruptedException, IOException {

        TtsClient ttsClient = TtsClientFactory.buildClient(buildTtsConfig(args));

        TtsRequest request = createRequest(args);

        // 如果是保存文件，删除原来的文件
        Files.deleteIfExists(Path.of(args.getFilePath()));


        TtsStreamContext streamContext = ttsClient.asyncToSpeech(args.getText(),  e-> {
            try {
                // 追加写音频流
                Files.write(Path.of(args.getFilePath()),
                        e, StandardOpenOption.APPEND, StandardOpenOption.CREATE);
            } catch (IOException ex) {
                System.out.println("write file encounter exception");
                ex.printStackTrace();
            }
        }, request);
        streamContext.enableCallback(e -> {
            if (e != null) {
                e.printStackTrace();
            }
        });

        // wait to ensure to receive the last response
        streamContext.getFinishLatch().await();
        ttsClient.shutdown();

        System.out.println("all task finished");
    }

    /**
     * 创建tts配置
     *
     * @param args 请求参数
     * @return tts配置
     */
    public TtsConfig buildTtsConfig(TtsArgs args) {
        return TtsConfig.builder()
                .serverIp(args.getIp())
                .serverPort(args.getPort())
                .build();
    }


    /**
     * 创建TTS请求
     *
     * @param args 请求配置
     * @return 请求体
     */
    public TtsRequest createRequest(TtsArgs args) {
        TtsRequest ttsRequest = new TtsRequest();
        ttsRequest.setAue(args.getAue());
        ttsRequest.setPer(args.getPer());
        ttsRequest.setPit(args.getPit());
        ttsRequest.setSpd(args.getSpd());
        ttsRequest.setXml(args.getXml());
        ttsRequest.setVol(args.getVol());
        ttsRequest.setLan(args.getLan());
        return ttsRequest;
    }
}
