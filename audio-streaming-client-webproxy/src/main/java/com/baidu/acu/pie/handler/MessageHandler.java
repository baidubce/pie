package com.baidu.acu.pie.handler;

import com.baidu.acu.pie.service.SessionManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
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

    private static SessionManager sessionManager;


    @PostConstruct
    public void init() {
        sessionManager = new SessionManager();
    }

    @OnOpen
    public void onOpen(Session session) {
        log.info("开启连接：" + session.getId());
    }

    @OnClose
    public void onClose(Session session, CloseReason reason) {
        log.info("关闭连接:" + session.getId());
        sessionManager.unRegister(session);
    }

    @OnError
    public void onError(Session session, Throwable error){
        System.out.println("发生错误");
        sessionManager.unRegister(session);
        error.printStackTrace();
    }

    /**
     * 接收字节数组(默认是音频流)
     */
    @OnMessage
    public void onMessage(byte[] messages, Session session) {
        try {
            sessionManager.put(session, messages);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    /**
     * 接收string（未作逻辑处理）
     */
    @OnMessage
    public void onMessage(String messages, Session session) {
        try {

            session.getBasicRemote().sendText("接收到的信息："+ messages);

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }



}
