package com.baidu.acu.pie.service;

import com.baidu.acu.pie.constant.Constant;
import com.baidu.acu.pie.constant.RequestType;
import com.baidu.acu.pie.handler.AudioAsrHandler;
import com.baidu.acu.pie.model.info.AudioData;
import com.baidu.acu.pie.model.response.ServerResponse;
import com.baidu.acu.pie.utils.WebSocketUtil;
import com.baidu.acu.pie.utils.WxUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.websocket.Session;
import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class AudioHandlerService {

    private final LoginHandlerService loginHandlerService;

    private Map<String, AudioAsrHandler> asrClients = new ConcurrentHashMap<>();

    /**
     * 处理音频id类型的asr解析
     */
    public void handle(Session session, String audioId) {

        if(!loginHandlerService.userExists(session)) {
            WebSocketUtil.sendMsgToClient(session,
                    ServerResponse.failureStrResponse(Constant.MUST_LOGIN_BEFORE_USE, RequestType.ASR));
            return;
        }
        try {
            InputStream inputStream = WxUtil.getAudioStream(audioId);
            AudioData audioData = new AudioData();
            audioData.setAudioId(audioId);
            audioData.setInputStream(inputStream);
            put(session, audioData);
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }

    /**
     * 处理二进制数据解析
     */
    public void Handle(byte[] audioBytes, Session session) {
        if(!loginHandlerService.userExists(session)) {
            WebSocketUtil.sendMsgToClient(session,
                    ServerResponse.failureStrResponse(Constant.MUST_LOGIN_BEFORE_USE, RequestType.ASR));
            return;
        }
        byte[] copy = new byte[audioBytes.length];
        System.arraycopy(audioBytes, 0, copy, 0, audioBytes.length);
        AudioData audioData = new AudioData();
        audioData.setAudioBytes(copy);
        put(session, audioData);
    }

    /**
     * 取消asr客户端
     */
    public void unRegisterClient(Session session) {
        asrClients.remove(session.getId());
    }

    /**
     * 将收到的音频数据，放到asr处理器中
     */
    private void put(Session session, AudioData audioData) {
        String id = session.getId();
        if (!asrClients.containsKey(id)) {
            registerClient(session);
        }
        AudioAsrHandler asr = asrClients.get(id);
        asr.offer(audioData);
    }

    /**
     * 将session注册至map中
     */
    private synchronized void registerClient(Session session) {
        String id = session.getId();
        if (asrClients.containsKey(id)) {
            return;
        }
        AudioAsrHandler asrHandler = new AudioAsrHandler(session, this);
        asrClients.put(session.getId(), asrHandler);
        new Thread(asrHandler).start();
    }

}
