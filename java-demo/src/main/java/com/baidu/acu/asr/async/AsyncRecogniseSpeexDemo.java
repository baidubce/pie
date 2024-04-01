package com.baidu.acu.asr.async;

import com.baidu.acu.asr.model.AsrArgs;
import com.baidu.acu.pie.client.AsrClient;
import com.baidu.acu.pie.client.AsrClientFactory;
import com.baidu.acu.pie.client.Consumer;
import com.baidu.acu.pie.exception.GlobalException;
import com.baidu.acu.pie.model.AsrConfig;
import com.baidu.acu.pie.model.RecognitionResult;
import com.baidu.acu.pie.model.RequestMetaData;
import com.baidu.acu.pie.model.StreamContext;
import com.baidu.acu.pie.util.JacksonUtil;
import io.netty.util.internal.StringUtil;
import org.joda.time.DateTime;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * SpeexDemo
 *
 * @author Xia Shuai(xiashuai01@baidu.com)
 */
public class AsyncRecogniseSpeexDemo {
    private static AsrArgs asrArgs;
    public static void main(String[] args) {
        // java -jar java-demo-1.2.2-SNAPSHOT-shaded.jar -ip 127.0.0.1 -port 8051 -pid 1912 -username username -password password -enable-flush-data -audio-path speex.txt -times 1 -sleep-time=10
        AsyncRecogniseSpeexDemo speexDemo = new AsyncRecogniseSpeexDemo();
        try {
            AsyncRecogniseSpeexDemo.asrArgs = AsrArgs.parse(args);
            speexDemo.testSpeexRequest();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void testSpeexRequest() throws IOException, InterruptedException {
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

        streamContext.enableCallback(new Consumer<GlobalException>() {
            @Override
            public void accept(GlobalException e) {
                if (e != null) {
                    System.out.println(e);
                }
            }
        });

        List<String> audioContents = Files.readAllLines(Paths.get(asrArgs.getAudioPath()));
//        InputStream resourceAsStream = getClass().getResourceAsStream("/speex.txt");
//        byte[] bytes = ByteStreams.toByteArray(resourceAsStream);
//        String content = new String(bytes);
//        Gson gson = new GsonBuilder().create();
//        Result results = gson.fromJson(content, Result.class);

        for (int i = 0; i < asrArgs.getTimes(); i++) {
            for (String base64 : audioContents) {
                if (StringUtil.isNullOrEmpty(base64)) {
                    continue;
                }
                base64 = base64.replace("\"", "")
                        .replace(",", "")
                        .replace(" ", "");
                byte[] decode = Base64.getDecoder().decode(base64);
                int index = 0;
                int sliceSize = 60;
                while ((index + 1) * sliceSize <= decode.length) {
                    byte[] bytes1 = Arrays.copyOfRange(decode, index * sliceSize, (index + 1) * sliceSize);
                    streamContext.send(bytes1);
                    index++;
                    TimeUnit.MILLISECONDS.sleep(asrArgs.getSleepTime());
                }
            }
        }

        streamContext.complete();

        streamContext.getFinishLatch().await();
        asrClient.shutdown();
//        byte[] content = {1, 2, 3};

//        streamContext.send(content);
//        streamContext.send(content);
//        streamContext.send(content);
//        streamContext.send(content);
    }

    private AsrConfig buildAsrConfig() {
        // asrConfig构造后就不可修改
        // 当使用ssl client时，需要配置字段sslUseFlag以及sslPath
        return AsrConfig.builder()
                .appName("speex")
                .serverIp(asrArgs.getIp())
                .serverPort(asrArgs.getPort())
                .product(AsrArgs.parseProduct(asrArgs.getProductId()))
                .userName(asrArgs.getUsername())
                .password(asrArgs.getPassword())
                .build();
    }

    private AsrClient createAsrClient() {
        return AsrClientFactory.buildClient(buildAsrConfig());
    }

    private RequestMetaData createRequestMeta() {
        RequestMetaData requestMetaData = new RequestMetaData();
        requestMetaData.setSendPackageRatio(1);
        requestMetaData.setSleepRatio(asrArgs.getSleepRatio());
        requestMetaData.setTimeoutMinutes(120);
        requestMetaData.setEnableFlushData(true);
        // 随路信息根据需要设置
        Map<String, Object> extra_info = new HashMap<>();
        extra_info.put("demo", "java");
        requestMetaData.setExtraInfo(JacksonUtil.objectToString(extra_info));

        return requestMetaData;
    }
}
