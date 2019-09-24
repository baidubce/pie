package com.baidu.acu.pie.handler;

import com.baidu.acu.pie.client.AsrClient;
import com.baidu.acu.pie.client.AsrClientFactory;
import com.baidu.acu.pie.constant.RequestType;
import com.baidu.acu.pie.model.AsrConfig;
import com.baidu.acu.pie.model.RecognitionResult;
import com.baidu.acu.pie.model.RequestMetaData;
import com.baidu.acu.pie.model.StreamContext;
import com.baidu.acu.pie.model.info.AudioData;
import com.baidu.acu.pie.model.response.ServerResponse;
import com.baidu.acu.pie.model.result.AsrResult;
import com.baidu.acu.pie.service.AudioHandlerService;
import com.baidu.acu.pie.utils.WebSocketUtil;
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
    private Queue<AudioData> queue;
    private AudioHandlerService audioHandlerService;
    private StreamContext streamContext;
    private AsrClient asrClient;
    private String audioId = null;


    public AudioAsrHandler(Session session, AudioHandlerService audioHandlerService) {
        this.session = session;
        this.audioHandlerService = audioHandlerService;

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
        asrClient = createAsrClient();
        initStreamContext();
        try {
            // 10秒等待时间
            int waitingCount = 0;
            while (waitingCount <= 100) {

                if (!queue.isEmpty()) {
                    AudioData audioData = queue.poll();
                    waitingCount = 0;
                    send(audioData);
                } else {
                    Thread.sleep(100);
                    waitingCount ++;
                }
            }
            streamContext.complete();
            streamContext.getFinishLatch().await();

        } catch (Exception e) {
            log.info("asr recognition occur exception:" + e.getMessage());
        } finally {
            asrClient.shutdown();
            audioHandlerService.unRegisterClient(session);
        }
    }

    /**
     * 初始化initStreamContext
     */
    private void initStreamContext() {
        RequestMetaData requestMetaData = createRequestMeta();
        streamContext = asrClient.asyncRecognize(it -> {
            log.info(DateTime.now().toString() + Thread.currentThread().getId() + " receive fragment: " + it);
            handleRecognitionResult(it);
        }, requestMetaData);
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
        requestMetaData.setEnableFlushData(false);

        return requestMetaData;
    }

    /**
     * 处理识别结果
     */
    private void handleRecognitionResult (RecognitionResult result) {
        AsrResult asrResult = new AsrResult();
        asrResult.setAsrResult(result.getResult());
        asrResult.setCompleted(result.isCompleted());
        asrResult.setAudioId(audioId);
        WebSocketUtil.sendMsgToClient(session, ServerResponse.successStrResponse(asrResult, RequestType.ASR));

    }

    /**
     * 向队列中传送数据
     */
    public synchronized void offer(AudioData audioData) {
        queue.add(audioData);

    }

    /**
     * 向asr发送数据逻辑，可能包含二进制数组和inputStream两种方式
     * 若是音频id形式，那么每次处理完该音频后（），再处理下一条音频
     */
    private void send(AudioData audioData) throws IOException, InterruptedException {
        if (audioData.getAudioId() != null) {
            audioId = audioData.getAudioId();
            byte[] data = new byte[320];
            while (audioData.getInputStream().read(data) != -1) {
                streamContext.send(data);
            }
            streamContext.complete();
            streamContext.getFinishLatch().await();
            audioId = null;
            initStreamContext();
            return;
        }
        streamContext.send(audioData.getAudioBytes());
    }
}
