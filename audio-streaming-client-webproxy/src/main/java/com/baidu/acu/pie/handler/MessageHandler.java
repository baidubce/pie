package com.baidu.acu.pie.handler;

import com.baidu.acu.pie.service.SessionManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 *  webSocket message接收处理类
 */
@Slf4j
@Component
@ServerEndpoint(value="/ws/v1/videoSearch")
public class MessageHandler {

    private static SessionManager sessionManager;


    @PostConstruct
    public void init() {
        sessionManager = new SessionManager();
    }

    @OnOpen
    public void onOpen(Session session) {

    }

    @OnClose
    public void onClose() {
        System.out.println("关闭连接");
    }

    @OnError
    public void onError(Session session, Throwable error){
        System.out.println("发生错误");
        sessionManager.unRegister(session);
        error.printStackTrace();
    }

    /**
     * 接收字节数组
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
     * 接收string
     */
    @OnMessage
    public void onMessage(String messages, Session session) {
        try {

            String longAudioFilePath = "/Users/mazhenhua/Desktop/test.wav";
            try (InputStream audioStream = Files.newInputStream(Paths.get(longAudioFilePath))) {
                byte[] data = new byte[4096];
                while (audioStream.read(data) != -1) {
                    sessionManager.put(session, data);
                }
            }

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }



}
