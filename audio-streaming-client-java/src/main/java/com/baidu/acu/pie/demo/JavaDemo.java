// Copyright (C) 2019 Baidu Inc. All rights reserved.

package com.baidu.acu.pie.demo;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import org.joda.time.DateTime;

import com.baidu.acu.pie.client.AsrClient;
import com.baidu.acu.pie.client.AsrClientFactory;
import com.baidu.acu.pie.client.Consumer;
import com.baidu.acu.pie.model.AsrConfig;
import com.baidu.acu.pie.model.RecognitionResult;
import com.baidu.acu.pie.model.RequestMetaData;
import com.baidu.acu.pie.model.StreamContext;

/**
 * JavaDemo
 *
 * @author Cynric Shu (cynricshu@gmail.com)
 */
public class JavaDemo {
    public static void main(String[] args) {
        JavaDemo javaDemo = new JavaDemo();
        //        javaDemo.recognizeFile();
        javaDemo.asyncRecognition();
    }

    private AsrClient createAsrClient() {
        // asrConfig构造后就不可修改
        AsrConfig asrConfig = new AsrConfig()
                .serverIp("127.0.0.1")
                .serverPort(80)
                .appName("simple demo")
                .productId("1903")
                .userName("user1")
                .password("password1");

        return AsrClientFactory.buildClient(asrConfig);
    }

    public void recognizeFile() {
        String audioFilePath = "testaudio/bj8k.wav";
        AsrClient asrClient = createAsrClient();

        RequestMetaData requestMetaData = RequestMetaData.defaultRequestMeta();
        requestMetaData.enableFlushData(false);
        List<RecognitionResult> results = asrClient.syncRecognize(Paths.get(audioFilePath).toFile(), requestMetaData);

        // don't forget to shutdown !!!
        asrClient.shutdown();

        for (RecognitionResult result : results) {
            System.out.println(String.format(AsrConfig.TITLE_FORMAT,
                    "file_name",
                    "serial_num",
                    "start_time",
                    "end_time",
                    "result"));
            System.out.println(String.format(AsrConfig.TITLE_FORMAT,
                    audioFilePath,
                    result.getSerialNum(),
                    result.getStartTime(),
                    result.getEndTime(),
                    result.getResult()
            ));
        }
    }

    /**
     * 用户可以自己创建一个 RequestMeta 对象，用来控制请求时候的数据发送速度等参数
     */
    public void recognizeFileWithRequestMeta() {
        String audioFilePath = "testaudio/bj8k.wav";
        AsrClient asrClient = createAsrClient();

        RequestMetaData requestMetaData = new RequestMetaData();
        requestMetaData.sendPerSeconds(0.05);
        requestMetaData.sendPackageRatio(1);
        requestMetaData.sleepRatio(1);

        List<RecognitionResult> results = asrClient.syncRecognize(Paths.get(audioFilePath).toFile(), requestMetaData);

        // don't forget to shutdown !!!
        asrClient.shutdown();

        for (RecognitionResult result : results) {
            System.out.println(String.format(AsrConfig.TITLE_FORMAT,
                    "file_name",
                    "serial_num",
                    "start_time",
                    "end_time",
                    "result"));
            System.out.println(String.format(AsrConfig.TITLE_FORMAT,
                    audioFilePath,
                    result.getSerialNum(),
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

        StreamContext streamContext = asrClient.asyncRecognize(new Consumer<RecognitionResult>() {
            @Override
            public void accept(RecognitionResult it) {
                System.out.println(
                        DateTime.now().toString() + "\t" + Thread.currentThread().getId() +
                                " receive fragment: " + it);
            }
        });

        // 这里从文件中得到一个InputStream，实际场景下，也可以从麦克风或者其他音频源来得到InputStream
        try (InputStream audioStream = Files.newInputStream(Paths.get(longAudioFilePath))) {
            byte[] data = new byte[asrClient.getFragmentSize()];
            int readSize;

            System.out.println(new DateTime().toString() + "\t" + Thread.currentThread().getId() + " start to send");

            // 使用 sender.onNext 方法，将 InputStream 中的数据不断地发送到 asr 后端，发送的最小单位是 AudioFragment
            while ((readSize = audioStream.read(data)) != -1 && !streamContext.getFinishLatch().finished()) {
                streamContext.send(data);
                // 主动休眠一段时间，来模拟人说话场景下的音频产生速率
                // 在对接麦克风等设备的时候，可以去掉这个 sleep
                Thread.sleep(20);
            }
            System.out.println(new DateTime().toString() + "\t" + Thread.currentThread().getId() + " send finish");
            streamContext.complete();

            // wait to ensure to receive the last response
            streamContext.getFinishLatch().await();
        } catch (Throwable e) {
            e.printStackTrace();
        } finally {
            asrClient.shutdown();
        }

        System.out.println("all task finished");
    }
}
