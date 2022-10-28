package com.baidu.acu.asr.async;

/**
 * MyWebSocketClient created at 2022/8/31 16:50
 * 客户提供的客户端
 *
 * @author Xia Shuai(xiashuai01@baidu.com)
 */

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.json.JSONUtil;
import java.net.URI;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.TargetDataLine;
import lombok.SneakyThrows;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

public class MyMicrophoneWebSocketClient extends WebSocketClient {

    private static Map<String, Long> map = new HashMap<>();

    public MyMicrophoneWebSocketClient(URI serverURI) {
        super(serverURI);
    }

    @SneakyThrows
    @Override
    public void onOpen(ServerHandshake serverHandshake) {

    }

    @Override
    public void onMessage(String message) {
        System.out.println(message);
        String message1 = JSONUtil.parseObj(message).getStr("message");
        if ("NLP".equals(message1)) {
            Long sendTimeMillis = map.get("sendTimeMillis");
            if (sendTimeMillis != null && (System.currentTimeMillis() - sendTimeMillis) > 500) {
                System.out.println("发送end包到接收到nlp结果耗时=" + (System.currentTimeMillis() - sendTimeMillis));
            }
            map.remove("sendTimeMillis");
        }
    }

    @Override
    public void onClose(int i, String s, boolean b) {
    }

    @Override
    public void onError(Exception e) {
    }

    public void sendMsg() throws Exception {
//        String request_id = UUID.randomUUID().toString();
        String request_id = "729b4f1cq59ewa1075o61180yvk8usnm";
        String endFlag = "--end--";
        long request_time = DateUtil.current();
        ;
        System.out.println("request_id-----" + request_id);
        String config = "{\"device_id\":\"185252610000198\",\"mobile\":\"15512341234\",\"cmd_type\":\"2\",\"tts\":\"0\",\"request_time\":\"" + request_time + "\",\"vendor\":\"讯飞\",\"request_id\":\"" + request_id + "\",\"config\":{\"config\":\"config\"}}";
        send(config);

        TargetDataLine line = null;
        AudioFormat audioFormat = new AudioFormat(
                AudioFormat.Encoding.PCM_SIGNED,
                16000,
                16,
                1,
                2,    // (sampleSizeBits / 8) * channels
                16000,
                false);

        DataLine.Info info = new DataLine.Info(
                TargetDataLine.class,
                audioFormat);

        if (!AudioSystem.isLineSupported(info)) {
            System.out.println("line not support");
            return;
        }

        try {
            line = (TargetDataLine) AudioSystem.getLine(info);

            line.open(audioFormat, line.getBufferSize());
            line.start();

            int bufferLengthInBytes = 640;
            byte[] data = new byte[bufferLengthInBytes];
            System.out.println("start to record");
            while ((line.read(data, 0, bufferLengthInBytes)) != -1L) {
                String base64Audio = Base64.getEncoder().encodeToString(data);
                send("{\"bytes\":\"" + base64Audio + "\"}");
            }

            System.out.println(new DateTime().toString() + "\t" + Thread.currentThread().getId() + " send finish");

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            line.close();
        }

        String base64End = Base64.getEncoder().encodeToString(endFlag.getBytes());
        send("{\"bytes\":\"" + base64End + "\"}");
        map.put("sendTimeMillis", System.currentTimeMillis());
        System.out.println("发送结束标识完成");
    }

    public static void main(String[] args) throws Exception {
//        while (true){
        //号百
        URI url1 = new URI("ws://117.83.109.234:8686/getAIService?token=IlPZngmXy8ZMUgxb8YQeIg83DroaMytfdO98Dq2UYv4OYPXm53wGQ0gtHlOPY3mY&app_id=486161c0-7e2a-4e52-92c0-735c04fb3784");
        URI url2 = new URI("ws://audio.189smarthome.com:8686/getAIService?token=IlPZngmXy8ZMUgxb8YQeIg83DroaMytfdO98Dq2UYv4OYPXm53wGQ0gtHlOPY3mY&app_id=486161c0-7e2a-4e52-92c0-735c04fb3784");
        //捷通
        URI url3 = new URI("ws://117.83.109.234:8686/getAIService?token=W3hlPUzphdnL7I1FepKDm/2dwJeG5DxWzlQ5vI5HfajHYeLeRDKwILt8nvjreles&app_id=486161c0-7e2a-4e52-92c0-735c04fb3774");
        URI url4 = new URI("ws://audio.189smarthome.com:8686/getAIService?token=W3hlPUzphdnL7I1FepKDm/2dwJeG5DxWzlQ5vI5HfajHYeLeRDKwILt8nvjreles&app_id=486161c0-7e2a-4e52-92c0-735c04fb3774");
        //百度
        URI url5 = new URI("ws://117.83.109.234:8686/getAIService?token=9v6WoqhU1PilLtVKujtxADjEx02i7SFGXCzQWPUlVw8Asx/Bq1PhfjRFAVf0dPEq&app_id=486161c0-7e2a-4e52-92c0-735c04fb3754");
        URI url6 = new URI("ws://audio.189smarthome.com:8686/getAIService?token=9v6WoqhU1PilLtVKujtxADjEx02i7SFGXCzQWPUlVw8Asx/Bq1PhfjRFAVf0dPEq&app_id=486161c0-7e2a-4e52-92c0-735c04fb3754");
        // 百度测试环境
        MyMicrophoneWebSocketClient myClient = new MyMicrophoneWebSocketClient(url5);
        myClient.connect();
        while (!myClient.isOpen()) {
            System.out.println("还没有打开..." + myClient.getReadyState().toString());
            Thread.sleep(100);
        }
        System.out.println("连接成功...");
        myClient.sendMsg();
        Thread.sleep(3000);
        myClient.close();
//        }
    }

}