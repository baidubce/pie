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
import com.google.common.io.ByteStreams;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.joda.time.DateTime;

/**
 * SpeexDemo
 *
 * @author  Xia Shuai(xiashuai01@baidu.com)
 */
public class SpeexDemo {
    public static void main(String[] args) {
        SpeexDemo speexDemo = new SpeexDemo();
        try {
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

        streamContext.enableCallback(new Consumer<AsrException>() {
            @Override
            public void accept(AsrException e) {
                if (e != null) {
                    System.out.println(e);
                }
            }
        });

        InputStream resourceAsStream = getClass().getResourceAsStream("/speex.txt");
        byte[] bytes = ByteStreams.toByteArray(resourceAsStream);
        String content = new String(bytes);
        Gson gson = new GsonBuilder().create();
        Result results = gson.fromJson(content, Result.class);

        for (String base64 : results.getResult()) {
            byte[] decode = Base64.getDecoder().decode(base64);
            int index = 0;
            int sliceSize = 60;
            while ((index + 1) * sliceSize <= decode.length) {
                byte[] bytes1 = Arrays.copyOfRange(decode, index * sliceSize, (index + 1) * sliceSize);
                streamContext.send(bytes1);
                index++;
            }
        }
        streamContext.complete();

        streamContext.getFinishLatch().await();
        asrClient.shutdown();
    }

    private AsrConfig buildAsrConfig() {
        // asrConfig构造后就不可修改
        // 当使用ssl client时，需要配置字段sslUseFlag以及sslPath
        return AsrConfig.builder()
                .serverIp("127.0.0.1")
                .serverPort(8051)
                .appName("speex")
                .product(AsrProduct.SPEECH_SERVICE)
                .userName("user")
                .password("password")
//                .sslUseFlag(true)
//                .sslPath("ca.crt")
                .build();
    }

    private AsrClient createAsrClient() {
        return AsrClientFactory.buildClient(buildAsrConfig());
    }

    private RequestMetaData createRequestMeta() {
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

    public static class Result {
        private List<String> result;

        public List<String> getResult() {
            return result;
        }

        public void setResult(List<String> result) {
            this.result = result;
        }
    }
}
