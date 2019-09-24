package com.baidu.acu.pie.handler;

import com.baidu.acu.pie.service.AudioHandlerService;
import com.baidu.acu.pie.service.LoginHandlerService;
import com.baidu.acu.pie.service.MessageHandlerService;
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

/**
 *  webSocket message接收处理类
 */
@Slf4j
@Component
@ServerEndpoint(value="/ws/v1/asr")

public class MessageHandler {

    private static MessageHandlerService messageHandlerService;
    private static LoginHandlerService loginHandlerService;
    private static AudioHandlerService audioHandlerService;

    @Autowired
    public void setMessageHandlerService(MessageHandlerService messageHandlerService) {
        MessageHandler.messageHandlerService = messageHandlerService;
    }

    @Autowired
    public void setLoginHandlerService(LoginHandlerService loginHandlerService) {
        MessageHandler.loginHandlerService = loginHandlerService;
    }

    @Autowired
    public void setAudioHandlerService(AudioHandlerService audioHandlerService) {
        MessageHandler.audioHandlerService = audioHandlerService;
    }


    @OnOpen
    public void onOpen(Session session) {
        log.info("开启连接：" + session.getId());
    }

    @OnClose
    public void onClose(Session session, CloseReason reason) {
        log.info("关闭连接:" + session.getId() +" closeReason:" + reason.toString());
        loginHandlerService.cancelLogin(session);
    }

    @OnError
    public void onError(Session session, Throwable error) {
        log.error("发生错误" + error.getMessage());
        loginHandlerService.cancelLogin(session);
        error.printStackTrace();
    }

    /**
     * 接收字节数组(默认是音频流)
     */
    @OnMessage
    public void onMessage(Session session, byte[] messages) {
        audioHandlerService.Handle(messages, session);
    }

    /**
     * 接收string（未作逻辑处理）
     */
    @OnMessage
    public void onMessage(Session session, String messages) {
        messageHandlerService.handle(session, messages);
    }
}
