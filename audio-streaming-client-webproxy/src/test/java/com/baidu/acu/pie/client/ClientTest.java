package com.baidu.acu.pie.client;

import com.baidu.acu.pie.model.request.AsrInitRequest;
import com.baidu.acu.pie.utils.JsonUtil;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.TargetDataLine;
import javax.websocket.ContainerProvider;
import javax.websocket.Session;
import javax.websocket.WebSocketContainer;
import java.net.URI;
import java.nio.ByteBuffer;

/**
 * ClientTest
 *
 * @Author Xia Shuai(xiashuai01@baidu.com)
 * @Create 2020/10/23 2:15 下午
 */
@Slf4j
public class ClientTest {
    static Session session;

    public static void main(String[] args) {
        try {
            AsrInitRequest asrInitRequest = new AsrInitRequest();
            asrInitRequest.setAppName("websocket");
            asrInitRequest.setEnableFlushData(true);
            asrInitRequest.setUserName("user");
            asrInitRequest.setPassword("password");
            asrInitRequest.setProductId("1912");
            asrInitRequest.setSleepRatio(0);

            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
            String uri = "ws://172.18.53.15:8085/api/v1/asr/stream";
            container.connectToServer(AsrWebSocketClient.class, URI.create(uri));
            session.getBasicRemote().sendText(JsonUtil.transObjectToStr(asrInitRequest));
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

                int readLen = 640;

                byte[] data = new byte[readLen];
                System.out.println("start to record");
                int index = 0;
                while ((line.read(data, 0, readLen)) != -1L) {
//                    System.out.println("send: " + index++);
//                    log.info("data: {}", data);
                    ByteBuffer bb = ByteBuffer.wrap(data);
                    session.getBasicRemote().sendBinary(bb);
                }

                System.out.println(new DateTime().toString() + "\t" + Thread.currentThread().getId() + " send finish");

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                line.close();
            }

            session.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
