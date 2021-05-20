package com.baidu.acu.asr.async;

import com.baidu.acu.asr.model.Args;
import com.baidu.acu.pie.client.AsrClient;
import com.baidu.acu.pie.client.AsrClientFactory;
import com.baidu.acu.pie.model.AsrConfig;
import com.baidu.acu.pie.model.RequestMetaData;
import com.baidu.acu.pie.model.StreamContext;
import com.baidu.acu.pie.util.JacksonUtil;
import org.joda.time.DateTime;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.TargetDataLine;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * AsyncRecogniseWithMultiMicrophone
 *
 * @author Xia Shuai(xiashuai01@baidu.com)
 * @literal create at 2021/3/3 2:32 下午
 */
public class AsyncRecogniseWithMultiMicrophone {
    private static Args args;

    private static String appName = "multiMicrophone";     // 根据自己需求命名

    public static void main(String[] args) {
        AsyncRecogniseWithMultiMicrophone.args = Args.parse(args);
        List<Mixer.Info> MixerInfoList = readMicrophoneDevice();
        // 默认的麦克风忽略
        boolean removeDefault = true;

        for (Mixer.Info mixerInfo : MixerInfoList) {
            if (removeDefault) {
                removeDefault = false;
                continue;
            }

            new Thread(() -> asyncRecognitionWithMicrophone(mixerInfo)).start();
        }
    }

    private static AsrClient createAsrClient() {
        // 创建调用asr服务的客户端
        // asrConfig构造后就不可修改
        return AsrClientFactory.buildClient(buildAsrConfig());
    }

    private static AsrConfig buildAsrConfig() {
        // asrConfig构造后就不可修改
        // 当使用ssl client时，需要配置字段sslUseFlag以及sslPath
        return AsrConfig.builder()
                .appName(appName)
                .serverIp(args.getIp())
                .serverPort(args.getPort())
                .product(Args.parseProduct(args.getProductId()))
                .userName(args.getUsername())
                .password(args.getPassword())
                .build();
    }

    private static RequestMetaData createRequestMeta() {
        RequestMetaData requestMetaData = new RequestMetaData();
        requestMetaData.setSendPackageRatio(1);
        requestMetaData.setSleepRatio(0);
        requestMetaData.setTimeoutMinutes(120);
        requestMetaData.setEnableFlushData(true);
        // 随路信息根据需要设置
        Map<String, Object> extra_info = new HashMap<>();
        extra_info.put("demo", "java");
        requestMetaData.setExtraInfo(JacksonUtil.objectToString(extra_info));

        return requestMetaData;
    }

    private static AudioFormat createAudioFormat() {
        AsrConfig asrConfig = buildAsrConfig();
        return new AudioFormat(
                AudioFormat.Encoding.PCM_SIGNED,
                asrConfig.getProduct().getSampleRate(),
                16,
                1,
                2,    // (sampleSizeBits / 8) * channels
                asrConfig.getProduct().getSampleRate(),
                false);
    }

    public static List<Mixer.Info> readMicrophoneDevice() {
        Mixer.Info[] mixerInfos = AudioSystem.getMixerInfo();
        List<Mixer.Info> MixerInfoList = new ArrayList<>();
        for (Mixer.Info info : mixerInfos) {
            try {
                AudioSystem.getTargetDataLine(createAudioFormat(), info);
            } catch (Exception e) {
                continue;
            }
            MixerInfoList.add(info);
        }
        return MixerInfoList;
    }

    /**
     * 识别麦克风音频流
     */
    public static void asyncRecognitionWithMicrophone(Mixer.Info mixerInfo) {
        TargetDataLine targetDataLine;

        try {
            targetDataLine = AudioSystem.getTargetDataLine(createAudioFormat(), mixerInfo);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        AsrClient asrClient = createAsrClient();

        RequestMetaData requestMetaData = createRequestMeta();

        StreamContext streamContext = asrClient.asyncRecognize(it -> System.out.println(mixerInfo.getName() + "\t" +
                DateTime.now() + "\t" +
                Thread.currentThread().getId() +
                " receive fragment: " + it), requestMetaData);

        streamContext.enableCallback(e -> {
            if (e != null) {
                e.printStackTrace();
            }
        });

        FileOutputStream fop = null;
        try {
            if (!args.getAudioPath().equals("")) {
                File file = Paths.get(args.getAudioPath(), generateAudioName(mixerInfo.getName())).toFile();
                if (file.exists() || file.createNewFile()) {
                    fop = new FileOutputStream(file);
                }
            }

            targetDataLine.open();
            targetDataLine.start();

            int bufferLengthInBytes = asrClient.getFragmentSize();
            byte[] data = new byte[bufferLengthInBytes];
            System.out.println("start to record =======> " + mixerInfo.getName());
            while ((targetDataLine.read(data, 0, bufferLengthInBytes)) != -1L &&
                    !streamContext.getFinishLatch().finished()) {
                streamContext.send(data);
                if (fop != null) {
                    fop.write(data);
                    fop.flush();
                }
            }

            System.out.println(new DateTime() + "\t" + Thread.currentThread().getId() + " send finish");
            streamContext.complete();

            // wait to ensure to receive the last response
            streamContext.getFinishLatch().await();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            targetDataLine.close();
            asrClient.shutdown();

            if (fop != null) {
                try {
                    fop.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        System.out.println("all task finished");
    }

    private static String generateAudioName(String mixerInfo) {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        String time = dateTimeFormatter.format(LocalDateTime.now());
        return String.format("%s%s(%s).pcm", time, UUID.randomUUID(), mixerInfo);
    }
}
