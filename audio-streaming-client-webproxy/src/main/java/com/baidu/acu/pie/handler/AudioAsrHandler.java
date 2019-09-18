package com.baidu.acu.pie.handler;

import com.baidu.acu.pie.client.AsrClient;
import com.baidu.acu.pie.client.AsrClientFactory;
import com.baidu.acu.pie.model.AsrConfig;
import com.baidu.acu.pie.model.RecognitionResult;
import com.baidu.acu.pie.model.RequestMetaData;
import com.baidu.acu.pie.model.StreamContext;
import com.baidu.acu.pie.model.response.ServerResponse;
import com.baidu.acu.pie.model.result.AsrResult;
import com.baidu.acu.pie.service.SessionManager;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;

import javax.websocket.Session;
import java.io.IOException;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * asr解析处理
 */
@Slf4j
public class AudioAsrHandler implements Runnable {

    private Session session;
    private Queue<byte[]> queue;
    private SessionManager sessionManager;


    public AudioAsrHandler(Session session, SessionManager sessionManager) {
        this.session = session;
        this.sessionManager = sessionManager;

        queue = new ConcurrentLinkedQueue<>();
    }

    @Override
    public void run() {
        asyncRecognition();
    }

    /**
     * 开启识别过程
     */
    private void asyncRecognition() {

        AsrClient asrClient = createAsrClient();

        RequestMetaData requestMetaData = createRequestMeta();

        StreamContext streamContext = asrClient.asyncRecognize(it -> {
            log.info(
                    DateTime.now().toString() + "\t" + Thread.currentThread().getId() +
                            " receive fragment: " + it);
            handlerRecognitionResult(it);
        }, requestMetaData);

        try {

            // 10秒等待时间
            int waitingCount = 0;
            while (waitingCount <= 100) {

                if (!queue.isEmpty()) {
                    byte[] data =  queue.poll();
                    waitingCount = 0;
                    streamContext.send(data);
                } else {
                    Thread.sleep(100);
                    waitingCount ++;
                }
            }
            streamContext.complete();
            streamContext.getFinishLatch().await();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            asrClient.shutdown();
            sessionManager.unRegister(session);
        }
    }

    /**
     * 创建asr客户端
     */
    private AsrClient createAsrClient() {
        // asrConfig构造后就不可修改
        // TODO 后期优化，暂时写死
        AsrConfig asrConfig = AsrConfig.builder()
                .build();

        return AsrClientFactory.buildClient(asrConfig);
    }

    private RequestMetaData createRequestMeta() {
        RequestMetaData requestMetaData = new RequestMetaData();
        requestMetaData.setSendPackageRatio(1);
        requestMetaData.setSleepRatio(1);
        requestMetaData.setTimeoutMinutes(120);
        requestMetaData.setEnableFlushData(true);

        return requestMetaData;
    }

    /**
     * 处理识别结果
     */
    private void handlerRecognitionResult (RecognitionResult result) {
        AsrResult asrResult = new AsrResult();
        asrResult.setAsrResult(result.getResult());
        asrResult.setCompleted(result.isCompleted());
        asrResult.setFinished(false);
        ServerResponse<AsrResult> response = ServerResponse.successResponse(asrResult);

        try {
            session.getBasicRemote().sendText(response.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 向队列中传送数据
     */
    public synchronized void offer(byte[] data) {
        byte[] copy = new byte[data.length];
        System.arraycopy(data, 0, copy, 0, data.length);
        queue.add(copy);

    }
}
