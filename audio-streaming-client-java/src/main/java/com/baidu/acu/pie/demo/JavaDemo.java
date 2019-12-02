// Copyright (C) 2019 Baidu Inc. All rights reserved.

package com.baidu.acu.pie.demo;

import com.baidu.acu.pie.client.AsrClient;
import com.baidu.acu.pie.client.AsrClientFactory;
import com.baidu.acu.pie.client.Consumer;
import com.baidu.acu.pie.model.AsrConfig;
import com.baidu.acu.pie.model.AsrProduct;
import com.baidu.acu.pie.model.RecognitionResult;
import com.baidu.acu.pie.model.RequestMetaData;
import com.baidu.acu.pie.model.StreamContext;
import com.baidu.acu.pie.util.JacksonUtil;
import org.joda.time.DateTime;
import org.joda.time.Duration;

import java.io.File;
import java.io.FileFilter;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        // 当使用ssl client时，需要配置字段sslUseFlag以及sslPath
        AsrConfig asrConfig = AsrConfig.builder()
                .serverIp("asr.baiduai.cloud")
                .serverPort(8051)
                .appName("simpleDemo")
                .product(AsrProduct.CUSTOMER_SERVICE_FINANCE)
                .userName("testcommon00")
                .password("00nomnoctset")
//                .sslUseFlag(true)
//                .sslPath("ca.crt")
                .build();

        return AsrClientFactory.buildClient(asrConfig);
    }

    private RequestMetaData createRequestMeta() {
        RequestMetaData requestMetaData = new RequestMetaData();
        requestMetaData.setSendPackageRatio(1);
        requestMetaData.setSleepRatio(1);
        requestMetaData.setTimeoutMinutes(120);
        requestMetaData.setEnableFlushData(false);
        // 随路信息根据需要设置
        Map<String, Object> extra_info = new HashMap<>();
        extra_info.put("demo", "java");
        requestMetaData.setExtraInfo(JacksonUtil.objectToString(extra_info));

        return requestMetaData;
    }

    /**
     * 用户可以自己创建一个 RequestMeta 对象，用来控制请求时候的数据发送速度等参数
     */
    public void recognizeFileWithRequestMeta() {
        File audioFile = new File("testaudio/10s.wav");

        AsrClient asrClient = createAsrClient();

        DateTime startTime = DateTime.now();
        List<RecognitionResult> results = asrClient.syncRecognize(audioFile, createRequestMeta());
        DateTime endTime = DateTime.now();

        Duration duration = new Duration(startTime, endTime);
        System.out.println("time cost millis : " + duration.getMillis());

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
                    audioFile.getName(),
                    result.getSerialNum(),
                    result.getStartTime(),
                    result.getEndTime(),
                    result.getResult()
            ));
        }
    }

    public void recognizeDirectory() {
        AsrClient asrClient = createAsrClient();

        RequestMetaData requestMetaData = createRequestMeta();

        String audioFileDir = "testaudio";
        File dir = new File(audioFileDir);
        File[] files = dir.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return pathname.getName().endsWith("wav");
            }
        });

        int count = 0;
        for (File file : files) {
            List<RecognitionResult> results = asrClient.syncRecognize(file, requestMetaData);
            count += 1;
        }
        System.out.println("***********************  count: " + count);

        // don't forget to shutdown !!!
        asrClient.shutdown();
    }

    /**
     * 使用长音频来模拟人对着麦克风不断说话的情况
     */
    public void asyncRecognition() {
        String longAudioFilePath = "testaudio/1.wav";
        AsrClient asrClient = createAsrClient();

        RequestMetaData requestMetaData = createRequestMeta();

        StreamContext streamContext = asrClient.asyncRecognize(new Consumer<RecognitionResult>() {
            @Override
            public void accept(RecognitionResult it) {
                System.out.println(
                        DateTime.now().toString() + "\t" + Thread.currentThread().getId() +
                                " receive fragment: " + it);
            }
        }, requestMetaData);

        // 这里从文件中得到一个InputStream，实际场景下，也可以从麦克风或者其他音频源来得到InputStream
        try (InputStream audioStream = Files.newInputStream(Paths.get(longAudioFilePath))) {
            byte[] data = new byte[asrClient.getFragmentSize(requestMetaData)];

            System.out.println(new DateTime().toString() + "\t" + Thread.currentThread().getId() + " start to send");

            // 使用 sender.onNext 方法，将 InputStream 中的数据不断地发送到 asr 后端，发送的最小单位是 AudioFragment
            while (audioStream.read(data) != -1 && !streamContext.getFinishLatch().finished()) {
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
