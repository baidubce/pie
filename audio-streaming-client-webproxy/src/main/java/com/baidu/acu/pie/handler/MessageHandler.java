package com.baidu.acu.pie.handler;

import com.baidu.acu.pie.client.AsrClient;
import com.baidu.acu.pie.client.Consumer;
import com.baidu.acu.pie.exception.AsrException;
import com.baidu.acu.pie.exception.WebProxyException;
import com.baidu.acu.pie.model.AsrProduct;
import com.baidu.acu.pie.model.RecognitionResult;
import com.baidu.acu.pie.model.RequestMetaData;
import com.baidu.acu.pie.model.StreamContext;
import com.baidu.acu.pie.model.info.AudioData;
import com.baidu.acu.pie.model.request.AsrInitRequest;
import com.baidu.acu.pie.model.response.ServerResponse;
import com.baidu.acu.pie.model.result.AsrResult;
import com.baidu.acu.pie.service.MessageHandlerService;
import com.baidu.acu.pie.utils.JsonUtil;
import com.baidu.acu.pie.utils.WebSocketUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.websocket.CloseReason;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * webSocket message接收处理类
 */
@Slf4j
@Component
@ServerEndpoint(value = "/api/v1/asr/stream")
public class MessageHandler {

    private static MessageHandlerService messageHandlerService;

    private BlockingQueue<AudioData> blockingQueue = new LinkedBlockingDeque<AudioData>();
    private AsrClient asrClient;
    private StreamContext streamContext;

    @Autowired
    public void setMessageHandlerService(MessageHandlerService messageHandlerService) {
        MessageHandler.messageHandlerService = messageHandlerService;
    }

    @OnOpen
    public void onOpen(Session session) {
        log.info("websocket 开启连接：" + session.getId());
    }

    @OnClose
    public void onClose(Session session, CloseReason reason) {
        log.info("websocket 关闭连接:" + session.getId() + " closeReason:" + reason.toString());
        if ((asrClient != null) && (streamContext != null)) {
            try {
                blockingQueue.put(new AudioData(new byte[]{}, true));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            streamContext.complete();
            asrClient.shutdown();
        }
    }

    @OnError
    public void onError(Session session, Throwable error) {
        log.error("websocket 发生错误: " + error.getMessage());
        try {
            blockingQueue.put(new AudioData(new byte[]{}, true));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        WebSocketUtil.sendMsgToClient(session,
                ServerResponse.failureStrResponse(error.getMessage()));
    }

    /**
     * 接收字节数组(默认是音频流)
     */
    @OnMessage
    public void onMessage(Session session, byte[] messages) {
        if ((asrClient == null) || (streamContext == null)) {
            log.warn("websocket asr client does not init!");
            WebSocketUtil.sendMsgToClient(session,
                    ServerResponse.failureStrResponse("请先传入配置参数"));
            return;
        }
        log.debug("websocket get message len: {}", messages.length);
        try {
            blockingQueue.put(new AudioData(messages, false));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 接收asr配置信息
     */
    @OnMessage
    public void onMessage(Session session, String messages) {
        if ((asrClient != null) && (streamContext != null)) {
            log.warn("websocket asr client is already init!");
            WebSocketUtil.sendMsgToClient(session,
                    ServerResponse.failureStrResponse("请求参数已存在，请勿重复传递"));
            return;
        }

        log.info("websocket asr config: {}", messages);

        // 反序列化参数
        AsrInitRequest asrInitRequest;
        try {
            asrInitRequest = JsonUtil.readValue(messages, AsrInitRequest.class);
        } catch (WebProxyException e) {
            WebSocketUtil.sendFailureMsgToClient(session, "配置错误");
            return;
        }

        // 验证获取product id信息
        AsrProduct asrProduct = messageHandlerService.getAsrProduct(asrInitRequest.getProductId());
        if (asrProduct == null) {
            WebSocketUtil.sendFailureMsgToClient(session, "product id错误");
            return;
        }

        asrClient = messageHandlerService.getAsrClient(asrInitRequest);
        RequestMetaData requestMetaData = messageHandlerService.getRequestMetaData(asrInitRequest);

        this.streamContext = asrClient.asyncRecognize(new Consumer<RecognitionResult>() {
            @Override
            public void accept(RecognitionResult recognitionResult) {
                log.info("get asr result: {}", recognitionResult);
                WebSocketUtil.sendMsgToClient(session, JsonUtil.transObjectToStr(AsrResult.fromRecogniseResult(recognitionResult)));
            }
        }, requestMetaData);

        this.streamContext.enableCallback(new Consumer<AsrException>() {
            @Override
            public void accept(AsrException e) {
                if (e != null) {
                    WebSocketUtil.sendFailureMsgToClient(session, e.getMessage());
                }
            }
        });


        new Thread(new Runnable() {
            @Override
            public void run() {
                AudioData audioData;
                while (true) {
                    try {
                        audioData = blockingQueue.take();
                    } catch (InterruptedException e) {
                        WebSocketUtil.close(session);
                        break;
                    }
                    if (audioData.isClose()) {
                        WebSocketUtil.close(session);
                        break;
                    }
                    log.debug("audio send to server");
                    streamContext.send(audioData.getAudioBytes());
                }
                log.info("asr send finish in new thread");
            }

        }).start();

        log.info("websocket asr client init finish!");
    }
}
