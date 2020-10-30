package com.baidu.acu.pie.client;

import com.baidu.acu.pie.model.request.AsrInitRequest;
import com.baidu.acu.pie.utils.JsonUtil;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;

import javax.websocket.ContainerProvider;
import javax.websocket.Session;
import javax.websocket.WebSocketContainer;
import java.io.InputStream;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.CountDownLatch;

/**
 * ClientTest
 *
 * @Author Xia Shuai(xiashuai01@baidu.com)
 * @Create 2020/10/23 2:15 下午
 */
@Slf4j
public class FileClientTest {
    static Session session;

    public static void main(String[] args) {
        String longAudioFilePath = "/Users/xiashuai01/Downloads/16k.wav";
        try {
            AsrInitRequest asrInitRequest = new AsrInitRequest();
            asrInitRequest.setAppName("websocket");
            asrInitRequest.setEnableFlushData(true);
            asrInitRequest.setUserName("user");
            asrInitRequest.setPassword("password");
            asrInitRequest.setProductId("1912");
            asrInitRequest.setSleepRatio(0);

            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
            String uri = "ws://127.0.0.1:8081/api/v1/asr/stream";
            container.connectToServer(FileAsrWebSocketClient.class, URI.create(uri));
            session.getBasicRemote().sendText(JsonUtil.transObjectToStr(asrInitRequest));
            CountDownLatch latch = new CountDownLatch(1);
            // 这里从文件中得到一个InputStream，实际场景下，也可以从麦克风或者其他音频源来得到InputStream
            try (InputStream audioStream = Files.newInputStream(Paths.get(longAudioFilePath))) {
                byte[] data = new byte[640];

                System.out.println(new DateTime().toString() + "\t" + Thread.currentThread().getId() + " start to send");

                // 使用 sender.onNext 方法，将 InputStream 中的数据不断地发送到 asr 后端，发送的最小单位是 AudioFragment
                while (audioStream.read(data) != -1) {
                    ByteBuffer bb = ByteBuffer.wrap(data);
                    session.getBasicRemote().sendBinary(bb);
                    // 主动休眠一段时间，来模拟人说话场景下的音频产生速率
                    // 在对接麦克风等设备的时候，可以去掉这个 sleep
                    Thread.sleep(20);
                }
                System.out.println(new DateTime().toString() + "\t" + Thread.currentThread().getId() + " send finish");
                latch.await();
            } catch (Throwable e) {
                e.printStackTrace();
            }
            session.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
