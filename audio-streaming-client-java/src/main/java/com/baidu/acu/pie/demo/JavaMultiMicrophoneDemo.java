package com.baidu.acu.pie.demo;

import com.baidu.acu.pie.client.AsrClient;
import com.baidu.acu.pie.client.AsrClientFactory;
import com.baidu.acu.pie.client.Consumer;
import com.baidu.acu.pie.exception.AsrException;
import com.baidu.acu.pie.model.AsrConfig;
import com.baidu.acu.pie.model.AsrProduct;
import com.baidu.acu.pie.model.RecognitionResult;
import com.baidu.acu.pie.model.RequestMetaData;
import com.baidu.acu.pie.model.StreamContext;
import com.baidu.acu.pie.util.JacksonUtil;
import org.joda.time.DateTime;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.TargetDataLine;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

/**
 * JavaMicrophoneDemo
 *
 * @Author Xia Shuai(xiashuai01@baidu.com)
 * @Create 2021/1/18 6:48 下午
 */
public class JavaMultiMicrophoneDemo {
    private TargetDataLine line;


    public JavaMultiMicrophoneDemo() {
        List<TargetDataLine> targetDataLines = readMicrophoneDevice();
        if (targetDataLines.size() == 0) {
            throw new RuntimeException("不存在输入设备");
        }
        this.line = targetDataLines.get(0);
    }

    public static void main(String[] args) {
        CountDownLatch latch = new CountDownLatch(1);
        new Thread(new Runnable() {
            @Override
            public void run() {
                JavaMultiMicrophoneDemo javaDemo = new JavaMultiMicrophoneDemo();
                List<TargetDataLine> targetDataLines = javaDemo.readMicrophoneDevice();

                javaDemo.setTargetDataLine(targetDataLines.get(1));
                System.out.println(targetDataLines.get(1).getLineInfo().toString());
                javaDemo.asyncRecognitionWithMicrophone(targetDataLines.get(1).hashCode() + "");
            }
        }).start();

        new Thread(new Runnable() {
            @Override
            public void run() {

                JavaMultiMicrophoneDemo javaDemo = new JavaMultiMicrophoneDemo();
                List<TargetDataLine> targetDataLines = javaDemo.readMicrophoneDevice();
                javaDemo.setTargetDataLine(targetDataLines.get(2));
                System.out.println("======>  device2: " + targetDataLines.get(2).getLineInfo().toString());
                javaDemo.asyncRecognitionWithMicrophone(targetDataLines.get(2).hashCode() + "");
            }
        }).start();

        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void setTargetDataLine(TargetDataLine line) {
        this.line = line;
    }

    private AsrConfig buildAsrConfig() {
        // asrConfig构造后就不可修改
        // 当使用ssl client时，需要配置字段sslUseFlag以及sslPath
        return AsrConfig.builder()
                .serverIp("127.0.0.1")
                .serverPort(8051)
                .appName("simpleDemo")
                .product(AsrProduct.FAR_FIELD)
                .userName("username")
                .password("password")
//                .sslUseFlag(true)
//                .sslPath("ca.crt")
                .build();
    }

    private AsrClient createAsrClient() {
        return AsrClientFactory.buildClient(buildAsrConfig());
    }

    private AudioFormat createAudioFormat() {
        AsrConfig asrConfig = buildAsrConfig();
        AudioFormat audioFormat = new AudioFormat(
                AudioFormat.Encoding.PCM_SIGNED,
                asrConfig.getProduct().getSampleRate(),
                16,
                1,
                2,    // (sampleSizeBits / 8) * channels
                asrConfig.getProduct().getSampleRate(),
                false);
        return audioFormat;
    }

    private RequestMetaData createRequestMeta() {
        RequestMetaData requestMetaData = new RequestMetaData();
        requestMetaData.setSendPackageRatio(1);
        requestMetaData.setSleepRatio(0);
        requestMetaData.setTimeoutMinutes(120);
        requestMetaData.setEnableFlushData(false);
        // 随路信息根据需要设置
        Map<String, Object> extra_info = new HashMap<>();
        extra_info.put("demo", "java");
        requestMetaData.setExtraInfo(JacksonUtil.objectToString(extra_info));

        return requestMetaData;
    }

    public List<TargetDataLine> readMicrophoneDevice() {
        Mixer.Info[] mixerInfos = AudioSystem.getMixerInfo();
        List<TargetDataLine> targetDataLines = new ArrayList<>();
        for (Mixer.Info info : mixerInfos) {
            String mixerInfo = info.getName();
            System.out.println(mixerInfo);
            TargetDataLine targetDataLine = null;
            try {
                targetDataLine = AudioSystem.getTargetDataLine(createAudioFormat(), info);
            } catch (Exception e) {
                System.out.println("err");
                continue;
            }
            try {
                Class c2 = Class.forName("com.sun.media.sound.DirectAudioDeviceProvider$DirectAudioDeviceInfo");
                Field deviceID = c2.getDeclaredField("deviceID");
                deviceID.setAccessible(true);
                int value = deviceID.getInt(info);
                System.out.println("value: " + value);
            } catch (Exception e) {
                e.printStackTrace();
            }
            System.out.println("------- > " + info);
            System.out.println("+++++ > " + info.getName());
            System.out.println("=========> mixer info:" + mixerInfo);
            System.out.println(targetDataLine.getLineInfo());
            System.out.println(targetDataLine.getFramePosition());
            targetDataLines.add(targetDataLine);
        }
        System.out.println("============> len:" + targetDataLines.size());
        return targetDataLines;
    }


    /**
     * 识别麦克风音频流
     */
    public void asyncRecognitionWithMicrophone(final String micInfo) {
        AsrClient asrClient = createAsrClient();

        RequestMetaData requestMetaData = createRequestMeta();

        StreamContext streamContext = asrClient.asyncRecognize(new Consumer<RecognitionResult>() {
            @Override
            public void accept(RecognitionResult it) {
                System.out.println(micInfo + "      " +
                        DateTime.now().toString() + "\t" + Thread.currentThread().getId() +
                        " receive fragment: " + it);
            }
        }, requestMetaData);

        streamContext.enableCallback(new Consumer<AsrException>() {
            @Override
            public void accept(AsrException e) {
                if (e != null) {
                    System.out.println(e);
                }
            }
        });

        AsrConfig asrConfig = buildAsrConfig();

//        TargetDataLine line = null;
        AudioFormat audioFormat = new AudioFormat(
                AudioFormat.Encoding.PCM_SIGNED,
                asrConfig.getProduct().getSampleRate(),
                16,
                1,
                2,    // (sampleSizeBits / 8) * channels
                asrConfig.getProduct().getSampleRate(),
                false);

        try {
            this.line.open();

//            this.line.open(audioFormat, line.getBufferSize());
            this.line.start();

            int bufferLengthInBytes = asrClient.getFragmentSize();
            byte[] data = new byte[bufferLengthInBytes];
            System.out.println("start to record");
            while ((line.read(data, 0, bufferLengthInBytes)) != -1L &&
                    !streamContext.getFinishLatch().finished()) {
                streamContext.send(data);
            }

            System.out.println(new DateTime().toString() + "\t" + Thread.currentThread().getId() + " send finish");
            streamContext.complete();

            // wait to ensure to receive the last response
            streamContext.getFinishLatch().await();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            line.close();
            asrClient.shutdown();
        }
        System.out.println("all task finished");
    }
}