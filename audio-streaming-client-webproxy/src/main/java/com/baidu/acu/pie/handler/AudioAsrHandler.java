package com.baidu.acu.pie.handler;

import com.baidu.acu.pie.client.AsrClient;
import com.baidu.acu.pie.client.AsrClientFactory;
import com.baidu.acu.pie.model.AsrConfig;
import com.baidu.acu.pie.model.AsrProduct;
import com.baidu.acu.pie.model.RecognitionResult;
import com.baidu.acu.pie.model.RequestMetaData;
import com.baidu.acu.pie.model.StreamContext;
import com.baidu.acu.pie.model.response.ServerResponse;
import com.baidu.acu.pie.model.result.MediaResult;
import com.baidu.acu.pie.service.SessionManager;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;

import javax.websocket.Session;
import java.io.IOException;
import java.util.ArrayList;

/**
 * asr解析处理
 */
@Slf4j
public class AudioAsrHandler implements Runnable {

    private Session session;
    private ArrayList<byte[]> queue;
    private SessionManager sessionManager;


    public AudioAsrHandler(Session session, SessionManager sessionManager) {
        this.session = session;
        this.sessionManager = sessionManager;

        //TODO 暂时不考虑性能，先确保队列先进先处理，后期改成queue优化
        queue = new ArrayList<>(10000);
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

        // 发送asr数据
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

                if (queue.size() > 0) {
                    byte[] data =  queue.remove(0);
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
                .serverIp("asr.baiduai.cloud")
                .serverPort(8051)
                .appName("zhenhua")
                .product(AsrProduct.CUSTOMER_SERVICE_FINANCE)
                .userName("ppdaitest")
                .password("ppdai")
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
        MediaResult mediaResult = new MediaResult();
        mediaResult.setAsrResult(result.getResult());
        mediaResult.setCompleted(result.isCompleted());
        mediaResult.setFinished(false);
        ServerResponse<MediaResult> response = ServerResponse.successResponse(mediaResult);

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
